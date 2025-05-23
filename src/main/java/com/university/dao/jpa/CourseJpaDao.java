package com.university.dao.jpa;

import com.university.entity.Course;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CourseJpaDao {

    @PersistenceContext
    private EntityManager em;

    public List<Course> getAllCourses() {
        return em.createQuery("SELECT c FROM Course c", Course.class).getResultList();
    }

    public Course getCourseById(Long id) {
        return em.find(Course.class, id);
    }

    @Transactional
    public void saveCourse(Course course) {
        if (course.getId() == null) {
            em.persist(course);
        } else {
            em.merge(course);
        }
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = em.find(Course.class, id);
        if (course != null) {
            em.remove(course);
        }
    }

    public List<Course> getCoursesByFacultyId(Long facultyId) {
        return em.createQuery(
                        "SELECT c FROM Course c WHERE c.faculty.id = :facultyId",
                        Course.class)
                .setParameter("facultyId", facultyId)
                .getResultList();
    }
}
