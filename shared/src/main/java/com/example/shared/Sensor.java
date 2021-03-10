package com.example.shared;

/**
 * Type of sensor used to obtain a certain piece of data.
 *
 * @see SensorReadingList
 */
public enum Sensor {
    /** Photoplethysmography sensor takes a reading using list to measure density. */
    PPG,
    /** Bioimpedance sensor measures the impedance of an electrical signal to measure densities. */
    BIOIMPEDANCE;

    @Override
    public String toString() {
        switch(this) {
            case PPG:
                return "PPG";
            case BIOIMPEDANCE:
                return "Bioimpedance";
            default:
                return "UNKNOWN";
        }
    }
}
