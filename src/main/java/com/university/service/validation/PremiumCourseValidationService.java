package com.university.service.validation;

import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.inject.Inject;
import com.university.service.CourseService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Specializes
@ApplicationScoped
public class PremiumCourseValidationService extends CourseValidationService {

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}$");

    @Inject
    private CourseService courseService;

    @Override
    public List<String> validateCourse(Course course) {
        List<String> errors = new ArrayList<>(super.validateCourse(course));

        if (course.getCourseCode() != null && !course.getCourseCode().trim().isEmpty()) {
            try {
                List<Course> existingCourses = courseService.getAllCoursesJpa();

                for (Course existingCourse : existingCourses) {
                    if (existingCourse.getId().equals(course.getId())) {
                        continue;
                    }

                    if (existingCourse.getCourseCode().equalsIgnoreCase(course.getCourseCode().trim())) {
                        errors.add("PREMIUM: Course code '" + course.getCourseCode() + "' already exists in the database");
                        break;
                    }
                }

            } catch (Exception e) {
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