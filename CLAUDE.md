# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RuoYi v3.9.2 — a Chinese rapid development admin framework with front/back separation. Backend is Spring Boot 4.0.3 (Java 17), frontend is Vue 2 + Element UI.

## Build & Run

**Backend (Maven):**
```bash
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar
```
No test suite exists; tests are always skipped during build.

**Frontend (pnpm):**
```bash
cd ruoyi-ui
pnpm install
pnpm run dev          # dev server on port 80, proxies /dev-api → localhost:8080
pnpm run build:prod   # production build → ruoyi-ui/dist/
```

**Server scripts:** `ry.sh start|stop|restart|status` (Linux) or `ry.bat` (Windows).

## Architecture

Six Maven modules with a layered dependency chain:

```
ruoyi-admin          → entry point, controllers, Spring Boot app
  ruoyi-framework    → security (JWT), AOP, config, interceptors
    ruoyi-system     → domain entities, mappers, services
      ruoyi-common   → base classes, utils, annotations, constants
  ruoyi-quartz       → scheduled task management
  ruoyi-generator    → code generation (Velocity templates)
```

### Key packages (base: `com.ruoyi`)

- `common.annotation` — `@Log` (audit logging), `@RateLimiter`, `@DataSource` (master/slave)
- `common.core.controller.BaseController` — base class for all controllers; provides `startPage()` pagination helper
- `common.core.domain.AjaxResult` — standard JSON response wrapper
- `common.core.page.TableDataInfo` — paginated list response wrapper
- `common.utils.SecurityUtils` — get current user, check permissions
- `framework.aspectj` — AOP aspects for data scope filtering, data source routing, operation logging, rate limiting
- `framework.security` — JWT filter, token handling, Spring Security config
- `system.service` — business logic layer (interfaces prefixed `ISys*`, impls prefixed `Sys*Impl`)

### Controller conventions

Controllers extend `BaseController`, use `@RestController`, and follow this pattern:
- `@PreAuthorize("@ss.hasPermi('module:entity:action')")` for permission checks
- `@Log(title = "...", businessType = BusinessType.INSERT/UPDATE/DELETE)` for audit logging
- Return `AjaxResult` for single objects, `TableDataInfo` for paginated lists
- Standard CRUD: `list`, `getInfo(id)`, `add`, `edit`, `remove(ids)`, `export`

### Frontend structure (`ruoyi-ui/src/`)

- `api/` — axios API functions mirroring backend endpoints
- `views/` — page components matching backend controllers
- `store/` — Vuex modules (user, permission, app, dict)
- `router/` — dynamic route loading from backend menu data
- `utils/request.js` — axios instance with JWT Bearer token injection, error handling, and duplicate submission prevention
- `utils/auth.js` — token management (cookie-based)
- `utils/ruoyi.js` — common utility functions
- `components/` — reusable components (pagination, CRUD helpers, tree select, etc.)
- `directive/` — custom Vue directives (permissions, clipboard, etc.)

## Configuration

- `ruoyi-admin/src/main/resources/application.yml` — main config (port 8080, Redis, token TTL, XSS filter)
- `ruoyi-admin/src/main/resources/application-druid.yml` — MySQL/Druid connection pool
- Profile upload path: `ruoyi.profile` in application.yml (default `D:/ruoyi/uploadPath`)
- Frontend proxies `/dev-api` to `localhost:8080` via `vue.config.js`

## Database

MySQL (`ry-vue` database). Schema scripts in `/sql/ry_20260321.sql` and `/sql/quartz.sql`. MyBatis mapper XML files live at `ruoyi-*/src/main/resources/mapper/**/*.xml`.

## Common patterns

- **Pagination:** In controller, call `startPage()` before the service query, then wrap result with `getDataTable(list)`
- **Data scope:** Annotate service methods with `@DataScope` to auto-filter by department/data permissions
- **Code generation:** Use `ruoyi-generator` module; Velocity templates in `ruoyi-generator/src/main/resources/vm/` generate Java, Vue, and SQL files
- **i18n:** Resource bundles in `ruoyi-common/src/main/resources/i18n/messages*.properties`
- **Swagger:** Enabled via SpringDoc at `/swagger-ui.html` (development only by default)
