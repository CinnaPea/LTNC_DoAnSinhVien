# Student Thesis Management System Demo

A class-project thesis management system built with **Spring Boot**, **Ruby on Rails API**, and **SQL Server**.

The project supports three main actors:

- **Admin**: manages student/instructor accounts and monitors academic data
- **Instructor**: creates topics, approves students, reviews progress/submissions, and evaluates theses
- **Student**: browses topics, registers, updates progress, submits work, and views results

## Architecture

This project uses a split architecture:

| Layer | Responsibility |
|---|---|
| Spring Boot | Login, session, dashboards, HTML pages, role-based UI |
| Ruby on Rails | Academic CRUD API and admin user API |
| SQL Server | Persistent data, ID counters, triggers, stored procedures |

Main idea:

```text
HTML Template -> Spring Controller -> Spring Service -> Rails API / SQL Repository -> SQL Server
```

## Main Features

### Authentication

- Login/logout
- Cookie-based session
- Role-based dashboard routing
- Student/instructor signup
- Inactive account blocking

### Admin

- View users
- Create student/instructor accounts
- Edit username, email, password, status, and profile name
- Delete accounts
- Monitor topics, registrations, and theses
- User list pagination: 5 users per page
- Hides raw `ND_ID` from table numbering

### Instructor

- Create, edit, and delete owned topics
- View topic list with pagination: 4 topics per page
- Approve/reject student registrations
- View supervised theses
- Review student progress
- Add progress feedback
- Review submissions
- Create/update/delete evaluations

### Student

- Browse open and closed topics
- Topic pagination: 4 topics per page
- Register for a topic
- Update/delete pending registration
- View thesis status
- Create/update/delete progress entries
- Create/update/delete submissions
- View final evaluation/result

## Project Structure

```text
final_webapp/
├── src/                         # Spring Boot application
│   └── main/
│       ├── java/com/webappfinal/final_webapp/
│       │   ├── controller/      # Web routes and page handlers
│       │   ├── service/         # Business logic and Rails API calls
│       │   ├── repository/      # SQL Server repositories
│       │   ├── entity/          # Java entity classes
│       │   ├── dto/             # Form, API, and view DTOs
│       │   ├── config/          # Spring configuration
│       │   ├── interceptor/     # Auth/error interceptors
│       │   └── util/            # Utility classes
│       └── resources/
│           ├── templates/       # Thymeleaf HTML pages
│           ├── static/          # CSS, JS, images, vendor assets
│           └── application.properties
│
├── ruby_api/                    # Ruby on Rails API
│   ├── app/controllers/api/     # API controllers
│   ├── app/models/              # Rails models
│   ├── config/routes.rb         # API routes
│   └── public/swagger/          # API documentation JSON
│
├── db/
│   └── QLDASV_UTF8.sql          # SQL Server schema, seed data, triggers, procedures
│
├── openapi.yaml                 # OpenAPI/Swagger documentation
├── PROJECT_ANALYSIS.md          # Detailed project notes and workflow explanation
├── pom.xml                      # Maven configuration
└── mvnw / mvnw.cmd              # Maven wrapper
```

## Database

The canonical database script is:

```text
db/QLDASV_UTF8.sql
```

It includes:

- Tables:
  - `IdCounter`
  - `VaiTro`
  - `NguoiDung`
  - `SinhVien`
  - `GiangVien`
  - `TheLoai`
  - `DangKy`
  - `DoAn`
  - `TienDo`
  - `BaiDang`
  - `DanhGia`

- Stored procedures:
  - `sp_CreateNguoiDung`
  - `sp_DeleteNguoiDung`

- Triggers:
  - `trg_CreateProfile`
  - `trg_DeleteNguoiDungCascade`

The database handles automatic profile creation from `NguoiDung` into `SinhVien` or `GiangVien`, and also handles account cleanup during deletion.

## Demo Workflow

Recommended demo path:

1. Admin logs in at `/login`
2. Admin opens `/admin/dashboard`
3. Admin creates or reviews student/instructor accounts
4. Instructor logs in
5. Instructor creates a topic at `/instructor/topics/new`
6. Student logs in
7. Student browses `/student/topics`
8. Student registers for a topic
9. Instructor approves the registration at `/instructor/students`
10. Student adds progress at `/student/progress`
11. Student submits work at `/student/submissions`
12. Instructor evaluates at `/instructor/evaluation`
13. Student views result at `/student/result`

## Important Routes

### Shared

```text
/
/login
/signup
/logout
/dashboard
```

### Admin

```text
/admin/dashboard
/admin/users/new
/admin/users/{ndId}/edit
/admin/topics
/admin/registrations
/admin/thesis
```

### Instructor

```text
/instructor/topics
/instructor/topics/new
/instructor/students
/instructor/thesis
/instructor/progress
/instructor/submissions
/instructor/evaluation
/instructor/profile
```

### Student

```text
/student/topics
/student/my-thesis
/student/progress
/student/submissions
/student/result
/student/profile
```

## Rails API Surface

The Rails API provides routes for:

```text
/api/admin/users
/api/the-loai
/api/dang-ky
/api/do-an
/api/tien-do
/api/bai-dang
/api/danh-gia
```

The OpenAPI specification is available in:

```text
openapi.yaml
```

## Running the Project

### Prerequisites

- Java 21+
- Maven
- Ruby
- Rails
- SQL Server
- SQL Server database created from `db/QLDASV_UTF8.sql`

### 1. Start Rails API

```bash
cd ruby_api
bundle install
ruby bin/rails server -p 3000
```

Rails API should run at:

```text
http://localhost:3000
```

### 2. Start Spring Boot

From the project root:

```bash
./mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

Spring Boot should run at:

```text
http://localhost:8080
```

## Testing

Spring tests:

```bash
mvn test
```

Current verified result:

```text
37 tests
0 failures
0 errors
```

Rails tests:

```bash
cd ruby_api
ruby bin/rails test
```

Current verified result:

```text
17 runs
0 failures
0 errors
17 skips
```

Rails integration tests may skip when SQL Server test connectivity is unavailable.

## Current Completion Status

For class-project/demo scope, this project is approximately:

```text
91% complete
```

Completed areas:

- Authentication/session
- Role dashboards
- Student workflow
- Instructor workflow
- Admin workflow
- Topic registration lifecycle
- Thesis progress/submission/evaluation lifecycle
- SQL trigger/procedure lifecycle
- Pagination improvements
- Swagger/OpenAPI cleanup

Remaining limitations:

- Not production-grade security
- Rails API has no standalone authentication layer
- Integration testing depends on SQL Server availability
- Deployment hardening is limited
- Some workflow assumptions are shared across Spring, Rails, and SQL Server

## Final Notes

This project is oriented toward a **user-facing academic workflow system**.

The main goal is not only to store data, but to let real actors perform their tasks:

- Admin prepares accounts
- Instructor creates and supervises thesis topics
- Student registers and works on a thesis
- Instructor reviews and evaluates
- Student receives the result

The system is suitable for a class-project showcase and demonstrates a complete multi-role thesis management workflow.
