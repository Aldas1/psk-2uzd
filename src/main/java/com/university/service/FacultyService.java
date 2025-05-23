package com.university.service;

import com.university.dao.jpa.FacultyJpaDao;
import com.university.dao.mybatis.FacultyMyBatisDao;
import com.university.entity.Faculty;
import com.university.mybatis.entity.FacultyMB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FacultyService {

    @Inject
    private FacultyJpaDao facultyJpaDao;

    @Inject
    private FacultyMyBatisDao facultyMyBatisDao;

    // JPA methods
    public Faculty getFacultyByIdJpa(Long id) {
        return facultyJpaDao.getFacultyById(id);
    }

    // MyBatis methods
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
