package jp.apple.arad.substation;

public enum SubStationMode {
    STOP_POSITION_CORRECTION("停車位置補正");

    public final String label;

    SubStationMode(String label) {
        this.label = label;
    }

    public SubStationMode next() {
        SubStationMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}