package com.university.beans;

import com.university.entity.Faculty;
import com.university.service.FacultyService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.OptimisticLockException;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class FacultyBean implements Serializable {

    @Inject
    private FacultyService facultyService;

    private List<Faculty> faculties;
    private Faculty newFaculty;
    private Faculty selectedFaculty;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        faculties = facultyService.getAllFacultiesJpa();
        newFaculty = new Faculty();
        selectedFaculty = new Faculty();
    }

    public String saveFaculty() {
        try {
            facultyService.saveFacultyJpa(newFaculty);
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
            facultyService.deleteFacultyJpa(id);
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

    public String editFaculty(Faculty faculty) {
        // Create a detached copy to ensure optimistic locking works
        this.selectedFaculty = new Faculty();
        this.selectedFaculty.setId(faculty.getId());
        this.selectedFaculty.setName(faculty.getName());
        this.selectedFaculty.setDepartment(faculty.getDepartment());
        this.selectedFaculty.setVersion(faculty.getVersion());
        this.editMode = true;
        return null;
    }

    public String updateFaculty() {
        try {
            facultyService.updateFacultyJpa(selectedFaculty);
            this.editMode = false;
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty updated successfully."));
            return null;
        } catch (OptimisticLockException ole) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Optimistic Lock Exception",
                            "The faculty was modified by another user. Please refresh and try again."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating faculty", e.getMessage()));
            return null;
        }
    }

    // Getters and setters
    public List<Faculty> getFaculties() {
        return faculties;
    }

    public Faculty getNewFaculty() {
        return newFaculty;
    }

    public void setNewFaculty(Faculty newFaculty) {
        this.newFaculty = newFaculty;
    }

    public Faculty getSelectedFaculty() {
        return selectedFaculty;
    }

    public void setSelectedFaculty(Faculty selectedFaculty) {
        this.selectedFaculty = selectedFaculty;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}