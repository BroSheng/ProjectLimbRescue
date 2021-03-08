package com.example.shared;

import org.json.JSONObject;

/**
 * A JSON friendly representation of a sensor reading from an Android smart watch or other health
 * devices. Each value from a watch is an analog signal and is relative. Readings are accompanied
 * by a nano second timestamp relative to the start of the session it was read in. The JSON
 * repesentation has a two fields: "time" being a nanosecond timestamp represented as a number
 * and "value" being the value from the sensor as a float.
 *
 * <code>
 *     {
 *         "time": 10000,
 *         "value": 0.253
 *     }
 * </code>
 *
 * @see ReadingSession
 */
public class SensorReading extends JSONObject {
    /** Number of nanoseconds since the reading session started. */
    public long timestamp;

    /** Relative value read from the sensor. */
    public float value;

    /**
     * Constructs a JSON object with the sensor value and relevant timestamp.
     *
     * @param timestamp A number of nanoseconds since the reading session started.
     * @param value Relative value read from the sensor.
     */
    public SensorReading(long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;

        put("time", this.timestamp);
        put("value", this.value);
    }
}
