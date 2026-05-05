package com.edusmart.model;

/**
 * Simple model representing a Google Calendar event.
 */
public class CalendarEvent {
    private String id;
    private String summary;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String htmlLink;

    public CalendarEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }

    public String getEndDateTime() { return endDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }

    public String getHtmlLink() { return htmlLink; }
    public void setHtmlLink(String htmlLink) { this.htmlLink = htmlLink; }
}
