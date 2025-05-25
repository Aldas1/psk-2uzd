package com.university.beans;

import com.university.entity.Course;
import com.university.entity.Faculty;
import com.university.interceptor.Auditable;
import com.university.mybatis.entity.FacultyMB;
import com.university.service.CourseService;
import com.university.service.FacultyService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
@Auditable  // This will trigger the audit interceptor
public class CourseBean implements Serializable {

    @Inject
    private CourseService courseService;

    @Inject
    private FacultyService facultyService;

    private List<Course> courses;
    private Course newCourse;
    private Course selectedCourse;
    private Long selectedFacultyId;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        courses = courseService.getAllCoursesJpa();
        newCourse = new Course();
        selectedCourse = new Course();
    }

    @Auditable
    public String saveCourse() {
        try {
            // If a faculty is selected, set it for the new course
            if (selectedFacultyId != null) {
                // Get faculty using JPA since Course entity still uses JPA Faculty entity
                Faculty faculty = facultyService.getFacultyByIdJpa(selectedFacultyId);
                newCourse.setFaculty(faculty);
            }

            // The decorator will handle validation and enhanced saving
            courseService.saveCourseJpa(newCourse);

            init(); // Refresh the list

            // Only add success message if no validation errors occurred
            // Check if there are any error messages already in the context
            boolean hasErrors = FacesContext.getCurrentInstance().getMessages(null).hasNext() &&
                    FacesContext.getCurrentInstance().getMessages().next().getSeverity().equals(FacesMessage.SEVERITY_ERROR);

            if (!hasErrors) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success", "Course saved successfully with enhanced validation."));
            }
            return null;
        } catch (IllegalArgumentException e) {
            // Validation errors are already added to JSF context by the decorator
            // Don't add another generic error message - just return null to stay on page
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error saving course", e.getMessage()));
            return null;
        }
    }

    @Auditable
    public String deleteCourse(Long id) {
        try {
            courseService.deleteCourseJpa(id);
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Course deleted successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error deleting course", e.getMessage()));
            return null;
        }
    }

    public String editCourse(Course course) {
        // Load the fresh course with all associations
        this.selectedCourse = courseService.getCourseByIdJpa(course.getId());
        // Set the selected faculty ID
        if (this.selectedCourse.getFaculty() != null) {
            this.selectedFacultyId = this.selectedCourse.getFaculty().getId();
        } else {
            this.selectedFacultyId = null;
        }
        this.editMode = true;
        return null; // Stay on current page
    }

    @Auditable
    public String updateCourse() {
        try {
            // If a faculty is selected, set it for the course
            if (selectedFacultyId != null) {
                // Get faculty using JPA since Course entity still uses JPA Faculty entity
                Faculty faculty = facultyService.getFacultyByIdJpa(selectedFacultyId);
                selectedCourse.setFaculty(faculty);
            } else {
                selectedCourse.setFaculty(null);
            }

            // The decorator will handle validation and enhanced saving
            courseService.saveCourseJpa(selectedCourse);

            this.editMode = false;
            init(); // Refresh the list

            // Only add success message if no validation errors occurred
            boolean hasErrors = FacesContext.getCurrentInstance().getMessages(null).hasNext() &&
                    FacesContext.getCurrentInstance().getMessages().next().getSeverity().equals(FacesMessage.SEVERITY_ERROR);

            if (!hasErrors) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success", "Course updated successfully with enhanced validation."));
            }
            return null; // Stay on current page
        } catch (IllegalArgumentException e) {
            // Validation errors are already added to JSF context by the decorator
            // Don't add another generic error message - just return null to stay on page
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating course", e.getMessage()));
            return null;
        }
    }

    // Method to get faculties for dropdown - now using MyBatis
    public List<FacultyMB> getFacultiesForDropdown() {
        return facultyService.getAllFacultiesMyBatis();
    }

    // Getters and setters
    public List<Course> getCourses() {
        return courses;
    }

    public Course getNewCourse() {
        return newCourse;
    }

    public void setNewCourse(Course newCourse) {
        this.newCourse = newCourse;
    }

    public Course getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public Long getSelectedFacultyId() {
        return selectedFacultyId;
    }

    public void setSelectedFacultyId(Long selectedFacultyId) {
        this.selectedFacultyId = selectedFacultyId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}