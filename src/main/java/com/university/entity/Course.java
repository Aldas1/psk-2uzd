package com.university.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true)
    private String courseCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "credits")
    private Integer credits;

    // Many-to-one relationship with faculty
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    // Many-to-many relationship with students
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // For displaying in UI
    @Override
    public String toString() {
        return courseCode + " - " + title;
    }
}
