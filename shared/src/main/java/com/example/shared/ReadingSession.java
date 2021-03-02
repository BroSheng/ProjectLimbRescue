package com.example.shared;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A JSON friendly representation of a session of readings. It has three fields: "desc" being a
 * description of the device the reading was taken from, "limb" being the limb right or left that
 * the reading was taken from, and "sensors" being an array of sensors and their readings.
 *
 * <code>
 *    {
 *       "desc": "FOSSIL_GEN_5",
 *       "limb": "LEFT_ARM",
 *       "sensors": [
 *         {
 *           "desc": "PPG",
 *           "readings": [
 *             {
 *               "time": (some milliseconds value),
 *               "value": 12345.6789
 *             },
 *             (...)
 *           ]
 *         }
 *       ]
 *     }
 * </code>
 */
public class ReadingSession extends JSONObject {
    private final JSONArray sensors;

    /**
     * Constructs the JSON object with the device name, limb, and array of sensors.
     *
     * @param device Device that the readings were taken from.
     * @param limb Limb that the readings were taken from.
     */
    public ReadingSession(Device device, Limb limb) {
        this.sensors = new JSONArray();

        put("desc", device.toString());
        put("limb", limb.toString());
        put("sensors", this.sensors);
    }

    /**
     * Add a sensor's readings to the list.
     *
     * @param list Sensor reading list.
     */
    public void addSensor(SensorReadingList list) {
        sensors.put(list);
    }
}
