package com.edusmart.controller.teacher;

import com.edusmart.model.Course;
import com.edusmart.service.CourseService;
import com.edusmart.service.impl.CourseServiceImpl;
import com.edusmart.dao.jdbc.JdbcCourseDao;
import com.edusmart.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class TeacherCalendarController implements Initializable {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Label selectedDateLabel;
    @FXML private VBox eventsListPane;
    @FXML private VBox emptyEventsPane;

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private List<Course> allCourses = new ArrayList<>();
    private final CourseService courseService = new CourseServiceImpl(new JdbcCourseDao());

    private static final String[] ROW_HEIGHTS = {"80", "80", "80", "80", "80", "80"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        Platform.runLater(() -> {
            allCourses = courseService.getAllCourses();
            renderCalendar();
            showEventsForDate(selectedDate);
        });
    }

    // ── Build the monthly grid ───────────────────────────────────
    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        monthYearLabel.setText(currentMonth.format(fmt).substring(0, 1).toUpperCase()
                + currentMonth.format(fmt).substring(1));

        LocalDate firstDay = currentMonth.atDay(1);
        int startDow = firstDay.getDayOfWeek().getValue() % 7; // Sun=0
        int daysInMonth = currentMonth.lengthOfMonth();

        // Add 6 rows max
        for (int r = 0; r < 6; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(78);
            rc.setPrefHeight(78);
            calendarGrid.getRowConstraints().add(rc);
        }

        // Build a map: date -> courses
        Map<LocalDate, List<Course>> coursesByDate = buildCourseMap();

        int day = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int cellIndex = row * 7 + col;
                if (cellIndex < startDow || day > daysInMonth) {
                    // Empty cell
                    VBox empty = new VBox();
                    empty.setStyle("-fx-background-color: transparent;");
                    calendarGrid.add(empty, col, row);
                    continue;
                }

                LocalDate date = currentMonth.atDay(day);
                List<Course> dayCourses = coursesByDate.getOrDefault(date, Collections.emptyList());

                VBox cell = buildDayCell(date, dayCourses);
                calendarGrid.add(cell, col, row);
                day++;
            }
        }
    }

    private VBox buildDayCell(LocalDate date, List<Course> courses) {
        VBox cell = new VBox(4);
        cell.setPadding(new Insets(6, 6, 4, 6));
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setPrefHeight(78);

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);

        // Style the cell
        String bg = isSelected ? "#EEF2FF" : "transparent";
        String border = isSelected ? "rgba(79,70,229,0.4)" : "#E2E8F0";
        cell.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1; -fx-cursor: hand;",
                bg, border));

        // Day number label
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        if (isToday) {
            dayNum.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white; -fx-background-color: #4F46E5; " +
                    "-fx-background-radius: 20; -fx-min-width: 24; -fx-min-height: 24; " +
                    "-fx-alignment: center; -fx-padding: 0 4;");
        } else if (isSelected) {
            dayNum.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        } else {
            dayNum.setStyle("-fx-font-size: 12px; -fx-font-weight: normal; -fx-text-fill: #0F172A;");
        }
        cell.getChildren().add(dayNum);

        // Show up to 2 course pills
        for (int i = 0; i < Math.min(courses.size(), 2); i++) {
            Course c = courses.get(i);
            Label pill = new Label(c.getTitle().length() > 14 ? c.getTitle().substring(0, 12) + "…" : c.getTitle());
            String pillColor = getPillColor(c.getStatusValue());
            pill.setStyle("-fx-font-size: 10px; -fx-text-fill: white; " +
                    "-fx-background-color: " + pillColor + "; " +
                    "-fx-background-radius: 4; -fx-padding: 1 5; -fx-max-width: infinity;");
            pill.setMaxWidth(Double.MAX_VALUE);
            cell.getChildren().add(pill);
        }

        // "+N more" if overflow
        if (courses.size() > 2) {
            Label more = new Label("+" + (courses.size() - 2) + " autres");
            more.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748B;");
            cell.getChildren().add(more);
        }

        // Click handler
        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            renderCalendar();
            showEventsForDate(date);
        });

        return cell;
    }

    private String getPillColor(String status) {
        if (status == null) return "#6366F1";
        switch (status.toUpperCase()) {
            case "ACTIVE": return "#10B981";
            case "DRAFT":  return "#F59E0B";
            default:       return "#94A3B8";
        }
    }

    // ── Right panel: events for selected day ─────────────────────
    private void showEventsForDate(LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH);
        String label = fmt.format(date);
        selectedDateLabel.setText(label.substring(0, 1).toUpperCase() + label.substring(1));

        Map<LocalDate, List<Course>> map = buildCourseMap();
        List<Course> courses = map.getOrDefault(date, Collections.emptyList());

        eventsListPane.getChildren().clear();

        if (courses.isEmpty()) {
            eventsListPane.setVisible(false);
            emptyEventsPane.setVisible(true);
            emptyEventsPane.setManaged(true);
            eventsListPane.setManaged(false);
        } else {
            eventsListPane.setVisible(true);
            eventsListPane.setManaged(true);
            emptyEventsPane.setVisible(false);
            emptyEventsPane.setManaged(false);

            for (Course c : courses) {
                VBox card = buildEventCard(c);
                eventsListPane.getChildren().add(card);
            }
        }
    }

    private VBox buildEventCard(Course course) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12, 14, 12, 14));
        String accent = getPillColor(course.getStatusValue());
        card.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: " + accent + "; " +
                "-fx-border-width: 0 0 0 4; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0, 0, 2);");

        Label title = new Label("📚  " + course.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0F172A; -fx-wrap-text: true;");
        title.setWrapText(true);

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);

        if (course.getCreatedAt() != null) {
            LocalTime t = course.getCreatedAt().toLocalTime();
            Label time = new Label("🕐  " + t.format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " – " + t.plusHours(2).format(DateTimeFormatter.ofPattern("HH:mm")));
            time.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            meta.getChildren().add(time);
        }

        String statusText = course.getStatusValue() != null ? course.getStatusValue() : "Brouillon";
        Label badge = new Label(statusText.toUpperCase());
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: " + accent + "; " +
                "-fx-background-radius: 4; -fx-padding: 2 7;");
        meta.getChildren().add(badge);

        if (course.getPrice() > 0) {
            Label price = new Label(String.format("%.0f €", course.getPrice()));
            price.setStyle("-fx-font-size: 11px; -fx-text-fill: #10B981; -fx-font-weight: bold;");
            meta.getChildren().add(price);
        }

        card.getChildren().addAll(title, meta);
        return card;
    }

    // ── Utility ──────────────────────────────────────────────────
    private Map<LocalDate, List<Course>> buildCourseMap() {
        Map<LocalDate, List<Course>> map = new HashMap<>();
        for (Course c : allCourses) {
            LocalDate d = (c.getCreatedAt() != null)
                    ? c.getCreatedAt().toLocalDate()
                    : LocalDate.now();
            map.computeIfAbsent(d, k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    // ── Navigation ───────────────────────────────────────────────
    @FXML private void handlePrevMonth(ActionEvent e) {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
        showEventsForDate(selectedDate);
    }

    @FXML private void handleNextMonth(ActionEvent e) {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
        showEventsForDate(selectedDate);
    }

    @FXML private void handleToday(ActionEvent e) {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        renderCalendar();
        showEventsForDate(selectedDate);
    }

    // ── Sidebar Navigation ────────────────────────────────────────
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
    @FXML private void handleCalendar(ActionEvent event) { /* already here */ }
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
