package com.university.service;

import com.university.dao.jpa.CourseJpaDao;
import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CourseServiceImpl implements CourseService {

    @Inject
    private CourseJpaDao courseJpaDao;

    @Override
    public List<Course> getAllCoursesJpa() {
        return courseJpaDao.getAllCourses();
    }

    @Override
    public Course getCourseByIdJpa(Long id) {
        return courseJpaDao.getCourseById(id);
    }

    @Override
    @Transactional
    public void saveCourseJpa(Course course) {
        courseJpaDao.saveCourse(course);
    }

    @Override
    @Transactional
    public void deleteCourseJpa(Long id) {
        courseJpaDao.deleteCourse(id);
    }

    @Override
    public List<Course> getCoursesByFacultyIdJpa(Long facultyId) {
        return courseJpaDao.getCoursesByFacultyId(facultyId);
    }
}