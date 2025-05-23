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

    private List<Student> students;
    private Student newStudent;
    private Student selectedStudent;
    private Long selectedCourseId;
    private Long[] selectedCourseIds; // New array to hold selected course IDs from checkboxes
    private String persistenceType = "jpa"; // Default to JPA
    private boolean editMode = false;
    private boolean showStudentCourses = false;
    private Long showCoursesForStudentId = null;

    @PostConstruct
    public void init() {
        students = studentService.getAllStudentsJpa();
        newStudent = new Student();
        selectedStudent = new Student();
        selectedCourseIds = new Long[0]; // Initialize empty array
    }

    public String saveStudent() {
        try {
            // Save the student first to get an ID
            studentService.saveStudentJpa(newStudent);

            // Now enroll student in each selected course
            if (selectedCourseIds != null && selectedCourseIds.length > 0) {
                for (Long courseId : selectedCourseIds) {
                    studentService.enrollStudentInCourseJpa(newStudent.getId(), courseId);
                }
                // Refresh the student to include courses
                newStudent = studentService.getStudentByIdJpa(newStudent.getId());
            }

            init(); // Refresh the list
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
            init(); // Refresh the list
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
        // Reload the student to ensure all associations are loaded
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
        return null; // Stay on the current page
    }

    public String updateStudent() {
        try {
            // Get the current enrolled courses before updating
            Set<Course> currentCourses = null;
            if (selectedStudent.getCourses() != null) {
                currentCourses = new HashSet<>(selectedStudent.getCourses());
            } else {
                currentCourses = new HashSet<>();
            }

            // First save the basic student info
            studentService.saveStudentJpa(selectedStudent);

            // Convert selectedCourseIds to a Set of Course objects
            Set<Course> newCourses = new HashSet<>();
            if (selectedCourseIds != null && selectedCourseIds.length > 0) {
                for (Long courseId : selectedCourseIds) {
                    Course course = courseService.getCourseByIdJpa(courseId);
                    if (course != null) {
                        newCourses.add(course);
                    }
                }
            }

            // Determine courses to add
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
            init(); // Refresh the list

            // Refresh the students list to show the updated data
            students = studentService.getAllStudentsJpa();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Student updated successfully."));
            return null; // Stay on the current page
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating student", e.getMessage()));
            return null;
        }
    }


    public void toggleStudentCourses(Long studentId) {
        if (showCoursesForStudentId != null && showCoursesForStudentId.equals(studentId)) {
            // If already showing this student's courses, hide them
            showCoursesForStudentId = null;
            showStudentCourses = false;
        } else {
            // Otherwise show this student's courses
            showCoursesForStudentId = studentId;
            showStudentCourses = true;
            // Make sure we have the latest data by loading the student from the database
            Student student = studentService.getStudentByIdJpa(studentId);
            // The courses are now loaded via the getStudentCourses method
        }
    }

    public List<Course> getStudentCourses() {
        if (showCoursesForStudentId != null) {
            Student student = studentService.getStudentByIdJpa(showCoursesForStudentId);
            return new ArrayList<>(student.getCourses());
        }
        return new ArrayList<>();
    }

    // Getters and setters
    public List<Student> getStudents() {
        return students;
    }

    public Student getNewStudent() {
        return newStudent;
    }

    public void setNewStudent(Student newStudent) {
        this.newStudent = newStudent;
    }

    public Student getSelectedStudent() {
        return selectedStudent;
    }

    public void setSelectedStudent(Student selectedStudent) {
        this.selectedStudent = selectedStudent;
    }

    public Long getSelectedCourseId() {
        return selectedCourseId;
    }

    public void setSelectedCourseId(Long selectedCourseId) {
        this.selectedCourseId = selectedCourseId;
    }

    public Long[] getSelectedCourseIds() {
        return selectedCourseIds;
    }

    public void setSelectedCourseIds(Long[] selectedCourseIds) {
        this.selectedCourseIds = selectedCourseIds;
    }

    public String getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(String persistenceType) {
        this.persistenceType = persistenceType;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean isShowStudentCourses() {
        return showStudentCourses;
    }

    public void setShowStudentCourses(boolean showStudentCourses) {
        this.showStudentCourses = showStudentCourses;
    }

    public Long getShowCoursesForStudentId() {
        return showCoursesForStudentId;
    }

    public void setShowCoursesForStudentId(Long showCoursesForStudentId) {
        this.showCoursesForStudentId = showCoursesForStudentId;
    }
}