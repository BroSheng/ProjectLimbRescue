package com.example.projectlimbrescue;

/*
Class containing method(s) to parse a received, deserialized JsonObject representing readings from
a device and store them in the application database.

An example JSON received and stored by this class might be:

{
  "desc": "FOSSIL_GEN_5",
  "limb": "LEFT_ARM",
  "sensors": [
    {
      "desc": "PPG",
      "readings": [
        {
          "time": (some milliseconds value),
          "value": 12345.6789
        },
        (...)
      ]
    }
  ]
}

To use:
- Instantiate a Room database
- Create a db Session object, tracking its start and end times
- As device JsonObjects are deserialized, store them in a list, queue, or similar flexible-length structure
- Once all data collection is done, store the Session using SessionDao
- For each JsonObject in list/queue:
    - Call InsertJson, passing in the id of the recently-inserted session, the database, and the
    JsonObject itself
*/

import com.example.projectlimbrescue.db.*;
import com.example.projectlimbrescue.db.device.*;
import com.example.projectlimbrescue.db.reading.*;
import com.example.projectlimbrescue.db.sensor.*;
import com.example.projectlimbrescue.db.session.*;

import org.json.*;

import java.math.BigDecimal;
import java.util.List;

public class JsonToDb {
    /*
    Workflow:
    1. Store device 'limb'
    2. Check if device matching 'desc' already exists in db
        - If so, store its id
        - If not, insert it and store its id
    3. Check if a SessionWithDevices for the session id already contains a device with the device id
        - If not, create and insert a SessionReadsFromDevice relationship with session id and device id
    4. For each JsonObject in 'sensors':
        4a. Check if sensor matching 'desc' already exists in db
            - If so, store its id
            - If not, insert it and store its id
        4b. Check if a DeviceWithSensors for the device id already contains a sensor with the sensor id
            - If not, create and insert a DeviceContainsSensor relationship with the device id and sensor id
        4c. Check if a SessionWithSensors for the session id already contains a sensor with the sensor id
            - If not, create an insert a SessionMeasuresSensor relationship with the session id and sensor id
        4d. For each JsonObject in 'readings':
            4d-1. Create a Reading object with:
                - sessionId: the session id
                - deviceId: the device id
                - sensorId: the sensor id
                - time: the JsonObject's 'time'
                - value: the JsonObject's 'value'
                - limb: the limb initially stored
     */
    public static void InsertJson(JSONObject json, long sessionId, AppDatabase db) throws org.json.JSONException {
        // Set up database DAOs
        DeviceDao deviceDao = db.deviceDao();
        DeviceContainsSensorDao deviceContainsSensorDao = db.deviceContainsSensorDao();
        ReadingDao readingDao = db.readingDao();
        SensorDao sensorDao = db.sensorDao();
        SessionDao sessionDao = db.sessionDao();
        SessionMeasuresSensorDao sessionMeasuresSensorDao = db.sessionMeasuresSensorDao();
        SessionReadsFromDeviceDao sessionReadsFromDeviceDao = db.sessionReadsFromDeviceDao();

        ReadingLimb limb = ReadingLimb.valueOf(json.getString("limb"));
        DeviceDesc deviceDesc = DeviceDesc.valueOf(json.getString("desc"));

        long deviceId;
        List<Device> devicesWithDesc = deviceDao.getDevicesByDesc(deviceDesc);
        if (devicesWithDesc.size() == 0) { // no device with desc yet exists
            Device device = new Device();
            device.desc = deviceDesc;
            deviceId = deviceDao.insert(device)[0];
        } else {
            deviceId = devicesWithDesc.get(0).deviceId;
        }

        SessionWithDevices sessionWithDevices = sessionDao.getSessionsWithDevicesByIds(new long[]{sessionId}).get(0);
        boolean foundDevice = false;
        for (Device device : sessionWithDevices.devices) {
            if (device.deviceId == deviceId) {
                foundDevice = true;
                break;
            }
        }
        if (!foundDevice) {
            SessionReadsFromDevice sessionReadsFromDevice = new SessionReadsFromDevice();
            sessionReadsFromDevice.sessionId = sessionId;
            sessionReadsFromDevice.deviceId = deviceId;
            sessionReadsFromDeviceDao.insert(sessionReadsFromDevice);
        }

        JSONArray jsonSensors = json.getJSONArray("sensors");
        for (int i = 0; i < jsonSensors.length(); i++) { // JSONArray can't foreach for some reason
            JSONObject jsonSensor = jsonSensors.getJSONObject(i);
            SensorDesc sensorDesc = SensorDesc.valueOf(jsonSensor.getString("desc"));

            long sensorId;
            List<Sensor> sensorsWithDesc = sensorDao.getSensorsByDesc(sensorDesc);
            if (sensorsWithDesc.size() == 0) { // no sensor with desc yet exists
                Sensor sensor = new Sensor();
                sensor.desc = sensorDesc;
                sensorId = sensorDao.insert(sensor)[0];
            } else {
                sensorId = sensorsWithDesc.get(0).sensorId;
            }

            DeviceWithSensors deviceWithSensors = deviceDao.getDevicesWithSensorsByIds(new long[]{deviceId}).get(0);
            boolean foundSensor = false;
            for (Sensor sensor : deviceWithSensors.sensors) {
                if (sensor.sensorId == sensorId) {
                    foundSensor = true;
                    break;
                }
            }
            if (!foundSensor) {
                DeviceContainsSensor deviceContainsSensor = new DeviceContainsSensor();
                deviceContainsSensor.deviceId = deviceId;
                deviceContainsSensor.sensorId = sensorId;
                deviceContainsSensorDao.insert(deviceContainsSensor);
            }

            SessionWithSensors sessionWithSensors = sessionDao.getSessionsWithSensorsByIds(new long[]{sessionId}).get(0);
            foundSensor = false;
            for (Sensor sensor : sessionWithSensors.sensors) {
                if (sensor.sensorId == sensorId) {
                    foundSensor = true;
                    break;
                }
            }
            if (!foundSensor) {
                SessionMeasuresSensor sessionMeasuresSensor = new SessionMeasuresSensor();
                sessionMeasuresSensor.sessionId = sessionId;
                sessionMeasuresSensor.sensorId = sensorId;
                sessionMeasuresSensorDao.insert(sessionMeasuresSensor);
            }

            JSONArray jsonReadings = jsonSensor.getJSONArray("readings");
            for (int j = 0; j < jsonReadings.length(); j++) {
                JSONObject jsonReading = jsonReadings.getJSONObject(j);

                Reading reading = new Reading();
                reading.sessionId = sessionId;
                reading.deviceId = deviceId;
                reading.sensorId = sensorId;
                reading.time = jsonReading.getLong("time");
                reading.value = jsonReading.getDouble("value");
                reading.limb = limb;

                readingDao.insert(reading);
            }
        }
    }
}
