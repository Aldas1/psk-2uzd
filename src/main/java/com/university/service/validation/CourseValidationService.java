package com.university.service.validation;

import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CourseValidationService {

    public List<String> validateCourse(Course course) {
        List<String> errors = new ArrayList<>();

        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            errors.add("Course code is required");
        }

        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            errors.add("Course title is required");
        }

        if (course.getCredits() == null || course.getCredits() <= 0) {
            errors.add("Credits must be positive");
        }

        return errors;
    }

    public String getValidationType() {
        return "Basic";
    }
}