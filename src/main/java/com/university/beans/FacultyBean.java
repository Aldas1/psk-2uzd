package com.university.beans;

import com.university.mybatis.entity.FacultyMB;
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
public class FacultyBean implements Serializable {

    @Inject
    private FacultyService facultyService;

    private List<FacultyMB> faculties;
    private FacultyMB newFaculty;
    private FacultyMB selectedFaculty;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        // Now using MyBatis instead of JPA
        faculties = facultyService.getAllFacultiesMyBatis();
        newFaculty = new FacultyMB();
        selectedFaculty = new FacultyMB();
    }

    public String saveFaculty() {
        try {
            // Using MyBatis service method
            facultyService.saveFacultyMyBatis(newFaculty);
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty saved successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error saving faculty", e.getMessage()));
            return null;
        }
    }

    public String deleteFaculty(Long id) {
        try {
            // Using MyBatis service method
            facultyService.deleteFacultyMyBatis(id);
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty deleted successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error deleting faculty", e.getMessage()));
            return null;
        }
    }

    public String editFaculty(FacultyMB faculty) {
        // Load the fresh faculty using MyBatis
        this.selectedFaculty = facultyService.getFacultyByIdMyBatis(faculty.getId());
        this.editMode = true;
        return null; // Stay on the current page
    }

    public String updateFaculty() {
        try {
            // Using MyBatis service method
            facultyService.saveFacultyMyBatis(selectedFaculty);
            this.editMode = false;
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty updated successfully."));
            return null; // Stay on the current page
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating faculty", e.getMessage()));
            return null;
        }
    }

    // Getters and setters
    public List<FacultyMB> getFaculties() {
        return faculties;
    }

    public FacultyMB getNewFaculty() {
        return newFaculty;
    }

    public void setNewFaculty(FacultyMB newFaculty) {
        this.newFaculty = newFaculty;
    }

    public FacultyMB getSelectedFaculty() {
        return selectedFaculty;
    }

    public void setSelectedFaculty(FacultyMB selectedFaculty) {
        this.selectedFaculty = selectedFaculty;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}