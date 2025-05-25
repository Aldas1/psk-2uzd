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

/**
 * Asinchroninis servisas demonstracijai
 */
@Stateless
public class AsyncCalculationService {

    private static final Logger logger = Logger.getLogger(AsyncCalculationService.class.getName());

    @PersistenceContext
    private EntityManager em;
    @Asynchronous
    @Transactional
    public Future<String> saveFacultyAsync(Faculty faculty) {
        logger.info("=== ASYNC FACULTY SAVE START ===");
        logger.info("ASYNC SAVE: Thread: " + Thread.currentThread().getName());
        logger.info("ASYNC SAVE: Faculty: " + faculty.getName() + " (ID: " + faculty.getId() + ")");

        try {
            // Imituojame ilgą išsaugojimo procesą (5 sekundės)
            logger.info("ASYNC SAVE: Pradedamas ilgas išsaugojimo procesas...");
            for (int i = 1; i <= 5; i++) {
                Thread.sleep(1000);
                logger.info("ASYNC SAVE: Progresas " + i + "/5 sekundžių...");
            }

            // Dabar tikrai išsaugome
            Faculty managedFaculty;
            if (faculty.getId() == null) {
                // Naujas faculty
                em.persist(faculty);
                managedFaculty = faculty;
                logger.info("ASYNC SAVE: Naujas faculty sukurtas");
            } else {
                // Atnaujinamas faculty
                managedFaculty = em.merge(faculty);
                logger.info("ASYNC SAVE: Faculty atnaujintas");
            }

            em.flush(); // Priverstinis išsaugojimas

            String result = "Faculty '" + managedFaculty.getName() + "' sėkmingai išsaugotas asinchroniškai po 5 sek.";
            logger.info("ASYNC SAVE: " + result);
            logger.info("=== ASYNC FACULTY SAVE END ===");

            return new AsyncResult<>(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String error = "Faculty išsaugojimas nutrauktas";
            logger.warning("ASYNC SAVE: " + error);
            return new AsyncResult<>(error);
        } catch (Exception e) {
            String error = "Klaida išsaugojant faculty: " + e.getMessage();
            logger.severe("ASYNC SAVE: " + error);
            return new AsyncResult<>(error);
        }
    }
}