package com.university.service;

import com.university.dao.jpa.FacultyJpaDao;
import com.university.dao.mybatis.FacultyMyBatisDao;
import com.university.entity.Faculty;
import com.university.mybatis.entity.FacultyMB;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.AsyncResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@ApplicationScoped
public class FacultyService {

    @Inject
    private FacultyJpaDao facultyJpaDao;

    @Inject
    private FacultyMyBatisDao facultyMyBatisDao;

    public List<Faculty> getAllFacultiesJpa() {
        return facultyJpaDao.getAllFaculties();
    }

    public Faculty getFacultyByIdJpa(Long id) {
        return facultyJpaDao.getFacultyById(id);
    }

    @Transactional
    public void saveFacultyJpa(Faculty faculty) {
        facultyJpaDao.saveFaculty(faculty);
    }

    @Transactional
    public void updateFacultyJpa(Faculty faculty) {
        facultyJpaDao.updateFaculty(faculty);
    }

    @Transactional
    public void deleteFacultyJpa(Long id) {
        facultyJpaDao.deleteFaculty(id);
    }

    public List<FacultyMB> getAllFacultiesMyBatis() {
        return facultyMyBatisDao.getAllFaculties();
    }

    public FacultyMB getFacultyByIdMyBatis(Long id) {
        return facultyMyBatisDao.getFacultyById(id);
    }

    @Transactional
    public void saveFacultyMyBatis(FacultyMB faculty) {
        facultyMyBatisDao.saveFaculty(faculty);
    }

    @Transactional
    public void deleteFacultyMyBatis(Long id) {
        facultyMyBatisDao.deleteFaculty(id);
    }
}