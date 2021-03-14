package com.example.shared;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingSessionTest {
    @Test
    public void readingSession_emptySensors() {
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, ReadingLimb.LEFT_ARM);
        assertEquals(DeviceDesc.FOSSIL_GEN_5, session.desc);
        assertEquals(ReadingLimb.LEFT_ARM, session.limb);
        assertEquals(0, session.sensors.size());
    }

    @Test
    public void readingSession_oneSensor() {
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, ReadingLimb.LEFT_ARM);
        session.addSensor(new SensorReadingList(SensorDesc.PPG));
        assertEquals(DeviceDesc.FOSSIL_GEN_5, session.desc);
        assertEquals(ReadingLimb.LEFT_ARM, session.limb);
        assertEquals(1, session.sensors.size());
    }

    @Test
    public void readingSession_multipleSensors() {
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, ReadingLimb.LEFT_ARM);

        for (int i = 0; i < 10; i++) {
            session.addSensor(new SensorReadingList(SensorDesc.PPG));
        }

        assertEquals(DeviceDesc.FOSSIL_GEN_5, session.desc);
        assertEquals(ReadingLimb.LEFT_ARM, session.limb);
        assertEquals(10, session.sensors.size());
    }

    @Test
    public void readingSession_jsonStringEmptySensors() {
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, ReadingLimb.LEFT_ARM);
        assertEquals("{\"limb\":\"LEFT_ARM\",\"sensors\":[],\"desc\":\"FOSSIL_GEN_5\"}", session.toString());
    }

    @Test
    public void readingSession_jsonStringOneSensor() {
        ReadingSession session = new ReadingSession(DeviceDesc.FOSSIL_GEN_5, ReadingLimb.LEFT_ARM);
        SensorReadingList sensor = new SensorReadingList(SensorDesc.PPG);
        JSONObject obj = new JSONObject();
        obj.put("time", 1000L);
        obj.put("value", 0.065f);
        sensor.addReading(obj);
        session.addSensor(sensor);
        assertEquals("{\"limb\":\"LEFT_ARM\",\"sensors\":[" +
                "{\"readings\":[{\"time\":1000,\"value\":0.065}],\"desc\":\"PPG\"}" +
                "],\"desc\":\"FOSSIL_GEN_5\"}", session.toString());
    }
}
