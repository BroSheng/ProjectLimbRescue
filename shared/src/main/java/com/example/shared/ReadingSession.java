package com.example.shared;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
public class ReadingSession {
    public List<SensorReadingList> sensors;
    public ReadingLimb limb;
    public DeviceDesc desc;

    public ReadingSession(DeviceDesc device, ReadingLimb limb) {
        this.sensors = new ArrayList<>();
        this.limb = limb;
        this.desc = device;
    }

    /**
     * Add a sensor's readings to the list.
     *
     * @param list Sensor reading list.
     */
    public void addSensor(SensorReadingList list) {
        sensors.add(list);
    }

    /**
     * Converts the ReadingSession to a JSON object using the above format.
     * 
     * @return JSON Object of the session.
     */
    public JSONObject toJson() {
        JSONArray sensors = new JSONArray();
        for(int i = 0; i < this.sensors.size(); i++) {
            sensors.put(this.sensors.get(i).toJson());
        }

        JSONObject obj = new JSONObject();
        obj.put("sensors", sensors);
        obj.put("desc", this.desc);
        obj.put("limb", this.limb);
        return obj;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
