package jp.apple.arad.spawn;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import jp.apple.arad.item.ItemArtpeTrain;
import jp.apple.arad.station.TileEntityStation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public final class FormationSpawner {

    private static final int RAIL_SEARCH_ABOVE_MIN = 1;
    private static final int RAIL_SEARCH_ABOVE_MAX = 3;

    private FormationSpawner() {
    }

    public static long spawnAt(World world,
            TileEntityStation firstStation,
            TileEntityStation secondStation,
            ItemStack formationItem) {
        if (world.isRemote || formationItem == null)
            return 0L;
        if (!(formationItem.getItem() instanceof ItemArtpeTrain))
            return 0L;

        int sx = firstStation.xCoord;
        int sy = firstStation.yCoord;
        int sz = firstStation.zCoord;

        RailMap rail = findRailAbove(world, sx, sy, sz);
        if (rail == null) {
            jp.apple.arad.AradCore.LOGGER.warn(
                    "[Arad] 駅 '{}' の上方にレールが見つかりません", firstStation.getStationName());
            return 0L;
        }

        int[] railPos = findRailBlockPos(world, sx, sy, sz);
        if (railPos == null) {
            railPos = new int[] { sx, sy + 2, sz };
        }

        float spawnYaw = calcSpawnYaw(rail, sx, sz, firstStation, secondStation);
        if (firstStation.isSpawnReversed()) {
            spawnYaw = NGTMath.wrapAngle(spawnYaw + 180.0f);
        }

        ItemArtpeTrain.spawnFormation(world, formationItem, railPos[0], railPos[1], railPos[2], spawnYaw);

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

    private static RailMap findRailAbove(World world, int sx, int sy, int sz) {
        for (int dy = RAIL_SEARCH_ABOVE_MIN; dy <= RAIL_SEARCH_ABOVE_MAX; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    double cx = sx + dx + 0.5;
                    double cy = sy + dy;
                    double cz = sz + dz + 0.5;
                    RailMap rm = TileEntityLargeRailBase.getRailMapFromCoordinates(
                            world, null, cx, cy, cz);
                    if (rm != null)
                        return rm;
                }
            }
        }
        return null;
    }

    private static int[] findRailBlockPos(World world, int sx, int sy, int sz) {
        for (int dy = RAIL_SEARCH_ABOVE_MIN; dy <= RAIL_SEARCH_ABOVE_MAX; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int cx = sx + dx;
                    int cy = sy + dy;
                    int cz = sz + dz;
                    if (world.getTileEntity(cx, cy, cz) instanceof jp.ngt.rtm.rail.TileEntityLargeRailBase) {
                        return new int[] { cx, cy, cz };
                    }
                }
            }
        }
        return null;
    }

    private static float calcSpawnYaw(RailMap rail, int sx, int sz,
            TileEntityStation first, TileEntityStation second) {
        int split = Math.max(8, (int) (rail.getLength() * 2.0));
        split = Math.min(split, 128);

        int nearestIdx = rail.getNearlestPoint(split,
                sx + 0.5, sz + 0.5);
        return NGTMath.wrapAngle(rail.getRailYaw(split, nearestIdx));
    }

    private static float angleDiff(float a, float b) {
        float diff = Math.abs(NGTMath.wrapAngle(a - b));
        return diff > 180f ? 360f - diff : diff;
    }
}
