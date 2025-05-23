package com.university.dao.mybatis;

import com.university.mybatis.entity.FacultyMB;
import com.university.mybatis.mapper.FacultyMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FacultyMyBatisDao {

    @Inject
    private FacultyMapper facultyMapper;

    public List<FacultyMB> getAllFaculties() {
        return facultyMapper.getAllFaculties();
    }

    public FacultyMB getFacultyById(Long id) {
        return facultyMapper.getFacultyById(id);
    }

    @Transactional
    public void saveFaculty(FacultyMB faculty) {
        if (faculty.getId() == null) {
            facultyMapper.insertFaculty(faculty);
        } else {
            facultyMapper.updateFaculty(faculty);
        }
    }

    @Transactional
    public void deleteFaculty(Long id) {
        facultyMapper.deleteFaculty(id);
    }
}
