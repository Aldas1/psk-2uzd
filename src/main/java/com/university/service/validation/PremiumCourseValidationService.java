package com.university.service.validation;

import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import com.university.service.CourseService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Premium course validation service - specializes the BASE CourseValidationService
 * This way it works regardless of whether StrictCourseValidationService is enabled or not
 */
@Specializes
@ApplicationScoped
public class PremiumCourseValidationService extends CourseValidationService {

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}$");

    @Inject
    private CourseService courseService;

    @Override
    public List<String> validateCourse(Course course) {
        // Start with basic validations from parent
        List<String> errors = new ArrayList<>(super.validateCourse(course));

        // Add premium feature: check for duplicate course codes in database
        if (course.getCourseCode() != null && !course.getCourseCode().trim().isEmpty()) {
            try {
                List<Course> existingCourses = courseService.getAllCoursesJpa();

                for (Course existingCourse : existingCourses) {
                    // Skip self when updating
                    if (existingCourse.getId().equals(course.getId())) {
                        continue;
                    }

                    // Check for duplicate course code
                    if (existingCourse.getCourseCode().equalsIgnoreCase(course.getCourseCode().trim())) {
                        errors.add("PREMIUM: Course code '" + course.getCourseCode() + "' already exists in the database");
                        break; // Found duplicate, no need to check further
                    }
                }

            } catch (Exception e) {
                // If database check fails, just log it but don't fail validation
                System.err.println("Premium validation warning: Could not check database for duplicates: " + e.getMessage());
            }
        }

        return errors;
    }

    @Override
    public String getValidationType() {
        return "Premium (all strict rules + database duplicate check)";
    }
}