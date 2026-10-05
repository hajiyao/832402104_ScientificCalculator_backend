# Calculator Backend

The back end of a front-end/back-end separated calculator system. It receives an expression
from the front end, validates, parses, and evaluates it, stores every successful result in
SQLite, and provides history query and delete endpoints. On startup it also serves the
front-end pages, so one URL is the whole website.

## Tech stack

- Java 8
- JDK built-in `com.sun.net.httpserver.HttpServer`
- SQLite via `sqlite-jdbc` 3.36.0.3 (in `lib/`)
- Hand-written recursive-descent parser — no `eval`

## Layout

```text
src/main/java/com/course/calculator/
├─ Main.java               entry point, routing
├─ controller/             HTTP endpoints, static files
├─ service/                 business orchestration
├─ core/                   expression parser, angle mode, exceptions
├─ db/                      database
├─ model/                  record model
└─ util/                   JSON helpers
src/main/resources/webapp/  front-end files packaged into the jar
src/test/java/             tests
lib/                        SQLite driver
```

## Run

No Maven needed on Windows:

```bat
build.bat
run.bat
```

Defaults to port 8080, then open http://localhost:8080.

With Maven:

```bash
mvn package
java -jar target/calculator-backend.jar
```

With Docker:

```bash
docker build -t calculator-backend .
docker run -p 8080:8080 calculator-backend
```

## Configuration

| Env variable | Purpose | Default |
|---|---|---|
| `PORT` | Listening port (injected by the cloud platform) | 8080 |
| `CALC_DB` | SQLite database file path | data/calculator.db |

## Database

The table is created automatically on first startup; no manual setup:

```sql
CREATE TABLE IF NOT EXISTS calculation_history (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    expression TEXT NOT NULL,
    result     TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

## API

| Method | Path | Description | Status code |
|---|---|---|---|
| GET | /api/health | Health check | 200 |
| POST | /api/calculate | Evaluate and save to history | 200 / 400 |
| GET | /api/history | List all history | 200 |
| DELETE | /api/history/{id} | Delete one record | 200 / 404 |
| DELETE | /api/history | Clear all history | 200 |

Request:

```json
{ "expression": "(1+2)*3", "angleMode": "DEG" }
```

Success:

```json
{ "success": true, "expression": "(1+2)*3", "result": "9",
  "id": 1, "createdAt": "2026-10-05 11:20:00" }
```

Error:

```json
{ "success": false, "message": "除数不能为 0（位置 4）" }
```

## Connecting the front end

- Default: the front-end files live in `src/main/resources/webapp/` and are served
  same-origin by the back end, so `API_BASE` in `js/config.js` stays empty.
- Separate deployment: set `API_BASE` to this service's address. Responses already carry
  CORS headers.
