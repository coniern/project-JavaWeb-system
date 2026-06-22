CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    avatar VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS project (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL,
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    progress INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    assignee_id BIGINT NOT NULL,
    due_date VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_project_owner_id ON project (owner_id);
CREATE INDEX IF NOT EXISTS idx_task_project_id ON task (project_id);
CREATE INDEX IF NOT EXISTS idx_task_assignee_id ON task (assignee_id);
