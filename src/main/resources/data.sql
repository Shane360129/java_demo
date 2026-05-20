INSERT INTO tasks (title, description, status, priority, due_date, created_at, completed_at) VALUES
('熟悉 Spring Boot 專案結構', '看完官方 Getting Started、跑起 hello world', 'DONE', 'MEDIUM', '2026-05-08', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('忘了做的拖延任務', '這個應該早就要完成了，記得補上', 'TODO', 'MEDIUM', '2026-05-12', CURRENT_TIMESTAMP, NULL),
('串接 Vue 前端 CRUD', '用 axios 打 /api/tasks，做列表、新增、編輯、刪除', 'DONE', 'HIGH', '2026-05-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('響應式 UI 與深色模式', '把表單和列表的響應式做好，補上深色模式', 'IN_PROGRESS', 'HIGH', '2026-05-20', CURRENT_TIMESTAMP, NULL),
('整合行事曆檢視', '月曆 + 點日期看當天任務 + 在日期上快速新增', 'IN_PROGRESS', 'HIGH', '2026-05-21', CURRENT_TIMESTAMP, NULL),
('部署到 Render', 'Dockerfile 推上 GitHub 後接 Render 自動 build', 'TODO', 'HIGH', '2026-05-22', CURRENT_TIMESTAMP, NULL),
('設定 GitHub Pages', '前端 GHP + 後端 Render 雙部署', 'TODO', 'MEDIUM', '2026-05-23', CURRENT_TIMESTAMP, NULL),
('週末跑步 5K', '台大操場早上 7 點', 'TODO', 'LOW', '2026-05-24', CURRENT_TIMESTAMP, NULL),
('寫單元測試', '為 TaskService 補上 JUnit 覆蓋率 80%+', 'TODO', 'LOW', '2026-05-26', CURRENT_TIMESTAMP, NULL),
('整理履歷', '把 demo URL 放上去，補幾張 screenshot', 'TODO', 'MEDIUM', '2026-05-30', CURRENT_TIMESTAMP, NULL),
('準備技術面試', 'LeetCode top 100 + system design 複習', 'TODO', 'HIGH', '2026-06-01', CURRENT_TIMESTAMP, NULL),
('讀完 Effective Java', '3rd edition，每章做筆記', 'TODO', 'LOW', '2026-06-15', CURRENT_TIMESTAMP, NULL),
('整理書桌', '雜物太多了', 'TODO', 'LOW', NULL, CURRENT_TIMESTAMP, NULL);
