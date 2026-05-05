package com.edusmart.service.impl;

import com.edusmart.model.Course;
import com.edusmart.service.CalendarService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class CalendarServiceImpl implements CalendarService {

    private static final String APPLICATION_NAME = "EduSmart";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private Calendar service;
    private final String apiKey;

    public CalendarServiceImpl() {
        this.apiKey = System.getenv("EDUSMART_GOOGLE_API_KEY");
        try {
            this.service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCourseToGoogleCalendar(Course course) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Google API Key is not set. Calendar event not created.");
            return;
        }

        Event event = new Event()
            .setSummary(course.getTitle())
            .setDescription(course.getDescription());

        // Use course created_at or default to current time
        ZonedDateTime startZDT = (course.getCreatedAt() != null) 
            ? course.getCreatedAt().atZone(ZoneId.systemDefault()) 
            : ZonedDateTime.now();
            
        // Default course duration to 2 hours
        ZonedDateTime endZDT = startZDT.plusHours(2);

        DateTime startDateTime = new DateTime(Date.from(startZDT.toInstant()));
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone(ZoneId.systemDefault().getId());
        event.setStart(start);

        DateTime endDateTime = new DateTime(Date.from(endZDT.toInstant()));
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone(ZoneId.systemDefault().getId());
        event.setEnd(end);

        try {
            // Because we are using an API key, we might only be able to read public calendars.
            // Creating an event usually requires OAuth2. But we will make the call and log any errors.
            String calendarId = "primary";
            event = service.events().insert(calendarId, event).setKey(apiKey).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());
        } catch (IOException e) {
            System.err.println("Error creating event in Google Calendar: " + e.getMessage());
            // It will likely throw 401 Unauthorized for inserts without OAuth
        }
    }

    @Override
    public void updateCourseInGoogleCalendar(Course course) {
        // To update, we'd need the Google Calendar Event ID.
        // For simplicity, we just print a log or attempt to re-add.
        System.out.println("Updating course in Google Calendar is not fully implemented without event mapping.");
    }

    @Override
    public List<Event> getEventsFromGoogleCalendar() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Google API Key is not set. Cannot fetch events.");
            return new ArrayList<>();
        }
        
        try {
            // Try fetching events from a public calendar or primary (if possible with API Key)
            // Typically "primary" with API key fails unless it's a public calendar ID.
            // Using a dummy public calendar ID or just 'primary' for demo purposes.
            String calendarId = "en.french#holiday@group.v.calendar.google.com"; // example public calendar
            Events events = service.events().list(calendarId)
                .setKey(apiKey)
                .setMaxResults(10)
                .setTimeMin(new DateTime(System.currentTimeMillis()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
            return events.getItems();
        } catch (IOException e) {
            System.err.println("Error fetching events from Google Calendar: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
