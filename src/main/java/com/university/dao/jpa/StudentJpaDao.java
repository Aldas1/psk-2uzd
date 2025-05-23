package com.university.dao.jpa;

import com.university.entity.Course;
import com.university.entity.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class StudentJpaDao {

    @PersistenceContext
    private EntityManager em;

    public List<Student> getAllStudents() {
        return em.createQuery("SELECT s FROM Student s", Student.class).getResultList();
    }

    public Student getStudentById(Long id) {
        return em.createQuery(
                    "SELECT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id",
                    Student.class)
            .setParameter("id", id)
            .getSingleResult();
    }

    @Transactional
    public void saveStudent(Student student) {
        if (student.getId() == null) {
            em.persist(student);
        } else {
            em.merge(student);
        }
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = em.find(Student.class, id);
        if (student != null) {
            em.remove(student);
        }
    }

    @Transactional
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        Student student = em.find(Student.class, studentId);
        Course course = em.find(Course.class, courseId);

        if (student != null && course != null) {
            student.addCourse(course);
        }
    }

    @Transactional
    public void removeStudentFromCourse(Long studentId, Long courseId) {
        Student student = em.find(Student.class, studentId);
        Course course = em.find(Course.class, courseId);

        if (student != null && course != null) {
            student.removeCourse(course);
        }
    }

    public List<Student> getStudentsByCourseId(Long courseId) {
        return em.createQuery(
                        "SELECT s FROM Student s JOIN s.courses c WHERE c.id = :courseId",
                        Student.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }
}
