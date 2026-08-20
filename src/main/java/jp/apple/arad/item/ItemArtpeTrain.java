package jp.apple.arad.item;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.*;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationEntry;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 編成召喚アイテム（旧APE ItemArtpeTrain をARAD内に内包した1.7.10版）
 * NBT形式: formations (NBTTagList[10]) / 各要素: model, index, pos_x, pos_y, pos_z,
 * yaw, pitch, dir
 */
@SuppressWarnings("unused")
public class ItemArtpeTrain extends Item {

    private static final AtomicLong lastId = new AtomicLong(0);
    private static final int SEARCH_SPLIT = 128; // 1.7.10 ItemTrain相当
    private static final int POS_SPLIT = 128;

    public ItemArtpeTrain() {
        super();
        setUnlocalizedName("arad_train");
        setTextureName("minecraft:minecart_normal");
        setMaxStackSize(1);
    }

    private long getUniqueId() {
        return lastId.incrementAndGet() + System.currentTimeMillis();
    }

    // ----------------------------------------------------------------
    // 右クリック（レールブロック上）→ 編成召喚
    // ----------------------------------------------------------------
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;

        RailMap rm0 = findRailMap(world, player, x, y, z);
        if (rm0 == null)
            return false;

        List<TrainSet> trainSets = getFormationFromItem(stack);
        if (trainSets.isEmpty())
            return false;

