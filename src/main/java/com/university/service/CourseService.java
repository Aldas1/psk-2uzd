package com.university.service;

import com.university.entity.Course;

import java.util.List;

public interface CourseService {
    List<Course> getAllCoursesJpa();
    Course getCourseByIdJpa(Long id);
    void saveCourseJpa(Course course);
    void deleteCourseJpa(Long id);
    List<Course> getCoursesByFacultyIdJpa(Long facultyId);
}