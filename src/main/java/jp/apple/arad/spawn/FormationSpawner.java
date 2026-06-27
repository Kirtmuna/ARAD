package jp.apple.arad.spawn;

import jp.apple.arad.station.TileEntityStation;
import jp.apple.artpe.item.ItemArtpeTrain;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FormationSpawner {

    private static final int RAIL_SEARCH_ABOVE_MIN = 1;
    private static final int RAIL_SEARCH_ABOVE_MAX = 3;

    private FormationSpawner() {
    }

    public static long spawnAt(World world,
            TileEntityStation firstStation,
            TileEntityStation secondStation,
            ItemStack formationItem) {
        if (world.isRemote || formationItem.isEmpty())
            return 0L;
        if (!(formationItem.getItem() instanceof ItemArtpeTrain))
            return 0L;

        BlockPos stationPos = firstStation.getPos();

        RailMap rail = findRailAbove(world, stationPos);
        if (rail == null) {
            jp.apple.arad.AradCore.LOGGER.warn(
                    "[Arad] 駅 '{}' の上方にレールが見つかりません", firstStation.getStationName());
            return 0L;
        }

        BlockPos railPos = findRailBlockPos(world, stationPos);
        if (railPos == null)
            railPos = stationPos.up(2);

        float spawnYaw = calcSpawnYaw(rail, stationPos, firstStation, secondStation);
        if (firstStation.isSpawnReversed()) {
            spawnYaw = NGTMath.wrapAngle(spawnYaw + 180.0f);
        }

        ItemArtpeTrain.spawnFormation(world, formationItem, railPos, spawnYaw);

        long newId = 0L;
        for (Long fid : FormationManager.getInstance().getFormations().keySet()) {
            if (fid > newId)
                newId = fid;
        }

        jp.apple.arad.AradCore.LOGGER.info(
                "[Arad] 編成召喚: 駅={} yaw={} formationId={}",
                firstStation.getStationName(), spawnYaw, newId);

        return newId;
    }

    private static RailMap findRailAbove(World world, BlockPos stationPos) {
        for (int dy = RAIL_SEARCH_ABOVE_MIN; dy <= RAIL_SEARCH_ABOVE_MAX; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    double cx = stationPos.getX() + dx + 0.5;
                    double cy = stationPos.getY() + dy;
                    double cz = stationPos.getZ() + dz + 0.5;
                    RailMap rm = TileEntityLargeRailBase.getRailMapFromCoordinates(
                            world, null, cx, cy, cz);
                    if (rm != null)
                        return rm;
                }
            }
        }
        return null;
    }

    private static BlockPos findRailBlockPos(World world, BlockPos stationPos) {
        for (int dy = RAIL_SEARCH_ABOVE_MIN; dy <= RAIL_SEARCH_ABOVE_MAX; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos candidate = stationPos.add(dx, dy, dz);
                    if (world.getTileEntity(candidate) instanceof jp.ngt.rtm.rail.TileEntityLargeRailBase) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static float calcSpawnYaw(RailMap rail, BlockPos stationPos,
                                      TileEntityStation first, TileEntityStation second) {
        int split = Math.max(8, (int) (rail.getLength() * 2.0));
        split = Math.min(split, 128);

        int nearestIdx = rail.getNearlestPoint(split,
                stationPos.getX() + 0.5, stationPos.getZ() + 0.5);
        return NGTMath.wrapAngle(rail.getRailYaw(split, nearestIdx));
    }

    private static float angleDiff(float a, float b) {
        float diff = Math.abs(NGTMath.wrapAngle(a - b));
        return diff > 180f ? 360f - diff : diff;
    }
}
