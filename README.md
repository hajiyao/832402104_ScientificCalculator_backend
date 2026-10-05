# 计算器后端

前后端分离计算器系统的后端。接收前端发来的表达式，校验、解析、计算，
把每次成功的结果存进 SQLite，并提供历史记录的查询和删除。
启动后同时托管前端页面，一个地址就是完整网站。

## 技术栈

- Java 8
- JDK 自带 `com.sun.net.httpserver.HttpServer`
- SQLite，驱动 `sqlite-jdbc` 3.36.0.3（在 `lib/` 下）
- 手写递归下降解析器，不用 eval

## 目录

```
src/main/java/com/course/calculator/
├─ Main.java               入口、路由
├─ controller/             接口、静态文件
├─ service/                业务编排
├─ core/                   表达式解析、角度模式、异常
├─ db/                     数据库
├─ model/                  记录模型
└─ util/                   JSON
src/main/resources/webapp/ 打包进 jar 的前端
src/test/java/             测试
lib/                       SQLite 驱动
```

## 运行

Windows 下不需要 Maven：

```bat
build.bat
run.bat
```

默认 8080 端口，访问 http://localhost:8080。

用 Maven：

```bash
mvn package
java -jar target/calculator-backend.jar
```

用 Docker：

```bash
docker build -t calculator-backend .
docker run -p 8080:8080 calculator-backend
```

## 配置

| 环境变量 | 作用 | 默认值 |
|---|---|---|
| `PORT` | 端口（云平台会注入） | 8080 |
| `CALC_DB` | 数据库文件路径 | data/calculator.db |

## 数据库

首次启动自动建表，不用手动处理：

```sql
CREATE TABLE IF NOT EXISTS calculation_history (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    expression TEXT NOT NULL,
    result     TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

## 接口

| 方法 | 路径 | 说明 | 状态码 |
|---|---|---|---|
| GET | /api/health | 健康检查 | 200 |
| POST | /api/calculate | 计算并存历史 | 200 / 400 |
| GET | /api/history | 查全部历史 | 200 |
| DELETE | /api/history/{id} | 删一条 | 200 / 404 |
| DELETE | /api/history | 清空 | 200 |

请求：

```json
{ "expression": "(1+2)*3", "angleMode": "DEG" }
```

成功：

```json
{ "success": true, "expression": "(1+2)*3", "result": "9",
  "id": 1, "createdAt": "2026-10-05 11:20:00" }
```

失败：

```json
{ "success": false, "message": "除数不能为 0（位置 4）" }
```

## 和前端的连接

- 默认：前端文件放在 `src/main/resources/webapp/`，后端同源托管，
  前端 `js/config.js` 里 `API_BASE` 留空。
- 分开部署：`API_BASE` 填本服务地址，响应已带跨域头。
