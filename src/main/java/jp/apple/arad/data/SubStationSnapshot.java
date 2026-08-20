package jp.apple.arad.data;

public final class SubStationSnapshot {
    public final String id;
    public final String parentStationId;
    public final float x;
    public final float z;
    public final int dim;
    public final String mode;
    public final boolean turnback;
    public final int blockX;
    public final int blockY;
    public final int blockZ;
    public final boolean doorLeft;
    public final boolean doorRight;

    public SubStationSnapshot(String id, String parentStationId, float x, float z, int dim, String mode,
            boolean turnback, int blockX, int blockY, int blockZ, boolean doorLeft, boolean doorRight) {
        this.id = id;
        this.parentStationId = parentStationId;
        this.x = x;
        this.z = z;
        this.dim = dim;
        this.mode = mode;
        this.turnback = turnback;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.doorLeft = doorLeft;
        this.doorRight = doorRight;
    }

    public byte getDoorData() {
        if (doorLeft && doorRight)
            return 3;
        if (doorRight)
            return 1;
        if (doorLeft)
            return 2;
        return 0;
    }
}