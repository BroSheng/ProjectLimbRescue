package com.example.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingSessionTest {
    @Test
    public void readingSession_emptySensors() {
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, Limb.LEFT_ARM);
        assertEquals("Fossil Gen 5", session.getString("desc"));
        assertEquals("Left Arm", session.getString("limb"));
        assertEquals(0, session.getJSONArray("sensors").length());
    }

    @Test
    public void readingSession_oneSensor() {
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, Limb.LEFT_ARM);
        session.addSensor(new SensorReadingList(Sensor.PPG));
        assertEquals("Fossil Gen 5", session.getString("desc"));
        assertEquals("Left Arm", session.getString("limb"));
        assertEquals(1, session.getJSONArray("sensors").length());
    }

    @Test
    public void readingSession_multipleSensors() {
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, Limb.LEFT_ARM);

        for (int i = 0; i < 10; i++) {
            session.addSensor(new SensorReadingList(Sensor.PPG));
        }

        assertEquals("Fossil Gen 5", session.getString("desc"));
        assertEquals("Left Arm", session.getString("limb"));
        assertEquals(10, session.getJSONArray("sensors").length());
    }

    @Test
    public void readingSession_jsonStringEmptySensors() {
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, Limb.LEFT_ARM);
        assertEquals("{\"limb\":\"Left Arm\",\"sensors\":[],\"desc\":\"Fossil Gen 5\"}", session.toString());
    }

    @Test
    public void readingSession_jsonStringOneSensor() {
        ReadingSession session = new ReadingSession(Device.FOSSIL_GEN_5, Limb.LEFT_ARM);
        SensorReadingList sensor = new SensorReadingList(Sensor.PPG);
        sensor.addReading(new SensorReading(1000L, 0.065f));
        session.addSensor(sensor);
        assertEquals("{\"limb\":\"Left Arm\",\"sensors\":[" +
                "{\"readings\":[{\"time\":1000,\"value\":0.065}],\"desc\":\"PPG\"}" +
                "],\"desc\":\"Fossil Gen 5\"}", session.toString());
    }
}
