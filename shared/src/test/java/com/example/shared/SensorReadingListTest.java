package com.example.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorReadingListTest {
    @Test
    public void sensorReadingList_emptyReadings() {
        SensorReadingList list = new SensorReadingList(Sensor.PPG);
        assertEquals("PPG", list.getString("desc"));
        assertEquals(0, list.getJSONArray("readings").length());
    }

    @Test
    public void sensorReadingList_oneReading() {
        SensorReadingList list = new SensorReadingList(Sensor.PPG);
        list.addReading(new SensorReading(1000L, 0.065f));

        assertEquals("PPG", list.getString("desc"));
        assertEquals(1, list.getJSONArray("readings").length());
    }

    @Test
    public void sensorReadingList_multipleReadings() {
        SensorReadingList list = new SensorReadingList(Sensor.BIOIMPEDANCE);

        for (int i = 0; i < 10; i++) {
            list.addReading(new SensorReading(i * 2000L, 0.1f * i));
        }

        assertEquals(Sensor.BIOIMPEDANCE.toString(), list.getString("desc"));
        assertEquals(10, list.getJSONArray("readings").length());
    }
}
