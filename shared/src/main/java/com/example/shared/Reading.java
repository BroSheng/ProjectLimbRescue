package com.example.shared;

public class Reading {
    public long time;
    public float channel0;
    public float channel1;

    public Reading(long time, float channel0, float channel1) {
        this.time = time;
        this.channel0 = channel0;
        this.channel1 = channel1;
    }
}
