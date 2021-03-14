package com.example.shared;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorReadingListTest {
    @Test
    public void sensorReadingList_emptyReadings() {
        SensorReadingList list = new SensorReadingList(SensorDesc.PPG);
        assertEquals(SensorDesc.PPG, list.desc);
        assertEquals(0, list.readings.size());
    }

    @Test
    public void sensorReadingList_oneReading() {
        SensorReadingList list = new SensorReadingList(SensorDesc.PPG);
        JSONObject obj = new JSONObject();
        obj.put("time", 1000L);
        obj.put("value", 0.065f);
        list.addReading(obj);

        assertEquals(SensorDesc.PPG, list.desc);
        assertEquals(1, list.readings.size());
    }

    @Test
    public void sensorReadingList_multipleReadings() {
        SensorReadingList list = new SensorReadingList(SensorDesc.PPG);

        for (int i = 0; i < 10; i++) {
            JSONObject obj = new JSONObject();
            obj.put("time", i * 2000L);
            obj.put("value", i * 0.1f);
            list.addReading(obj);
        }

        assertEquals(SensorDesc.PPG, list.desc);
        assertEquals(10, list.readings.size());
    }
}
