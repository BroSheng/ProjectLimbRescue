package com.example.projectlimbrescue.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.projectlimbrescue.db.sensor.Sensor;
import com.example.projectlimbrescue.db.sensor.SensorDao;
import com.example.projectlimbrescue.db.sensor.SensorDesc;
import com.example.projectlimbrescue.db.session.Session;
import com.example.projectlimbrescue.db.session.SessionDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
Test class for SessionDao.
TODO: add tests for SessionWithDevices, SessionWithReadings, SessionWithSensors
 */

@RunWith(AndroidJUnit4.class)
public class SessionDaoTest {
    private SessionDao sessionDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        sessionDao = db.sessionDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetSession() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        List<Session> sessions = sessionDao.getSessions();

        assertEquals(sessions.get(0).sessionId, session.sessionId);
        assertEquals(sessions.get(0).startTime, session.startTime);
        assertEquals(sessions.get(0).endTime, session.endTime);
    }

    @Test
    public void insertAndGetSessionById() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        List<Session> sessions = sessionDao.getSessionsByIds(new int[]{123});

        assertEquals(sessions.get(0).sessionId, session.sessionId);
        assertEquals(sessions.get(0).startTime, session.startTime);
        assertEquals(sessions.get(0).endTime, session.endTime);
    }

    @Test
    public void insertAndGetMultipleSessionsById() throws Exception {
        Session session1 = new Session();
        session1.sessionId = 123;
        session1.startTime = new Timestamp(1000);
        session1.endTime = new Timestamp(2000);

        Session session2 = new Session();
        session2.sessionId = 456;
        session2.startTime = new Timestamp(1000);
        session2.endTime = new Timestamp(2000);

        sessionDao.insert(session1, session2);
        List<Session> sessions = sessionDao.getSessionsByIds(new int[]{123, 456});

        assertEquals(sessions.get(0).sessionId, session1.sessionId);
        assertEquals(sessions.get(0).startTime, session1.startTime);
        assertEquals(sessions.get(0).endTime, session1.endTime);
        assertEquals(sessions.get(1).sessionId, session2.sessionId);
        assertEquals(sessions.get(1).startTime, session2.startTime);
        assertEquals(sessions.get(1).endTime, session2.endTime);
    }

    @Test
    public void insertAndDeleteSession() throws Exception {
        Session session = new Session();
        session.sessionId = 123;
        session.startTime = new Timestamp(1000);
        session.endTime = new Timestamp(2000);

        sessionDao.insert(session);
        sessionDao.delete(session);

        List<Session> sessions = sessionDao.getSessions();

        assertEquals(sessions.size(), 0);
    }
}
