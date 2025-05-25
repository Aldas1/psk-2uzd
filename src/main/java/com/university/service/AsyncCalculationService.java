package com.university.service;

import com.university.entity.Faculty;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.concurrent.Future;
import java.util.logging.Logger;

@Stateless
public class AsyncCalculationService {
    @PersistenceContext
    private EntityManager em;
    @Asynchronous
    @Transactional
    public Future<String> saveFacultyAsync(Faculty faculty) {
        try {
            Thread.sleep(5000);
            Faculty managedFaculty;
            if (faculty.getId() == null) {
                em.persist(faculty);
                managedFaculty = faculty;
            } else {
                managedFaculty = em.merge(faculty);
            }
            em.flush();
            String result = "Faculty '" + managedFaculty.getName() + "' sėkmingai išsaugotas asinchroniškai po 5 sek.";
            return new AsyncResult<>(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String error = "Faculty išsaugojimas nutrauktas";
            return new AsyncResult<>(error);
        } catch (Exception e) {
            String error = "Klaida išsaugojant faculty: " + e.getMessage();
            return new AsyncResult<>(error);
        }
    }
}