package com.university.service.validation;

import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Strict course validation service - enhanced validation rules
 * This is a CDI Alternative to CourseValidationService
 *
 * To enable this alternative, uncomment the <alternatives> section in beans.xml
 */
@Alternative
@ApplicationScoped
public class StrictCourseValidationService extends CourseValidationService {

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}$");

    @Override
    public List<String> validateCourse(Course course) {
        List<String> errors = new ArrayList<>();

        // Basic validations from parent
        errors.addAll(super.validateCourse(course));

        // Additional strict validations
        if (course.getCourseCode() != null && !course.getCourseCode().trim().isEmpty()) {
            if (!COURSE_CODE_PATTERN.matcher(course.getCourseCode().trim()).matches()) {
                errors.add("Course code must follow pattern: 2-4 letters followed by 3 digits (e.g., CS101, MATH201)");
            }
        }

        if (course.getTitle() != null) {
            if (course.getTitle().length() < 5) {
                errors.add("Course title must be at least 5 characters long");
            }
            if (course.getTitle().length() > 100) {
                errors.add("Course title cannot exceed 100 characters");
            }
        }

        if (course.getCredits() != null) {
            if (course.getCredits() < 1 || course.getCredits() > 10) {
                errors.add("Credits must be between 1 and 10");
            }
        }

        if (course.getFaculty() == null) {
            errors.add("Faculty assignment is required for all courses");
        }

        return errors;
    }

    @Override
    public String getValidationType() {
        return "Strict";
    }
}