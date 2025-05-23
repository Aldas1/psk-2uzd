package com.university.mybatis.entity;

import lombok.Data;

/**
 * MyBatis Faculty entity
 */
@Data
public class FacultyMB {
    private Long id;
    private String name;
    private String department;
}