# Task Demo · Spring Boot + Vue

任務管理 CRUD 範例專案，採 MVC 三層架構。

- **Backend**：Spring Boot 3.4 + Spring MVC + Spring Data JPA + H2
- **Frontend**：Vue 3 + Vite + Axios
- **Build**：本機/Render 走整合（Vue 編譯成靜態檔由 Spring Boot 提供）；GitHub Pages 走分離（GHP 服務前端，Render 跑 API）
- **Deploy**：multi-stage Dockerfile 上 Render；GitHub Actions 自動發佈到 Pages

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
cd frontend && npm run build       # 預設輸出到 ../src/main/resources/static
cd .. && mvn clean package
java -jar target/task-demo.jar
```

打開 `http://localhost:8080` 看到前端，`/api/tasks` 也在同一個埠。

## 部署：Render（後端 + 整合前端）

`render.yaml` 已備好。

1. 推到 GitHub（master 分支）
2. Render → **New +** → **Blueprint** → 選 repo
3. Render 自動讀 `render.yaml`，建出 free-tier Docker web service
4. 完成後拿到 `https://java-task-demo-xxxx.onrender.com`

冷啟動 ~50 秒（免費方案閒置 15 分鐘休眠）。

## 部署：GitHub Pages（純前端）

把前端推到 `https://<your-user>.github.io/java_demo/`，後端仍維持 Render。

### 一次性設定

1. **GitHub repo 設定**
   - Settings → Pages → **Source: GitHub Actions**
   - Settings → Secrets and variables → Actions → **Variables** → New variable
     - Name: `VITE_API_BASE_URL`
     - Value: `https://java-task-demo-xxxx.onrender.com/api`（你的 Render 服務 URL + `/api`）

2. **Render 後端設定 CORS**
   - `render.yaml` 已預設允許 `https://shane360129.github.io`
   - 若你的 GitHub username 不同，在 Render Dashboard → Environment 改 `APP_CORS_ALLOWED_ORIGINS` 為 `https://<your-user>.github.io,http://localhost:5173`
   - 多個來源用逗號分隔

3. **推到 master 觸發部署**
   ```bash
   git push origin master
   ```
   `.github/workflows/deploy-pages.yml` 會自動跑：build → upload artifact → deploy
   
   也可以在 Actions 頁面手動 **Run workflow**。

### 環境變數對照

| 環境 | `VITE_BASE_PATH` | `VITE_OUT_DIR` | `VITE_API_BASE_URL` |
| --- | --- | --- | --- |
| 本機 dev | （預設 `/`） | （不適用） | （預設 `/api`，由 vite proxy 接） |
| Render 整合 JAR | （預設 `/`） | `../src/main/resources/static` | （預設 `/api`，同源） |
| GitHub Pages | `/java_demo/` | `dist` | `https://<render>.onrender.com/api` |

CI 在 `.github/workflows/deploy-pages.yml` 已設好前兩項，第三項從 repo variable 讀。

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/tasks` | 列出所有任務 |
| GET | `/api/tasks/{id}` | 取得單筆 |
| POST | `/api/tasks` | 新增 |
| PUT | `/api/tasks/{id}` | 整筆更新 |
| PATCH | `/api/tasks/{id}/status` | 只更新狀態 |
| DELETE | `/api/tasks/{id}` | 刪除單筆 |
| DELETE | `/api/tasks/completed` | 清除所有已完成 |

Request body：

```json
{
  "title": "字串，必填，≤200",
  "description": "字串，≤1000",
  "status": "TODO | IN_PROGRESS | DONE",
  "priority": "LOW | MEDIUM | HIGH",
  "dueDate": "YYYY-MM-DD"
}
```

## 資料庫

H2 in-memory，每次重啟重建，`data.sql` 預載 5 筆範例。

要換 MySQL：改 `application.yml` 的 `spring.datasource.*`，並在 `pom.xml` 加：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
