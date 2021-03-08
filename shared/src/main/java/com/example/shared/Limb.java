package com.example.shared;

/** The limb a reading was taken from. */
public enum Limb {
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_LEG,
    RIGHT_LEG;

    @Override
    public String toString() {
        switch(this) {
            case LEFT_ARM:
                return "Left Arm";
            case RIGHT_ARM:
                return "Right Arm";
            case LEFT_LEG:
                return "Left Leg";
            case RIGHT_LEG:
                return "Right Leg";
            default:
                return "Unknown";
        }
    }
}
