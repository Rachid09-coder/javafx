package com.edusmart.service;

import com.edusmart.model.Course;
import com.google.api.services.calendar.model.Event;
import java.util.List;

public interface CalendarService {
    void addCourseToGoogleCalendar(Course course);
    void updateCourseInGoogleCalendar(Course course);
    List<Event> getEventsFromGoogleCalendar();
}
