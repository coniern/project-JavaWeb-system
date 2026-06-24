DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(32),
    avatar VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE project (
    project_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    tech_stack VARCHAR(64) NOT NULL,
    status VARCHAR(32),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    leader_id BIGINT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    progress INT DEFAULT 0
);

CREATE TABLE task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    assignee_id BIGINT NOT NULL,
    due_date VARCHAR(32),
    create_time TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE project_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(64) NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
