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
 * Premium course validation service - specializes the base CourseValidationService
 * Adds all strict validation rules plus database checks
 *
 * This automatically replaces CourseValidationService when present in the classpath
 */
@Specializes
@ApplicationScoped
public class PremiumCourseValidationService extends CourseValidationService {

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}$");

    @Inject
    private CourseService courseService;

    @Override
    public List<String> validateCourse(Course course) {
        List<String> errors = new ArrayList<>();

        // Basic validations from parent
        errors.addAll(super.validateCourse(course));

        // Strict validations (from StrictCourseValidationService)
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

        // Premium validations - check for duplicate course codes
        if (course.getCourseCode() != null && !course.getCourseCode().trim().isEmpty()) {
            try {
                List<Course> existingCourses = courseService.getAllCoursesJpa();
                boolean isDuplicate = existingCourses.stream()
                        .anyMatch(existingCourse ->
                                !existingCourse.getId().equals(course.getId()) && // Don't check against itself when updating
                                        existingCourse.getCourseCode().equalsIgnoreCase(course.getCourseCode().trim())
                        );

                if (isDuplicate) {
                    errors.add("Course code '" + course.getCourseCode() + "' already exists in the system");
                }
            } catch (Exception e) {
                // If we can't check for duplicates, just log and continue
                System.err.println("Warning: Could not check for duplicate course codes: " + e.getMessage());
            }
        }

        // Check for unrealistic credit combinations
        if (course.getCredits() != null && course.getTitle() != null) {
            String titleLower = course.getTitle().toLowerCase();
            if ((titleLower.contains("introduction") || titleLower.contains("basic")) && course.getCredits() > 6) {
                errors.add("Introductory courses typically should not exceed 6 credits");
            }
            if ((titleLower.contains("advanced") || titleLower.contains("graduate")) && course.getCredits() < 3) {
                errors.add("Advanced courses should have at least 3 credits");
            }
        }

        return errors;
    }

    @Override
    public String getValidationType() {
        return "Premium (strict rules + database checks)";
    }
}