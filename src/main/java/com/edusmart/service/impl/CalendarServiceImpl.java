package com.edusmart.service.impl;

import com.edusmart.model.CalendarEvent;
import com.edusmart.model.Course;
import com.edusmart.service.CalendarService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarServiceImpl implements CalendarService {

    private static final String CALENDAR_BASE_URL = "https://www.googleapis.com/calendar/v3";
    private static final String APPLICATION_NAME = "EduSmart";

    private final HttpClient client;
    private final Gson gson;
    private final String apiKey;

    public CalendarServiceImpl() {
        this.apiKey = System.getenv("EDUSMART_GOOGLE_API_KEY");
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    @Override
    public void addCourseToGoogleCalendar(Course course) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Google API Key is not set. Calendar event not created.");
            return;
        }

        ZonedDateTime startZDT = (course.getCreatedAt() != null)
                ? course.getCreatedAt().atZone(ZoneId.systemDefault())
                : ZonedDateTime.now();
        ZonedDateTime endZDT = startZDT.plusHours(2);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        JsonObject eventBody = new JsonObject();
        eventBody.addProperty("summary", course.getTitle());
        eventBody.addProperty("description", course.getDescription() != null ? course.getDescription() : "");

        JsonObject start = new JsonObject();
        start.addProperty("dateTime", startZDT.format(fmt));
        start.addProperty("timeZone", ZoneId.systemDefault().getId());
        eventBody.add("start", start);

        JsonObject end = new JsonObject();
        end.addProperty("dateTime", endZDT.format(fmt));
        end.addProperty("timeZone", ZoneId.systemDefault().getId());
        eventBody.add("end", end);

        try {
            String url = CALENDAR_BASE_URL + "/calendars/primary/events?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(eventBody)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                String htmlLink = result.has("htmlLink") ? result.get("htmlLink").getAsString() : "(no link)";
                System.out.printf("Event created: %s%n", htmlLink);
            } else {
                System.err.println("Error creating calendar event: HTTP " + response.statusCode() + " — " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error creating event in Google Calendar: " + e.getMessage());
        }
    }

    @Override
    public void updateCourseInGoogleCalendar(Course course) {
        System.out.println("Updating course in Google Calendar is not fully implemented without event mapping.");
    }

    @Override
    public List<CalendarEvent> getEventsFromGoogleCalendar() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Google API Key is not set. Cannot fetch events.");
            return new ArrayList<>();
        }

        try {
            String calendarId = "en.french%23holiday%40group.v.calendar.google.com";
            String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String url = CALENDAR_BASE_URL + "/calendars/" + calendarId + "/events"
                    + "?key=" + apiKey
                    + "&maxResults=10"
                    + "&timeMin=" + now
                    + "&orderBy=startTime"
                    + "&singleEvents=true";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            List<CalendarEvent> events = new ArrayList<>();
            if (response.statusCode() == 200) {
                JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                JsonArray items = result.getAsJsonArray("items");
                if (items != null) {
                    for (JsonElement item : items) {
                        JsonObject obj = item.getAsJsonObject();
                        CalendarEvent event = new CalendarEvent();
                        event.setId(obj.has("id") ? obj.get("id").getAsString() : null);
                        event.setSummary(obj.has("summary") ? obj.get("summary").getAsString() : "");
                        event.setDescription(obj.has("description") ? obj.get("description").getAsString() : "");
                        if (obj.has("htmlLink")) event.setHtmlLink(obj.get("htmlLink").getAsString());
                        if (obj.has("start")) {
                            JsonObject startObj = obj.getAsJsonObject("start");
                            String dt = startObj.has("dateTime") ? startObj.get("dateTime").getAsString()
                                    : (startObj.has("date") ? startObj.get("date").getAsString() : "");
                            event.setStartDateTime(dt);
                        }
                        if (obj.has("end")) {
                            JsonObject endObj = obj.getAsJsonObject("end");
                            String dt = endObj.has("dateTime") ? endObj.get("dateTime").getAsString()
                                    : (endObj.has("date") ? endObj.get("date").getAsString() : "");
                            event.setEndDateTime(dt);
                        }
                        events.add(event);
                    }
                }
            } else {
                System.err.println("Error fetching calendar events: HTTP " + response.statusCode());
            }
            return events;
        } catch (Exception e) {
            System.err.println("Error fetching events from Google Calendar: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
