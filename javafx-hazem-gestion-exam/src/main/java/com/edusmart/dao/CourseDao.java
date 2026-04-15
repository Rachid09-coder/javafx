package com.edusmart.dao;

import com.edusmart.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseDao {
    boolean create(Course course);
    List<Course> findAll();
    Optional<Course> findById(int id);
    boolean update(Course course);
    boolean delete(int id);
}
