package com.example.shared;

/**
 * Limb that a reading was taken on.
 */
public enum ReadingLimb {
    LEFT_ARM_SINGLE {
        public String toString() {
            return "Left Arm Single";
        }
    },
    RIGHT_ARM_SINGLE {
        public String toString() {
            return "Right Arm Single";
        }
    },
    LEFT_ARM_BOTH {
        public String toString() {
            return "Left Arm Both";
        }
    },
    RIGHT_ARM_BOTH {
        public String toString() {
            return "Right Arm Both";
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
