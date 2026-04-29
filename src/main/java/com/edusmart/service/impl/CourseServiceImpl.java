package com.edusmart.service.impl;

import com.edusmart.dao.CourseDao;
import com.edusmart.model.Course;
import com.edusmart.service.CourseService;

import java.util.List;
import java.util.Optional;

public class CourseServiceImpl implements CourseService {

    private final CourseDao courseDao;
    private com.edusmart.service.EmailService emailService;
    private com.edusmart.service.SubscriptionService subscriptionService;
    private com.edusmart.service.CalendarService calendarService;

    public CourseServiceImpl(CourseDao courseDao) {
        this.courseDao = courseDao;
        this.emailService = new com.edusmart.service.impl.EmailServiceImpl();
        this.subscriptionService = new com.edusmart.service.impl.SubscriptionServiceImpl(new com.edusmart.dao.jdbc.JdbcSubscriberDao());
        this.calendarService = new com.edusmart.service.impl.CalendarServiceImpl();
    }

    @Override
    public boolean createCourse(Course course) {
        boolean success = courseDao.create(course);
        if (success) {
            calendarService.addCourseToGoogleCalendar(course);
        }
        return success;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    @Override
    public Optional<Course> getCourseById(int id) {
        return courseDao.findById(id);
    }

    @Override
    public boolean updateCourse(Course course) {
        Optional<Course> oldCourseOpt = courseDao.findById(course.getId());
        boolean success = courseDao.update(course);
        if (success) {
            calendarService.updateCourseInGoogleCalendar(course);
            if (oldCourseOpt.isPresent()) {
                double oldPrice = oldCourseOpt.get().getPrice();
                if (course.getPrice() < oldPrice) {
                    List<com.edusmart.model.Subscriber> subs = subscriptionService.getAllSubscribers();
                    if (!subs.isEmpty()) {
                        emailService.sendPriceDropNotification(course, oldPrice, subs);
                    }
                }
            }
        }
        return success;
    }

    @Override
    public boolean deleteCourse(int id) {
        return courseDao.delete(id);
    }
}
