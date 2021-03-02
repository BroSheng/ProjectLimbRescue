package com.example.shared;

/**
 * Type of device a reading was taken from.
 */
public enum Device {
    /** The Fossil Generation 5 smart watch running WearOS. */
    FOSSIL_GEN_5,
    /** Wyze scale. */
    WYZE_SCALE;

    @Override
    public String toString() {
        switch(this) {
            case FOSSIL_GEN_5:
                return "Fossil Gen 5";
            case WYZE_SCALE:
                return "Wyze Scale";
            default:
                return "Unknown";
        }
    }
}
