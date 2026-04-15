package com.edusmart.service.impl;

import com.edusmart.dao.CourseDao;
import com.edusmart.model.Course;
import com.edusmart.service.CourseService;

import java.util.List;
import java.util.Optional;

public class CourseServiceImpl implements CourseService {

    private final CourseDao courseDao;

    public CourseServiceImpl(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @Override
    public boolean createCourse(Course course) {
        return courseDao.create(course);
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
        return courseDao.update(course);
    }

    @Override
    public boolean deleteCourse(int id) {
        return courseDao.delete(id);
    }
}
