INSERT INTO sys_user (id, username, password, nickname, email, status, deleted)
SELECT 1, 'admin', '123456', '管理员', 'admin@example.com', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 1);

INSERT INTO sys_user (id, username, password, nickname, email, status, deleted)
SELECT 2, 'zhangsan', '123456', '张三', 'zhangsan@example.com', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 2);

INSERT INTO sys_user (id, username, password, nickname, email, status, deleted)
SELECT 3, 'lisi', '123456', '李四', 'lisi@example.com', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 3);

INSERT INTO project (id, name, description, status, owner_id, start_date, end_date, progress, deleted)
SELECT 1, '电商平台开发', '基于 Spring Boot + Vue3 的电商平台项目', '进行中', 1, '2026-01-01', '2026-06-30', 65, 0
WHERE NOT EXISTS (SELECT 1 FROM project WHERE id = 1);

INSERT INTO project (id, name, description, status, owner_id, start_date, end_date, progress, deleted)
SELECT 2, '企业官网重构', '公司官网重新设计与开发', '已完成', 1, '2026-02-01', '2026-04-30', 100, 0
WHERE NOT EXISTS (SELECT 1 FROM project WHERE id = 2);

INSERT INTO project (id, name, description, status, owner_id, start_date, end_date, progress, deleted)
SELECT 3, '移动端 APP 开发', 'iOS 和 Android 双端应用开发', '规划中', 2, '2026-07-01', '2026-12-31', 10, 0
WHERE NOT EXISTS (SELECT 1 FROM project WHERE id = 3);

INSERT INTO task (id, project_id, title, description, status, priority, assignee_id, due_date, deleted)
SELECT 1, 1, '用户模块开发', '实现用户注册、登录、权限管理', '已完成', 1, 2, '2026-02-28', 0
WHERE NOT EXISTS (SELECT 1 FROM task WHERE id = 1);

INSERT INTO task (id, project_id, title, description, status, priority, assignee_id, due_date, deleted)
SELECT 2, 1, '商品模块开发', '商品管理、分类、搜索功能', '进行中', 2, 3, '2026-03-31', 0
WHERE NOT EXISTS (SELECT 1 FROM task WHERE id = 2);

INSERT INTO task (id, project_id, title, description, status, priority, assignee_id, due_date, deleted)
SELECT 3, 1, '订单模块开发', '购物车、下单、支付流程', '待开始', 1, 2, '2026-04-30', 0
WHERE NOT EXISTS (SELECT 1 FROM task WHERE id = 3);
