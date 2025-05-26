package com.university.beans;

import com.university.entity.Faculty;
import com.university.service.AsyncCalculationService;
import com.university.service.FacultyService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.OptimisticLockException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

@Named
@ViewScoped
public class FacultyBean implements Serializable {
    @Inject
    private FacultyService facultyService;

    @Inject
    private AsyncCalculationService asyncService;

    @Getter
    private List<Faculty> faculties;
    @Setter @Getter
    private Faculty newFaculty;
    @Setter @Getter
    private Faculty selectedFaculty;
    @Setter @Getter
    private boolean editMode = false;

    @Setter @Getter
    private boolean showConflictDialog = false;
    @Setter @Getter
    private Faculty conflictedFaculty;
    @Setter @Getter
    private Faculty databaseFaculty;

    private Future<String> asyncSaveTask = null;
    @Getter
    private boolean isAsyncSaving = false;

    @PostConstruct
    public void init() {
        faculties = facultyService.getAllFacultiesJpa();
        newFaculty = new Faculty();
        selectedFaculty = new Faculty();
    }

    public String saveFaculty() {
        try {
            facultyService.saveFacultyJpa(newFaculty);
            init();
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

    public String saveFacultyAsync() {
        try {
            Faculty facultyToSave = new Faculty();
            facultyToSave.setName(newFaculty.getName());
            facultyToSave.setDepartment(newFaculty.getDepartment());

            asyncSaveTask = asyncService.saveFacultyAsync(facultyToSave);
            isAsyncSaving = true;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Async išsaugojimas pradėtas",
                            "Faculty išsaugojimas vykdomas fone (5 sek). Galite paspausti 'Refresh' ir pamatyti būseną."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Klaida pradedant async save", e.getMessage()));
            return null;
        }
    }

    public String refreshAsyncStatus() {
        if (asyncSaveTask != null) {
            if (asyncSaveTask.isDone()) {
                try {
                    String result = asyncSaveTask.get();
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Async operacija baigta", result));
                    asyncSaveTask = null;
                    isAsyncSaving = false;
                    init();
                } catch (Exception e) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Async klaida", e.getMessage()));
                    asyncSaveTask = null;
                    isAsyncSaving = false;
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Faculty dar išsaugomas",
                                "Asinchroninis išsaugojimas dar vykdomas fone. Palaukite ir pabandykite vėl."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Statusas", "Nėra aktyvių asinchroninių operacijų."));
            init();
        }
        return null;
    }

    public String deleteFaculty(Long id) {
        try {
            facultyService.deleteFacultyJpa(id);
            init();
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
        this.selectedFaculty = facultyService.getFacultyByIdJpa(faculty.getId());
        this.editMode = true;
        return null;
    }

    public String updateFaculty() {
        try {
            Faculty currentInDb = facultyService.getFacultyByIdJpa(selectedFaculty.getId());

            if (!currentInDb.getVersion().equals(selectedFaculty.getVersion())) {
                this.conflictedFaculty = new Faculty();
                this.conflictedFaculty.setId(selectedFaculty.getId());
                this.conflictedFaculty.setName(selectedFaculty.getName());
                this.conflictedFaculty.setDepartment(selectedFaculty.getDepartment());
                this.conflictedFaculty.setVersion(selectedFaculty.getVersion());

                this.databaseFaculty = currentInDb;
                this.showConflictDialog = true;

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Concurrent Modification Detected",
                                "Another user has modified this faculty record. Please choose how to proceed."));
                return null;
            }

            facultyService.updateFacultyJpa(selectedFaculty);
            Faculty afterUpdate = facultyService.getFacultyByIdJpa(selectedFaculty.getId());

            this.editMode = false;
            init();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty updated successfully. New version: " + afterUpdate.getVersion()));
            return null;

        } catch (OptimisticLockException ole) {
            handleOptimisticLockException(ole);
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error updating faculty", e.getMessage()));
            return null;
        }
    }

    private void handleOptimisticLockException(OptimisticLockException ole) {
        try {
            Faculty currentInDb = facultyService.getFacultyByIdJpa(selectedFaculty.getId());

            this.conflictedFaculty = new Faculty();
            this.conflictedFaculty.setId(selectedFaculty.getId());
            this.conflictedFaculty.setName(selectedFaculty.getName());
            this.conflictedFaculty.setDepartment(selectedFaculty.getDepartment());
            this.conflictedFaculty.setVersion(selectedFaculty.getVersion());

            this.databaseFaculty = currentInDb;
            this.showConflictDialog = true;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Optimistic Lock Exception",
                            "Entity was modified by another user. Transaction was rolled back. Please choose how to proceed."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error handling conflict", e.getMessage()));
            init();
            this.editMode = false;
        }
    }

    public String discardChanges() {
        showConflictDialog = false;
        editMode = false;
        conflictedFaculty = null;
        databaseFaculty = null;
        init();

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Changes Discarded", "Your changes have been discarded. Data refreshed from database."));
        return null;
    }

    public String overwriteChanges() {
        try {
            Faculty facultyToSave = new Faculty();
            facultyToSave.setId(conflictedFaculty.getId());
            facultyToSave.setName(conflictedFaculty.getName());
            facultyToSave.setDepartment(conflictedFaculty.getDepartment());
            facultyToSave.setVersion(databaseFaculty.getVersion());

            facultyService.updateFacultyJpa(facultyToSave);

            showConflictDialog = false;
            editMode = false;
            conflictedFaculty = null;
            databaseFaculty = null;
            init();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Changes Overwritten", "Your changes have been saved, overwriting the other user's changes."));
            return null;

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error overwriting changes", e.getMessage()));
            return null;
        }
    }

    public String cancelConflictResolution() {
        showConflictDialog = false;
        conflictedFaculty = null;
        databaseFaculty = null;
        return null;
    }
}