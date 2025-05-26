package com.university.beans;

import com.university.entity.Course;
import com.university.entity.Faculty;
import com.university.entity.Student;
import com.university.interceptor.Auditable;
import com.university.mybatis.entity.FacultyMB;
import com.university.service.CourseService;
import com.university.service.FacultyService;
import com.university.service.StudentService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
@Auditable
public class CourseBean implements Serializable {

    @Inject
    private CourseService courseService;

    @Inject
    private FacultyService facultyService;

    @Inject
    private StudentService studentService;

    @Getter
    private List<Course> courses;
    @Setter
    @Getter
    private Course newCourse;
    @Setter
    @Getter
    private Course selectedCourse;
    @Setter
    @Getter
    private Long selectedFacultyId;
    @Setter
    @Getter
    private boolean editMode = false;
    @Setter
    @Getter
    private boolean showCourseStudents = false;
    @Setter
    @Getter
    private Long showStudentsForCourseId = null;

    @PostConstruct
    public void init() {
        courses = courseService.getAllCoursesJpa();
        newCourse = new Course();
        selectedCourse = new Course();
    }

    @Auditable
    public String saveCourse() {
        try {
            if (selectedFacultyId != null) {
                Faculty faculty = facultyService.getFacultyByIdJpa(selectedFacultyId);
                newCourse.setFaculty(faculty);
            }

            courseService.saveCourseJpa(newCourse);

            init();

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
            init();
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
        this.selectedCourse = courseService.getCourseByIdJpa(course.getId());
        // Set the selected faculty ID
        if (this.selectedCourse.getFaculty() != null) {
            this.selectedFacultyId = this.selectedCourse.getFaculty().getId();
        } else {
            this.selectedFacultyId = null;
        }
        this.editMode = true;
        return null;
    }

    @Auditable
    public String updateCourse() {
        try {
            if (selectedFacultyId != null) {
                Faculty faculty = facultyService.getFacultyByIdJpa(selectedFacultyId);
                selectedCourse.setFaculty(faculty);
            } else {
                selectedCourse.setFaculty(null);
            }

            // The decorator will handle validation and enhanced saving
            courseService.saveCourseJpa(selectedCourse);

            this.editMode = false;
            init();

            boolean hasErrors = FacesContext.getCurrentInstance().getMessages(null).hasNext() &&
                    FacesContext.getCurrentInstance().getMessages().next().getSeverity().equals(FacesMessage.SEVERITY_ERROR);

            if (!hasErrors) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success", "Course updated successfully with enhanced validation."));
            }
            return null;
        } catch (IllegalArgumentException e) {
            // Validation errors are already added to JSF context by the decorator
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating course", e.getMessage()));
            return null;
        }
    }

    public List<FacultyMB> getFacultiesForDropdown() {
        return facultyService.getAllFacultiesMyBatis();
    }

    public void toggleCourseStudents(Long courseId) {
        if (showStudentsForCourseId != null && showStudentsForCourseId.equals(courseId)) {
            showStudentsForCourseId = null;
            showCourseStudents = false;
        } else {
            showStudentsForCourseId = courseId;
            showCourseStudents = true;
        }
    }

    public List<Student> getCourseStudents() {
        if (showStudentsForCourseId != null) {
            return studentService.getStudentsByCourseIdJpa(showStudentsForCourseId);
        }
        return new ArrayList<>();
    }
}