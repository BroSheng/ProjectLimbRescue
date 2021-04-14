package com.example.shared;

/**
 * Limb that a reading was taken on.
 */
public enum ReadingLimb {
    LEFT_ARM {
        public String toString() {
            return "Left Arm";
        }
    },
    RIGHT_ARM {
        public String toString() {
            return "Right Arm";
        }
    };

    /**
     * Converts the toString's above back to the enum style to get the value.
     * 
     * @param val Readable string of the enum
     * @return The enum corresponding to that string.
     */
    static public ReadingLimb getEnum(String val) {
        return valueOf(val.replace(' ', '_').toUpperCase());
    }
}
