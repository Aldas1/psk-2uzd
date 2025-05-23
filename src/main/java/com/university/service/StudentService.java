package com.university.service;

import com.university.dao.jpa.StudentJpaDao;
import com.university.entity.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class StudentService {

    @Inject
    private StudentJpaDao studentJpaDao;

    // JPA methods
    public List<Student> getAllStudentsJpa() {
        return studentJpaDao.getAllStudents();
    }

    public Student getStudentByIdJpa(Long id) {
        return studentJpaDao.getStudentById(id);
    }

    @Transactional
    public void saveStudentJpa(Student student) {
        studentJpaDao.saveStudent(student);
    }

    @Transactional
    public void deleteStudentJpa(Long id) {
        studentJpaDao.deleteStudent(id);
    }

    @Transactional
    public void enrollStudentInCourseJpa(Long studentId, Long courseId) {
        studentJpaDao.enrollStudentInCourse(studentId, courseId);
    }

    @Transactional
    public void removeStudentFromCourseJpa(Long studentId, Long courseId) {
        studentJpaDao.removeStudentFromCourse(studentId, courseId);
    }

    public List<Student> getStudentsByCourseIdJpa(Long courseId) {
        return studentJpaDao.getStudentsByCourseId(courseId);
    }
}
