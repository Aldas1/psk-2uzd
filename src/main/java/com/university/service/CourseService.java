package com.university.service;

import com.university.dao.jpa.CourseJpaDao;
import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CourseService {

    @Inject
    private CourseJpaDao courseJpaDao;

    // JPA methods
    public List<Course> getAllCoursesJpa() {
        return courseJpaDao.getAllCourses();
    }

    public Course getCourseByIdJpa(Long id) {
        return courseJpaDao.getCourseById(id);
    }

    @Transactional
    public void saveCourseJpa(Course course) {
        courseJpaDao.saveCourse(course);
    }

    @Transactional
    public void deleteCourseJpa(Long id) {
        courseJpaDao.deleteCourse(id);
    }

    public List<Course> getCoursesByFacultyIdJpa(Long facultyId) {
        return courseJpaDao.getCoursesByFacultyId(facultyId);
    }
}
