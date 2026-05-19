INSERT INTO tasks (title, description, status, priority, due_date, created_at, completed_at) VALUES
('熟悉 Spring Boot 專案結構', '看完官方 Getting Started、跑起 hello world', 'DONE', 'MEDIUM', '2026-05-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('串接 Vue 前端 CRUD', '用 axios 打 /api/tasks，做出列表、新增、編輯、刪除', 'IN_PROGRESS', 'HIGH', '2026-05-20', CURRENT_TIMESTAMP, NULL),
('部署到 Render', 'Dockerfile 推上 GitHub 後接 Render 自動 build', 'TODO', 'HIGH', '2026-05-25', CURRENT_TIMESTAMP, NULL),
('寫單元測試', '為 TaskService 補上 JUnit 測試覆蓋率', 'TODO', 'LOW', NULL, CURRENT_TIMESTAMP, NULL),
('整理履歷', '把 demo URL 放上去，補幾張 screenshot', 'TODO', 'MEDIUM', '2026-05-30', CURRENT_TIMESTAMP, NULL);
