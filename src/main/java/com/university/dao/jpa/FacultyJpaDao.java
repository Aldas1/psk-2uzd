package com.university.dao.jpa;

import com.university.entity.Faculty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FacultyJpaDao {

    @PersistenceContext
    private EntityManager em;

    public List<Faculty> getAllFaculties() {
        return em.createQuery("SELECT f FROM Faculty f", Faculty.class).getResultList();
    }

    public Faculty getFacultyById(Long id) {
        return em.find(Faculty.class, id);
    }

    @Transactional
    public void saveFaculty(Faculty faculty) {
        if (faculty.getId() == null) {
            em.persist(faculty);
        } else {
            em.merge(faculty);
        }
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = em.find(Faculty.class, id);
        if (faculty != null) {
            em.remove(faculty);
        }
    }
}
