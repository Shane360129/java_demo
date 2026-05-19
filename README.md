# Task Demo · Spring Boot + Vue

任務管理 CRUD 範例專案，採 MVC 三層架構。

- **Backend**：Spring Boot 3.4 + Spring MVC + Spring Data JPA + H2
- **Frontend**：Vue 3 + Vite + Axios
- **Build**：Vue 編譯成靜態檔，由 Spring Boot 一起提供
- **Deploy**：multi-stage Dockerfile，一鍵推到 Render / Railway

## 本機開發

需要 Java 17、Maven 3.9+、Node 20+。

**Terminal 1（後端）**

```bash
mvn spring-boot:run
```

API 開在 `http://localhost:8080/api/tasks`。

**Terminal 2（前端）**

```bash
cd frontend
npm install
npm run dev
```

打開 `http://localhost:5173`，dev server 會自動把 `/api` 代理到 8080。

## 打包成單一 JAR

```bash
cd frontend && npm run build       # 輸出到 ../src/main/resources/static
cd .. && mvn clean package
java -jar target/task-demo.jar
```

打開 `http://localhost:8080` 看到前端，`/api/tasks` 也在同一個埠。

## 部署到 Render

1. 把專案推到 GitHub
2. Render → New → Web Service → Docker
3. 選這個 repo，build 約 3–5 分鐘
4. 完成後拿到 `https://<your-app>.onrender.com` 就是 Demo URL

Railway / Fly.io 流程類似，都吃 Dockerfile。

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/tasks` | 列出所有任務 |
| GET | `/api/tasks/{id}` | 取得單筆 |
| POST | `/api/tasks` | 新增 |
| PUT | `/api/tasks/{id}` | 更新 |
| DELETE | `/api/tasks/{id}` | 刪除 |

Request body：

```json
{
  "title": "字串，必填，≤200",
  "description": "字串，≤1000",
  "status": "TODO | IN_PROGRESS | DONE",
  "dueDate": "YYYY-MM-DD"
}
```

## 資料庫

H2 in-memory，每次重啟重建，`data.sql` 預載 3 筆範例。

要換 MySQL：改 `application.yml` 的 `spring.datasource.*`，並在 `pom.xml` 加：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
