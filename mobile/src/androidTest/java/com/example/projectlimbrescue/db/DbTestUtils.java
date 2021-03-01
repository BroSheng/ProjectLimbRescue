package com.example.projectlimbrescue.db;

/*
Class with useful functions for testing the application database.
 */

import com.example.projectlimbrescue.db.device.Device;
import com.example.projectlimbrescue.db.reading.Reading;
import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.session.Session;

import static org.junit.Assert.assertEquals;

public class DbTestUtils {
    public static void assertDeviceEquals(Device expected, Device actual) {
        assertEquals(expected.deviceId, actual.deviceId);
        assertEquals(expected.desc, actual.desc);
    }

    public static void assertReadingEquals(Reading expected, Reading actual) {
        assertEquals(expected.deviceId, actual.deviceId);
        assertEquals(expected.readingId, actual.readingId);
        assertEquals(expected.sensorId, actual.sensorId);
        assertEquals(expected.sessionId, actual.sessionId);
        assertEquals(expected.time, actual.time);
        assertEquals(expected.value, actual.value, 0.01f);
    }

    public static void assertSensorEquals(Sensor expected, Sensor actual) {
        assertEquals(expected.sensorId, actual.sensorId);
        assertEquals(expected.desc, actual.desc);
    }

    public static void assertSessionEquals(Session expected, Session actual) {
        assertEquals(expected.sessionId, actual.sessionId);
        assertEquals(expected.startTime, actual.startTime);
        assertEquals(expected.endTime, actual.endTime);
    }
}