        spawnSets(world, rm0, trainSets, -player.rotationYaw);
        return true;
    }

    // ----------------------------------------------------------------
    // 自動運転向けAPI: FormationSpawnerから呼ばれる
    // ----------------------------------------------------------------
    public static void spawnFormation(World world, ItemStack stack, int railX, int railY, int railZ, float spawnYaw) {
        if (world.isRemote)
            return;

        RailMap rm0 = findRailMap(world, null, railX, railY, railZ);
        if (rm0 == null)
            return;

        List<TrainSet> trainSets = getFormationFromItem(stack);
        if (trainSets.isEmpty())
            return;

        spawnSets(world, rm0, trainSets, spawnYaw);
    }

    // ----------------------------------------------------------------
    // 内部: 指定RailMap上に編成を生成
    // ----------------------------------------------------------------
    private static void spawnSets(World world, RailMap rm0, List<TrainSet> trainSets, float referenceYaw) {
        int startIndex = rm0.getNearlestPoint(SEARCH_SPLIT,
                rm0.getRailPos(POS_SPLIT, POS_SPLIT / 2)[1],
                rm0.getRailPos(POS_SPLIT, POS_SPLIT / 2)[0]);

        float railYawAtStart = NGTMath.wrapAngle(rm0.getRailYaw(SEARCH_SPLIT, startIndex));
        float fixedYaw = EntityBogie.fixBogieYaw(referenceYaw, railYawAtStart);
        boolean isReverse = Math.abs(NGTMath.wrapAngle(fixedYaw - railYawAtStart)) > 90.0F;
        double dirMul = isReverse ? -1.0D : 1.0D;
        double startDist = rm0.getLength() * ((double) startIndex / SEARCH_SPLIT);

        long formationId = lastId.incrementAndGet() + System.currentTimeMillis();
        Formation formation = new Formation(formationId, trainSets.size());

        float prevYaw = fixedYaw;

        for (int i = 0; i < trainSets.size(); i++) {
            TrainSet set = trainSets.get(i);
            double targetDist = startDist + set.posZ * dirMul;
            PosRotation pr = sampleRail(rm0, targetDist, prevYaw);
            prevYaw = pr.yaw;

            EntityTrainBase train = createTrainEntity(world, set.modelName);
            int entryDir = set.dir;
            float finalYaw = pr.yaw + (entryDir == 1 ? 180.0F : 0.0F);

            train.setPositionAndRotation(pr.posX, pr.posY, pr.posZ, finalYaw, pr.pitch);
            train.rotationRoll = pr.roll;
            train.prevRotationRoll = pr.roll;

            // 1.7.10: spawnTrain がbogie生成・世界スポーンを行う
            train.spawnTrain(world);

            // 状態をDataWatcherに直接書き込む（isControlCar()制約を回避）
            train.setTrainStateData_NoSync(TrainState.TrainStateType.State_Notch.id, (byte) -8);
            train.setTrainStateData_NoSync(TrainState.TrainStateType.State_TrainDir.id, (byte) entryDir);
            train.setTrainStateData_NoSync(TrainState.TrainStateType.State_ChunkLoader.id, (byte) 1);
            train.setSpeed_NoSync(0.0F);
            // train.getResourceState().setResourceName(set.modelName);

            // Formation に登録（内部でsendPacket含む）
            formation.setTrain(train, i, entryDir);
        }
    }

    // ----------------------------------------------------------------
    // レール座標解決
    // ----------------------------------------------------------------
    private static PosRotation sampleRail(RailMap rm, double dist, float refYaw) {
        double len = rm.getLength();
        if (len <= 0.0D) {
            double[] p = rm.getRailPos(1, 0);
            return new PosRotation(refYaw, 0f, 0f, p[1], rm.getRailHeight(1, 0), p[0]);
        }
        double ratio = MathHelper.clamp_double(dist / len, 0.0D, 1.0D);
        int index = MathHelper.clamp_int((int) (ratio * POS_SPLIT), 0, POS_SPLIT);

        float railYaw = NGTMath.wrapAngle(rm.getRailYaw(POS_SPLIT, index));
        float yaw = EntityBogie.fixBogieYaw(refYaw, railYaw);
        float pitch = EntityBogie.fixBogiePitch(rm.getRailPitch(POS_SPLIT, index), railYaw, yaw);
        float roll = rm.getRailRoll(POS_SPLIT, index);
        double[] posZX = rm.getRailPos(POS_SPLIT, index);
        return new PosRotation(yaw, pitch, roll, posZX[1], rm.getRailHeight(POS_SPLIT, index), posZX[0]);
    }

    public static RailMap findRailMap(World world, EntityPlayer player, int bx, int by, int bz) {
        for (int dy = 0; dy >= -2; dy--) {
            RailMap rm = TileEntityLargeRailBase.getRailMapFromCoordinates(world, player, bx + 0.5D, by + dy,
                    bz + 0.5D);
            if (rm != null)
                return rm;
        }
        return null;
    }

    // ----------------------------------------------------------------
    // EntityTrainBase生成（モデルのサブタイプに応じて分岐）
    // ----------------------------------------------------------------
    public static EntityTrainBase createTrainEntity(World world, String modelName) {
        try {
            ModelSetVehicleBase<TrainConfig> modelSet = ModelPackManager.INSTANCE.getModelSet(TrainConfig.TYPE,
                    modelName);
            if (modelSet != null && !modelSet.isDummy()) {
                String subType = modelSet.getConfig().getSubType();
                if ("DC".equalsIgnoreCase(subType))
                    return new EntityTrain(world, "");
                if ("CC".equalsIgnoreCase(subType))
                    return new EntityFreightCar(world, "");
                if ("TC".equalsIgnoreCase(subType))
                    return new EntityTanker(world, "");
            }
        } catch (Exception ignored) {
        }
        return new EntityTrain(world, "");
    }

    // ----------------------------------------------------------------
    // NBTからTrainSetリストを復元
    // ----------------------------------------------------------------
    public static List<TrainSet> getFormationFromItem(ItemStack stack) {
        List<TrainSet> list = new ArrayList<TrainSet>();
        if (stack.hasTagCompound()) {
            NBTTagList tagList = stack.getTagCompound().getTagList("formations", 10);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                list.add(TrainSet.readFromNBT(tagList.getCompoundTagAt(i)));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 内部データクラス
    // ----------------------------------------------------------------
    public static class TrainSet {
        public String modelName;
        public double posX, posY, posZ;
        public float yaw, pitch;
        public int index, dir;

        public TrainSet(String model, int index, double x, double y, double z,
                float yaw, float pitch, int dir) {
            this.modelName = model;
            this.index = index;
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.dir = dir;
        }

        public static TrainSet readFromNBT(NBTTagCompound nbt) {
            return new TrainSet(
                    nbt.getString("model"), nbt.getInteger("index"),
                    nbt.getFloat("pos_x"), nbt.getFloat("pos_y"), nbt.getFloat("pos_z"),
                    nbt.getFloat("yaw"), nbt.getFloat("pitch"),
                    nbt.hasKey("dir") ? nbt.getInteger("dir") : 0);
        }
    }

    private static class PosRotation {
        final float yaw, pitch, roll;
        final double posX, posY, posZ;

        PosRotation(float yaw, float pitch, float roll,
                double posX, double posY, double posZ) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
    }
}
