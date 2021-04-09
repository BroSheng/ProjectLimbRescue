package com.example.shared;

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

    static public ReadingLimb getEnum(String val) {
        return valueOf(val.replace(' ', '_').toUpperCase());
    }
}
