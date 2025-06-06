package com.university.beans;

import com.university.entity.Course;
import com.university.entity.Student;
import com.university.service.CourseService;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Named
@SessionScoped
public class StudentBean implements Serializable {

    @Inject
    private StudentService studentService;

    @Inject
    private CourseService courseService;

    @Getter
    private List<Student> students;
    @Setter
    @Getter
    private Student newStudent;
    @Setter
    @Getter
    private Student selectedStudent;
    @Setter
    @Getter
    private Long selectedCourseId;
    @Setter
    @Getter
    private Long[] selectedCourseIds;
    @Setter
    @Getter
    private String persistenceType = "jpa";
    @Getter
    @Setter
    private boolean editMode = false;
    @Setter
    @Getter
    private boolean showStudentCourses = false;
    @Setter
    @Getter
    private Long showCoursesForStudentId = null;

    @PostConstruct
    public void init() {
        students = studentService.getAllStudentsJpa();
        newStudent = new Student();
        selectedStudent = new Student();
        selectedCourseIds = new Long[0];
    }

    public String saveStudent() {
        try {
            // Save the student first to get an ID
            studentService.saveStudentJpa(newStudent);

            if (selectedCourseIds != null && selectedCourseIds.length > 0) {
                for (Long courseId : selectedCourseIds) {
                    studentService.enrollStudentInCourseJpa(newStudent.getId(), courseId);
                }
                newStudent = studentService.getStudentByIdJpa(newStudent.getId());
            }

            init();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Student saved successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error saving student", e.getMessage()));
            return null;
        }
    }

    public String deleteStudent(Long id) {
        try {
            studentService.deleteStudentJpa(id);
            init();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Student deleted successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error deleting student", e.getMessage()));
            return null;
        }
    }

    public String editStudent(Student student) {
        this.selectedStudent = studentService.getStudentByIdJpa(student.getId());

        // Initialize the selectedCourseIds array with current course IDs
        if (selectedStudent.getCourses() != null && !selectedStudent.getCourses().isEmpty()) {
            selectedCourseIds = selectedStudent.getCourses().stream()
                    .map(Course::getId)
                    .toArray(Long[]::new);
        } else {
            selectedCourseIds = new Long[0];
        }

        this.editMode = true;
        return null;
    }

    public String updateStudent() {
        try {
            Set<Course> currentCourses = null;
            if (selectedStudent.getCourses() != null) {
                currentCourses = new HashSet<>(selectedStudent.getCourses());
            } else {
                currentCourses = new HashSet<>();
            }

            studentService.saveStudentJpa(selectedStudent);

            Set<Course> newCourses = new HashSet<>();
            if (selectedCourseIds != null && selectedCourseIds.length > 0) {
                for (Long courseId : selectedCourseIds) {
                    Course course = courseService.getCourseByIdJpa(courseId);
                    if (course != null) {
                        newCourses.add(course);
                    }
                }
            }

            for (Course course : newCourses) {
                boolean alreadyEnrolled = false;
                for (Course currentCourse : currentCourses) {
                    if (currentCourse.getId().equals(course.getId())) {
                        alreadyEnrolled = true;
                        break;
                    }
                }
                if (!alreadyEnrolled) {
                    studentService.enrollStudentInCourseJpa(selectedStudent.getId(), course.getId());
                }
            }

            // Determine courses to remove
            for (Course course : currentCourses) {
                boolean shouldKeep = false;
                for (Course newCourse : newCourses) {
                    if (newCourse.getId().equals(course.getId())) {
                        shouldKeep = true;
                        break;
                    }
                }
                if (!shouldKeep) {
                    studentService.removeStudentFromCourseJpa(selectedStudent.getId(), course.getId());
                }
            }

            this.editMode = false;
            init();

            students = studentService.getAllStudentsJpa();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Student updated successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating student", e.getMessage()));
            return null;
        }
    }


    public void toggleStudentCourses(Long studentId) {
        if (showCoursesForStudentId != null && showCoursesForStudentId.equals(studentId)) {
            showCoursesForStudentId = null;
            showStudentCourses = false;
        } else {
            showCoursesForStudentId = studentId;
            showStudentCourses = true;
            Student student = studentService.getStudentByIdJpa(studentId);
        }
    }

    public List<Course> getStudentCourses() {
        if (showCoursesForStudentId != null) {
            Student student = studentService.getStudentByIdJpa(showCoursesForStudentId);
            return new ArrayList<>(student.getCourses());
        }
        return new ArrayList<>();
    }
}