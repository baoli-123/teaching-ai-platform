DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS exam_record;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS resource_item;
DROP TABLE IF EXISTS knowledge_chunk;
DROP TABLE IF EXISTS course;

CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resource_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(20) DEFAULT 'document',
    content CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT,
    title VARCHAR(200),
    content CLOB,
    tags VARCHAR(200)
);

CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    stem VARCHAR(500) NOT NULL,
    option_a VARCHAR(200),
    option_b VARCHAR(200),
    option_c VARCHAR(200),
    option_d VARCHAR(200),
    answer VARCHAR(10),
    analysis VARCHAR(500)
);

CREATE TABLE exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100),
    course_id BIGINT,
    score INT,
    total INT,
    detail VARCHAR(2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT,
    question VARCHAR(1000),
    answer CLOB,
    sources VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
