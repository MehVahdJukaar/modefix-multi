package net.mehvahdjukaar.modelfix.moonlight_configs;

public enum ConfigType {
    COMMON, COMMON_SYNCED, CLIENT;

    public boolean isSynced() {
        return this == COMMON_SYNCED;
    }
}
