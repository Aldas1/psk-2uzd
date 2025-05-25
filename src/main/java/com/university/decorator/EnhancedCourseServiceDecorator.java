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

@Decorator
public abstract class EnhancedCourseServiceDecorator implements CourseService {
    @Inject
    @Delegate
    private CourseService delegate;

    @Inject
    private CourseValidationService validationService;

    @Override
    public void saveCourseJpa(Course course) {

        List<String> validationErrors = validationService.validateCourse(course);

        if (!validationErrors.isEmpty()) {
            if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Validation Failed",
                                "Course validation failed with " + validationService.getValidationType() + " validation:"));

                for (String error : validationErrors) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, error, ""));
                }
            }

            StringBuilder errorMessage = new StringBuilder("Course validation failed (" + validationService.getValidationType() + "): ");
            for (int i = 0; i < validationErrors.size(); i++) {
                if (i > 0) errorMessage.append("; ");
                errorMessage.append(validationErrors.get(i));
            }

            throw new IllegalArgumentException(errorMessage.toString());
        }

        if (course.getCourseCode() != null) {
            course.setCourseCode(course.getCourseCode().trim().toUpperCase());
        }
        if (course.getTitle() != null) {
            course.setTitle(course.getTitle().trim());
        }
        delegate.saveCourseJpa(course);

        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Enhanced Save",
                            "Course '" + course.getCourseCode() + "' saved successfully with " +
                                    validationService.getValidationType() + " validation"));
        }
    }

    @Override
    public List<Course> getAllCoursesJpa() {
        List<Course> courses = delegate.getAllCoursesJpa();
        return courses;
    }

    @Override
    public Course getCourseByIdJpa(Long id) {
        Course course = delegate.getCourseByIdJpa(id);
        if (course != null) {
        } else {
        }
        return course;
    }

    @Override
    public void deleteCourseJpa(Long id) {
        // Check if course exists before deletion
        Course course = delegate.getCourseByIdJpa(id);
        if (course == null) {
            throw new IllegalArgumentException("Cannot delete: Course with ID " + id + " not found");
        }
        delegate.deleteCourseJpa(id);
    }

    @Override
    public List<Course> getCoursesByFacultyIdJpa(Long facultyId) {
        List<Course> courses = delegate.getCoursesByFacultyIdJpa(facultyId);
        return courses;
    }
}