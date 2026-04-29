package com.edusmart.controller.teacher;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.edusmart.model.Course;
import com.edusmart.service.CalendarService;
import com.edusmart.service.CourseService;
import com.edusmart.service.impl.CalendarServiceImpl;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.util.DateTime;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherCalendarController implements Initializable {

    @FXML private BorderPane calendarPane;

    private CalendarView calendarView;
    private CourseService courseService;
    private CalendarService googleCalendarService;

    public TeacherCalendarController() {
        this.courseService = new CourseServiceImpl(new JdbcCourseDao());
        this.googleCalendarService = new CalendarServiceImpl();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        calendarView = new CalendarView();

        // Hide unwanted CalendarFX UI controls to make it cleaner
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSearchResultsTray(false);
        calendarView.setShowSourceTrayButton(true);

        Calendar activeCalendar = new Calendar("Cours Actifs");
        activeCalendar.setStyle(Calendar.Style.STYLE3); // Green

        Calendar draftCalendar = new Calendar("Brouillons");
        draftCalendar.setStyle(Calendar.Style.STYLE4); // Orange

        Calendar inactiveCalendar = new Calendar("Inactifs/Archivés");
        inactiveCalendar.setStyle(Calendar.Style.STYLE7); // Gray

        CalendarSource myCalendarSource = new CalendarSource("Statut des Cours");
        myCalendarSource.getCalendars().addAll(activeCalendar, draftCalendar, inactiveCalendar);
        
        calendarView.getCalendarSources().setAll(myCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        // Event click handler
        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            showCourseDetails(entry);
            return null;
        });

        calendarPane.setCenter(calendarView);

        // Load data in background to prevent UI freeze
        Platform.runLater(() -> {
            loadLocalCourses(activeCalendar, draftCalendar, inactiveCalendar);
            // Optionally load from Google Calendar if wanted
            // loadGoogleCalendarEvents(activeCalendar);
        });
    }

    private void loadLocalCourses(Calendar activeCalendar, Calendar draftCalendar, Calendar inactiveCalendar) {
        List<Course> courses = courseService.getAllCourses();
        for (Course course : courses) {
            Entry<Course> entry = new Entry<>(course.getTitle());
            entry.setUserObject(course);
            
            if (course.getPrice() > 0) {
                entry.setLocation(course.getPrice() + " €");
            } else {
                entry.setLocation("Gratuit");
            }
            
            ZonedDateTime start = (course.getCreatedAt() != null)
                ? course.getCreatedAt().atZone(ZoneId.systemDefault())
                : ZonedDateTime.now().plusDays((long)(Math.random() * 10 - 5));
                
            entry.changeStartDate(start.toLocalDate());
            entry.changeStartTime(start.toLocalTime());
            entry.changeEndDate(start.toLocalDate());
            entry.changeEndTime(start.toLocalTime().plusHours(2));
            
            String st = course.getStatusValue() != null ? course.getStatusValue().toUpperCase() : "DRAFT";
            switch (st) {
                case "ACTIVE":
                    activeCalendar.addEntry(entry);
                    break;
                case "DRAFT":
                    draftCalendar.addEntry(entry);
                    break;
                default:
                    inactiveCalendar.addEntry(entry);
                    break;
            }
        }
    }

    private void loadGoogleCalendarEvents(Calendar calendar) {
        List<Event> googleEvents = googleCalendarService.getEventsFromGoogleCalendar();
        for (Event event : googleEvents) {
            Entry<Event> entry = new Entry<>(event.getSummary());
            entry.setUserObject(event);
            
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day event.
                start = event.getStart().getDate();
                LocalDate date = LocalDate.parse(start.toStringRfc3339());
                entry.changeStartDate(date);
                entry.changeEndDate(date);
                entry.setFullDay(true);
            } else {
                ZonedDateTime zdtStart = ZonedDateTime.parse(start.toStringRfc3339());
                entry.changeStartDate(zdtStart.toLocalDate());
                entry.changeStartTime(zdtStart.toLocalTime());
                
                DateTime end = event.getEnd().getDateTime();
                if (end != null) {
                    ZonedDateTime zdtEnd = ZonedDateTime.parse(end.toStringRfc3339());
                    entry.changeEndDate(zdtEnd.toLocalDate());
                    entry.changeEndTime(zdtEnd.toLocalTime());
                }
            }
            
            calendar.addEntry(entry);
        }
    }

    private void showCourseDetails(Entry<?> entry) {
        Object userObject = entry.getUserObject();
        String title = entry.getTitle();
        String content = "Date: " + entry.getStartDate() + " " + entry.getStartTime() + "\n";

        if (userObject instanceof Course) {
            Course course = (Course) userObject;
            content += "Prix: " + course.getPrice() + " €\n";
            content += "Description: " + (course.getDescription() != null ? course.getDescription() : "N/A");
        } else if (userObject instanceof Event) {
            Event event = (Event) userObject;
            content += "Description: " + (event.getDescription() != null ? event.getDescription() : "N/A");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle("Détails du Cours");
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    @FXML private void handleDashboard(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_DASHBOARD);
    }
    @FXML private void handleManageCourses(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_COURSES);
    }
    @FXML private void handleManageModules(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_MODULES);
    }
    @FXML private void handleManageExams(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_MANAGE_EXAMS);
    }
    @FXML private void handleCalendar(ActionEvent event) {
        // Already here
    }
    @FXML private void handleShopManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_SHOP_MANAGEMENT);
    }
    @FXML private void handleBulletins(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_BULLETINS);
    }
    @FXML private void handleCertifications(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_CERTIFICATIONS);
    }
    @FXML private void handleAnalysisAI(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_ANALYSIS_AI);
    }
    @FXML private void handleStudentManagement(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.TEACHER_STUDENT_MANAGEMENT);
    }
    @FXML private void handleLogout(ActionEvent event) {
        SceneManager.getInstance().navigateTo(SceneManager.Scene.LOGIN);
    }
}
