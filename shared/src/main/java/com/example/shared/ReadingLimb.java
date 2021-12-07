package com.example.shared;

/**
 * Limb that a reading was taken on.
 */
public enum ReadingLimb {
    LEFT_ARM {
        public String toString() {
            return "LEFT_ARM";
        }
    },
    RIGHT_ARM {
        public String toString() {
            return "RIGHT_ARM";
        }
    },
    LEFT_ARM_BILATERAL {
        public String toString() {
            return "LEFT_ARM_BILATERAL";
        }
    },
    RIGHT_ARM_BILATERAL {
        public String toString() {
            return "RIGHT_ARM_BILATERAL";
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
