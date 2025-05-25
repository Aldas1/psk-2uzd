package com.university.service;

import com.university.entity.Course;

import java.util.List;

/**
 * Interface for Course service to enable proper decoration
 */
public interface CourseService {
    List<Course> getAllCoursesJpa();
    Course getCourseByIdJpa(Long id);
    void saveCourseJpa(Course course);
    void deleteCourseJpa(Long id);
    List<Course> getCoursesByFacultyIdJpa(Long facultyId);
}