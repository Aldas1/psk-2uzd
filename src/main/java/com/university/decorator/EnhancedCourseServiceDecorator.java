package com.university.decorator;

import com.university.entity.Course;
import com.university.service.CourseService;
import com.university.service.validation.CourseValidationService;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

/**
 * Decorator that adds validation and enhanced functionality to CourseService
 * Note: Decorators must use @Dependent scope (default), not @ApplicationScoped
 */
@Decorator
public abstract class EnhancedCourseServiceDecorator implements CourseService {

    private static final Logger logger = Logger.getLogger(EnhancedCourseServiceDecorator.class.getName());

    @Inject
    @Delegate
    private CourseService delegate;

    @Inject
    private CourseValidationService validationService;

    @Override
    public void saveCourseJpa(Course course) {
        logger.info("DECORATOR: Enhanced course save initiated for course: " +
                (course.getCourseCode() != null ? course.getCourseCode() : "NEW"));

        // Perform validation before saving
        List<String> validationErrors = validationService.validateCourse(course);

        if (!validationErrors.isEmpty()) {
            logger.warning("DECORATOR: Validation failed for course " + course.getCourseCode());

            // Add individual validation error messages to JSF context if available
            if (FacesContext.getCurrentInstance() != null) {
                for (String error : validationErrors) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Failed", error));
                }
            }

            // Create detailed error message for exception
            StringBuilder errorMessage = new StringBuilder("Course validation failed: ");
            for (int i = 0; i < validationErrors.size(); i++) {
                if (i > 0) errorMessage.append("; ");
                errorMessage.append(validationErrors.get(i));
            }

            throw new IllegalArgumentException(errorMessage.toString());
        }

        // Normalize course data before saving
        if (course.getCourseCode() != null) {
            course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        }
        if (course.getTitle() != null) {
            course.setTitle(course.getTitle().trim());
        }

        logger.info("DECORATOR: Validation passed, delegating to actual service. Validation type: " +
                validationService.getValidationType());

        // Delegate to the actual service
        delegate.saveCourseJpa(course);

        logger.info("DECORATOR: Course saved successfully with enhanced validation");

        // Add success message if JSF context is available
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Enhanced Save",
                            "Course saved with " + validationService.getValidationType() + " validation"));
        }
    }

    @Override
    public List<Course> getAllCoursesJpa() {
        logger.info("DECORATOR: Enhanced course retrieval");
        List<Course> courses = delegate.getAllCoursesJpa();
        logger.info("DECORATOR: Retrieved " + courses.size() + " courses");
        return courses;
    }

    @Override
    public Course getCourseByIdJpa(Long id) {
        logger.info("DECORATOR: Enhanced course retrieval by ID: " + id);
        Course course = delegate.getCourseByIdJpa(id);
        if (course != null) {
            logger.info("DECORATOR: Found course: " + course.getCourseCode());
        } else {
            logger.warning("DECORATOR: Course not found with ID: " + id);
        }
        return course;
    }

    @Override
    public void deleteCourseJpa(Long id) {
        logger.info("DECORATOR: Enhanced course deletion for ID: " + id);

        // Check if course exists before deletion
        Course course = delegate.getCourseByIdJpa(id);
        if (course == null) {
            throw new IllegalArgumentException("Cannot delete: Course with ID " + id + " not found");
        }

        logger.info("DECORATOR: Deleting course: " + course.getCourseCode());
        delegate.deleteCourseJpa(id);
        logger.info("DECORATOR: Course deleted successfully");
    }

    @Override
    public List<Course> getCoursesByFacultyIdJpa(Long facultyId) {
        logger.info("DECORATOR: Enhanced course retrieval by faculty ID: " + facultyId);
        List<Course> courses = delegate.getCoursesByFacultyIdJpa(facultyId);
        logger.info("DECORATOR: Retrieved " + courses.size() + " courses for faculty " + facultyId);
        return courses;
    }
}