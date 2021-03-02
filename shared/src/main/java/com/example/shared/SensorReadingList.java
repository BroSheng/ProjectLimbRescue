package com.example.shared;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A JSON friendly representation of a grouping of readings from a sensor. Each sensor is identified
 * by the type of data is collects (i.e. "PPG", "Bioimpedance", etc.). JSON strings from sensors
 * have two fields: "desc" being the type of data as a string and "readings" being an array of
 * <code>SensorReading</code>. Reading order is not guaranteed. Timestamps should be used to order
 * them at the client side.
 *
 * <code>
 *     {
 *         "desc": "PPG",
 *         "readings": [
 *             {
 *                 "time": 10000,
 *                 "value": 0.1234
 *             },
 *             ...
 *         ]
 *     }
 * </code>
 */
public class SensorReadingList extends JSONObject {
    private final JSONArray readings;

    /**
     * Constructs a JSON object with the sensor type and readings array.
     *
     * @param type Type of sensor data.
     */
    public SensorReadingList(Sensor type) {
        this.readings = new JSONArray();
        this.put("desc", type.toString());
        this.put("readings", this.readings);
    }

    /**
     * Adds a sensor reading to the list.
     *
     * @param reading Reading to add to the list.
     */
    public void addReading(SensorReading reading) {
        this.readings.put(reading);
    }
}
