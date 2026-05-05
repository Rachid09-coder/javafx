package com.edusmart.service;

import com.edusmart.model.CalendarEvent;
import com.edusmart.model.Course;
import java.util.List;

public interface CalendarService {
    void addCourseToGoogleCalendar(Course course);
    void updateCourseInGoogleCalendar(Course course);
    List<CalendarEvent> getEventsFromGoogleCalendar();
}
