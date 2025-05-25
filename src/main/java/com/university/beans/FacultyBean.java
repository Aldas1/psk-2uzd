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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Named
@ViewScoped
public class FacultyBean implements Serializable {

    private static final Logger logger = Logger.getLogger(FacultyBean.class.getName());

    @Inject
    private FacultyService facultyService;

    @Inject
    private AsyncCalculationService asyncService;

    private List<Faculty> faculties;
    private Faculty newFaculty;
    private Faculty selectedFaculty;
    private boolean editMode = false;

    // Async state
    private Future<String> asyncSaveTask = null;
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

    /**
     * ASYNC IŠSAUGOJIMAS - demonstracija asinchroninio komunikavimo
     */
    public String saveFacultyAsync() {
        logger.info("=== UI: PRADEDAMAS ASYNC SAVE ===");
        logger.info("UI: Thread: " + Thread.currentThread().getName());

        try {
            // Kopijuojame faculty duomenis
            Faculty facultyToSave = new Faculty();
            facultyToSave.setName(newFaculty.getName());
            facultyToSave.setDepartment(newFaculty.getDepartment());

            // Pradedame asinchroninį išsaugojimą
            asyncSaveTask = asyncService.saveFacultyAsync(facultyToSave);
            isAsyncSaving = true;

            // UI pranešimas - grąžiname kontrolę iš karto
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "🔄 Async išsaugojimas pradėtas",
                            "Faculty išsaugojimas vykdomas fone (5 sek). Galite paspausti 'Refresh' ir pamatyti būseną."));

            logger.info("UI: Async save pradėtas, tęsiame UI darbą...");
            return null;

        } catch (Exception e) {
            logger.severe("UI: Klaida pradedant async save: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Klaida pradedant async save", e.getMessage()));
            return null;
        }
    }

    /**
     * REFRESH - tikrina async operacijų būseną
     */
    public String refreshAsyncStatus() {
        logger.info("=== UI: REFRESH ASYNC STATUS ===");
        logger.info("UI: Thread: " + Thread.currentThread().getName());

        if (asyncSaveTask != null) {
            if (asyncSaveTask.isDone()) {
                // Async operacija baigta
                try {
                    String result = asyncSaveTask.get(); // Neblokuoja, nes isDone() = true
                    logger.info("UI: Async save baigtas: " + result);

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "✅ Async operacija baigta", result));

                    // Reset async state
                    asyncSaveTask = null;
                    isAsyncSaving = false;

                    // Refresh faculty list
                    init();

                } catch (Exception e) {
                    logger.severe("UI: Klaida gaunant async rezultatą: " + e.getMessage());
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "❌ Async klaida", e.getMessage()));

                    asyncSaveTask = null;
                    isAsyncSaving = false;
                }
            } else {
                // Async operacija dar vykdoma
                logger.info("UI: Async save dar vykdomas...");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Faculty dar išsaugomas",
                                "Asinchroninis išsaugojimas dar vykdomas fone. Palaukite ir pabandykite vėl."));
            }
        } else {
            // Nėra async operacijų
            logger.info("UI: Nėra aktyvių async operacijų");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Statusas", "Nėra aktyvių asinchroninių operacijų."));

            // Tiesiog refresh data
            init();
        }

        return null;
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
        // Load fresh entity from database
        this.selectedFaculty = facultyService.getFacultyByIdJpa(faculty.getId());

        // DEBUG: Log the version when starting edit
        logger.info("Starting edit for Faculty ID: " + selectedFaculty.getId() +
                ", Version: " + selectedFaculty.getVersion() +
                ", Name: " + selectedFaculty.getName());

        this.editMode = true;
        return null;
    }

    public String updateFaculty() {
        try {
            // DEBUG: Log the version before update
            logger.info("Attempting update for Faculty ID: " + selectedFaculty.getId() +
                    ", Version: " + selectedFaculty.getVersion() +
                    ", Name: " + selectedFaculty.getName());

            // Check current version in database before update
            Faculty currentInDb = facultyService.getFacultyByIdJpa(selectedFaculty.getId());
            logger.info("Current version in database: " + currentInDb.getVersion());

            if (!currentInDb.getVersion().equals(selectedFaculty.getVersion())) {
                logger.warning("Version mismatch detected! DB version: " + currentInDb.getVersion() +
                        ", Entity version: " + selectedFaculty.getVersion());
                throw new OptimisticLockException("Version mismatch detected");
            }

            facultyService.updateFacultyJpa(selectedFaculty);

            // DEBUG: Log after successful update
            Faculty afterUpdate = facultyService.getFacultyByIdJpa(selectedFaculty.getId());
            logger.info("Update successful. New version: " + afterUpdate.getVersion());

            this.editMode = false;
            init(); // Refresh the list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Faculty updated successfully. New version: " + afterUpdate.getVersion()));
            return null;
        } catch (OptimisticLockException ole) {
            logger.warning("OptimisticLockException caught: " + ole.getMessage());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Concurrent Update Detected",
                            "Another user has modified this faculty record while you were editing it. " +
                                    ole.getMessage()));

            // Refresh data to show current state and exit edit mode
            init();
            this.editMode = false;
            return null;
        } catch (Exception e) {
            logger.severe("Update failed: " + e.getMessage());
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

    public boolean isAsyncSaving() {
        return isAsyncSaving;
    }
}