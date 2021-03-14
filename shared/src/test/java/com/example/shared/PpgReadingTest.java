package com.example.shared;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PpgReadingTest {
    @Test
    public void ppgReading_checkFieldsPresent() {
        SensorReading obj = new SensorReading(1000L, 0.65f);
        assertEquals(1000L, obj.getLong("time"));
        assertEquals(0.65f, obj.getFloat("value"), 0.001f);
    }
}
