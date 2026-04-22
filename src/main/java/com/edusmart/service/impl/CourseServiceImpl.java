package com.edusmart.service.impl;

import com.edusmart.dao.CourseDao;
import com.edusmart.model.Course;
import com.edusmart.service.CourseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CourseServiceImpl implements CourseService {

    private final CourseDao courseDao;

    public CourseServiceImpl(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @Override
    public boolean createCourse(Course course) {
        validateCourse(course);
        if (course.getCreatedAt() == null) {
            course.setCreatedAt(LocalDateTime.now());
        }
        if (course.getStatusValue() == null || course.getStatusValue().isBlank()) {
            course.setStatusValue("ACTIVE");
        }
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
        validateCourse(course);
        return courseDao.update(course);
    }

    @Override
    public boolean deleteCourse(int id) {
        return courseDao.delete(id);
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateCourse(Course course) {
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du cours est obligatoire.");
        }
        if (course.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("Le titre doit contenir au moins 3 caractères.");
        }
        if (course.getPrice() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        }
        if (course.getCoefficient() != null && course.getCoefficient() <= 0) {
            throw new IllegalArgumentException("Le coefficient doit être supérieur à 0.");
        }
        String status = course.getStatusValue();
        if (status != null && !status.isBlank()) {
            try {
                Course.Status.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Statut invalide : " + status);
            }
        }
    }
}
