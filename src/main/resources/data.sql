INSERT INTO sys_role (id, name, code, description, create_time, update_time) VALUES
(1, '系统管理员', 'ADMIN', '拥有全部权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '开发人员', 'DEVELOPER', '可以维护项目', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '测试人员', 'TESTER', '可以查看项目和测试信息', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_user (id, username, password, nickname, email, phone, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '管理员', 'admin@example.com', '13800000000', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'dev', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '开发同学', 'dev@example.com', '13900000000', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'tester', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '测试同学', '13700000000@example.com', '13700000000', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'javaweb01', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '后端实习生A', 'javaweb01@example.com', '13600000001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'javaweb02', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '前端协作B', 'javaweb02@example.com', '13600000002', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'javaweb03', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '测试工程师C', 'javaweb03@example.com', '13600000003', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'pm01', '$2a$10$aNTKh9mZzKISu8f8BXWB9OAkxVH84v/YF.n3CYxKhBf3lQ2YEFgmi', '项目经理D', 'pm01@example.com', '13600000004', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 2),
(5, 2),
(6, 3),
(7, 1);

INSERT INTO project (project_id, name, description, tech_stack, status, leader_id, create_time, update_time, progress) VALUES
(1, '电商平台项目', '电商系统开发与部署管理', 'Spring Boot', 'developing', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 65),
(2, '企业官网重构', '官网改版与接口整合', 'SSM', 'testing', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 85),
(3, '移动应用后端', '移动端后端服务建设', 'Spring Cloud', 'planning', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 20),
(4, '校园论坛系统', 'JavaWeb 课程项目论坛模块迭代', 'SSM', 'developing', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 42),
(5, '毕业设计管理平台', '教师端与学生端流程联调', 'Spring Boot', 'testing', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 73),
(6, '图书借阅系统', '传统 JavaWeb 业务迁移与权限改造', 'Spring Boot', 'deployed', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 100),
(7, '在线考试系统', '题库与考试流程模块开发', 'Spring Cloud', 'developing', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 58),
(8, '实习管理平台', '企业导师评价与过程记录', 'SSM', 'planning', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 15);

INSERT INTO project_member (id, project_id, user_id, role, create_time, update_time) VALUES
(1, 1, 1, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 3, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 2, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 3, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 1, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 4, 4, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 4, 5, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 4, 6, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 5, 7, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 5, 2, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 5, 6, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 6, 1, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 6, 4, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 7, 5, 'DEVELOPER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 7, 6, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 8, 7, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 8, 3, 'TESTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
