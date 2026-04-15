package com.edusmart.service.impl;

import com.edusmart.dao.ExamDao;
import com.edusmart.model.Exam;
import com.edusmart.service.ExamService;

import java.util.List;
import java.util.Optional;

public class ExamServiceImpl implements ExamService {

    private final ExamDao examDao;

    public ExamServiceImpl(ExamDao examDao) {
        this.examDao = examDao;
    }

    @Override
    public boolean createExam(Exam exam) {
        return examDao.create(exam);
    }

    @Override
    public List<Exam> getAllExams() {
        return examDao.findAll();
    }

    @Override
    public Optional<Exam> getExamById(int id) {
        return examDao.findById(id);
    }

    @Override
    public boolean updateExam(Exam exam) {
        return examDao.update(exam);
    }

    @Override
    public boolean deleteExam(int id) {
        return examDao.delete(id);
    }
}
