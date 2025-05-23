CREATE TABLE faculty (
     id SERIAL PRIMARY KEY,
     name VARCHAR(100) NOT NULL,
     department VARCHAR(100)
);

CREATE TABLE course (
    id SERIAL PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    credits INTEGER,
    faculty_id INTEGER REFERENCES faculty(id)
);

CREATE TABLE student (
     id SERIAL PRIMARY KEY,
     student_id VARCHAR(20) NOT NULL UNIQUE,
     first_name VARCHAR(50) NOT NULL,
     last_name VARCHAR(50) NOT NULL,
     email VARCHAR(100)
);

CREATE TABLE student_course (
    student_id INTEGER REFERENCES student(id),
    course_id INTEGER REFERENCES course(id),
    PRIMARY KEY (student_id, course_id)
);

INSERT INTO faculty (name, department) VALUES
    ('Faculty of Science', 'Computer Science'),
    ('Faculty of Arts', 'Music'),
    ('Faculty of Engineering', 'Civil Engineering');

INSERT INTO course (course_code, title, credits, faculty_id) VALUES
     ('CS101', 'Introduction to Programming', 6, 1),
     ('CS202', 'Algorithms and Data Structures', 6, 1),
     ('MUS101', 'Music Theory', 4, 2),
     ('CE101', 'Engineering Mechanics', 6, 3);

INSERT INTO student (student_id, first_name, last_name, email) VALUES
   ('S001', 'John', 'Doe', 'john.doe@example.com'),
   ('S002', 'Jane', 'Smith', 'jane.smith@example.com'),
   ('S003', 'Alice', 'Johnson', 'alice.johnson@example.com');

INSERT INTO student_course (student_id, course_id) VALUES
    (1, 1), (1, 2), (2, 3), (3, 4), (2, 1);