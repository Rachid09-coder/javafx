package com.edusmart.service;

import com.edusmart.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    boolean createCourse(Course course);
    List<Course> getAllCourses();
    Optional<Course> getCourseById(int id);
    boolean updateCourse(Course course);
    boolean deleteCourse(int id);
}
