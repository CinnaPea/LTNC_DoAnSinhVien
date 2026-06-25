# Project Assessment

**Date:** June 26, 2026  
**Project:** `final_webapp`  
**Architecture:** Spring Boot UI/Auth + Rails API + SQL Server

## Executive Summary

The project is in a strong class-project showcase state and covers the full intended academic workflow surface:

- Spring Boot owns login, session, page rendering, dashboards, profile pages, and feature screens.
- Rails owns the academic and admin CRUD APIs.
- SQL Server is the single source of truth and also owns part of the lifecycle logic through triggers and stored procedures.

The project is no longer blocked by missing major modules. The main remaining risk is not feature absence; it is integration confidence across Spring, Rails, and SQL Server during a live demo.

## Current Sanity Check

Verified from the current combined workspace on June 26, 2026:

- Spring routes exist for login, signup, dashboards, student workflows, instructor workflows, admin views, and profile deletion.
- Student workflow routes cover topics, registration, thesis, progress, submissions, and result viewing.
- Instructor workflow routes cover topics, student approvals, thesis list, progress feedback, submissions, and evaluation CRUD.
- Admin workflow routes cover dashboard, topics, registrations, theses, user create/edit/status/delete.
- `/admin/dashboard` user list is paginated at 5 users per page and shows a normal row number instead of exposing `ND_ID` as the visible table index.
- `/instructor/topics` topic lists are paginated at 4 topics per page for both managed/open topics and completed/closed topics.
- `/student/topics` topic lists are paginated at 4 topics per page for both open and closed topic sections.
- `openapi.yaml` currently declares OpenAPI `3.0.0` and has had duplicate YAML response keys removed.
- Rails API source is included in this repository under `ruby_api/`.
- SQL Server bootstrap script is included at `db/QLDASV_UTF8.sql`, including the profile-creation trigger and delete cascade procedure/trigger.
- The Spring project still depends on Rails API and SQL Server being reachable for a full live end-to-end demo.

## Current Architecture

### Spring Boot owns

- login/logout
- cookie session
- dashboard routing by role
- profile pages
- student, instructor, and admin HTML screens
- Rails API consumption
- self-delete entrypoints for student and instructor profiles

### Rails owns

- `TheLoai` CRUD
- `DangKy` CRUD plus approve/reject workflow
- `DoAn` lifecycle entrypoint from approved registrations
- `TienDo` CRUD
- `BaiDang` CRUD
- `DanhGia` CRUD
- admin user CRUD API
- API-side validation and workflow rules

### SQL Server owns

- persistent data
- `IdCounter`
- insert trigger for `NguoiDung -> SinhVien/GiangVien`
- delete procedure + delete trigger for account cleanup cascade

## Module Status

### 1. Authentication and Session

**Status:** Done for class-project scope

Implemented:

- DB-backed login against `NguoiDung`
- cookie-backed session
- logout
- signup for `SV` and `GV`
- admin role recognition
- inactive-account blocking

Known limits:

- simple session model
- no separate Rails auth layer
- no password reset or audit workflow

### 2. Student Workflow

**Status:** Done for intended scope

Implemented:

- browse open and closed topics
- view topic details
- create registration
- update/delete own pending registration
- view own thesis records
- full `TienDo` create/update/delete flow
- full `BaiDang` create/update/delete flow
- read evaluation/result page
- self-delete from profile page

### 3. Instructor Workflow

**Status:** Done for intended scope

Implemented:

- create/edit/delete own topics
- review registrations for owned topics
- approve/reject registrations
- view supervised theses
- review progress entries and add feedback
- review student submissions
- create/update/delete evaluations
- self-delete from profile page
- paginated topic lists on `/instructor/topics`, 4 topics per page for each section

### 4. Admin Workflow

**Status:** Done for intended scope

Implemented:

- list academic users
- filter practical scope to `SV` and `GV`
- create `NguoiDung` as `SV` or `GV`
- update username/email/password/status/profile name
- delete account through Rails API
- rely on SQL-side delete cascade for linked academic data
- paginated user list on `/admin/dashboard`, 5 users per page
- visible user numbering uses a standard row index instead of showing `ND_ID`

Explicitly not supported:

- multi-admin management
- promoting another user to admin
- role switching across `SV`, `GV`, `AD`

### 5. Academic Domain

#### `TheLoai`

**Status:** Done

- Rails owns validation and persistence
- Spring instructor pages perform create/update/delete
- Spring student pages perform list/detail

#### `DangKy`

**Status:** Done

- student create/read/update/delete for pending registrations
- instructor approve/reject
- Rails enforces duplicate prevention and open-topic checks

#### `DoAn`

**Status:** Done for current scope

- created automatically after registration approval
- listed for student and instructor
- thesis state is tied to evaluation workflow

#### `TienDo`

**Status:** Done

- Rails CRUD API exists
- student can create/update/delete own progress entries
- instructor can review and add feedback

#### `BaiDang`

**Status:** Done

- Rails CRUD API exists
- student can create/update/delete submissions
- instructor can review submissions

#### `DanhGia`

**Status:** Done

- Rails CRUD API exists
- instructor can create/update/delete evaluation
- student can read results
- creating/updating evaluation marks thesis complete
- deleting evaluation reopens thesis

## SQL Lifecycle Logic

### Insert path

- Spring signup and Rails admin-create both insert `NguoiDung`
- SQL trigger creates the linked `SinhVien` or `GiangVien`
- application code then works against the linked role row

### Delete path

- admin delete goes through Rails
- self-delete goes through Spring profile flow
- both converge on SQL delete logic
- `sp_DeleteNguoiDung` deletes from `NguoiDung`
- SQL `INSTEAD OF DELETE` trigger performs the cascade cleanup

Current delete cascade covers:

- `SinhVien`
- `GiangVien`
- `TheLoai`
- `DangKy`
- `DoAn`
- `TienDo`
- `BaiDang`
- `DanhGia`

`IdCounter` is intentionally not decremented on delete.

## TinyTDS / SQL Server Stability

**Status:** Safer than before, still environment-dependent

Implemented:

- Rails API now rescues SQL Server connection failures and returns controlled `503` JSON
- Rails test environment skips integration tests cleanly when SQL Server is unreachable
- SQL Server adapter config now has explicit pool, login timeout, and retry-related settings

Important limitation:

- This does not remove dependency on SQL Server availability
- It only turns a hard crash into controlled degradation

## Testing Status

### Verified passing

- Spring `mvn test` passes
- Spring role and workflow pages compile and run
- Rails route registration is present for all current modules
- Rails syntax checks pass for the new models/controllers/tests
- Swagger/OpenAPI file parses as valid OpenAPI `3.0.0`

### Added coverage

Rails integration tests now exist for:

- `TienDo`
- `BaiDang`
- `DanhGia`
- account delete cascade behavior

### Remaining limitation

Rails integration tests are present but not always executable in this environment because SQL Server test connectivity to `PEANUT\\SQLEXPRESS` can fail. After the TinyTDS hardening, those tests now skip cleanly instead of crashing.

## Security Assessment

**Status:** Functional, not production-grade

Reasonable for this project:

- role-gated pages
- blocked inactive accounts
- admin scope kept intentionally narrow
- no direct browser-to-Rails auth exposure

Still weak for production:

- Rails API has no standalone auth layer
- mixed password handling history exists
- trigger/procedure behavior is trusted heavily
- no serious audit logging
- no formal CSRF/security review of the split architecture

## Main Risks

### Shared-schema coupling

Spring, Rails, triggers, and procedures all depend on one SQL Server schema. Any schema drift can break multiple layers at once.

### Contract drift

Spring depends on Rails response shapes and workflow behavior. The riskiest boundaries are better than before, but still not fully contract-tested end to end.

### Database-side behavior

Part of the real application now lives in SQL:

- profile creation trigger
- delete cascade trigger
- delete stored procedure

That is acceptable for this project, but it raises maintenance cost.

### Environment sensitivity

The project is usable only when:

- Spring is up
- Rails is up
- SQL Server is reachable

The TinyTDS changes improve failure handling, but do not remove that dependency.

## Re-Evaluation of Prior Concerns

### "Rails side is required but not present here"

No longer true for the final GitHub snapshot.

- The Rails API source is now included under `ruby_api/`.
- Full end-to-end proof still requires Spring, Rails, and SQL Server together.

### "Thesis/progress/evaluation/reporting are still mostly read-only or absent"

No longer true.

- `DoAn`, `TienDo`, `BaiDang`, and `DanhGia` now have real user workflows.

### "Security is functional but not production-grade"

Still true.

### "Tests pass, but they do not cover the riskiest areas"

Partially true.

- Coverage is better now because Rails integration tests for the missing modules and delete cascade were added.
- The biggest remaining weakness is environment-dependent execution, not complete absence of tests.

### "The architecture still has cross-database consistency risk"

The more accurate description remains:

- not cross-database risk
- shared-schema / split-ownership risk

Both apps use the same SQL Server database.

## Demo Showcase Workflow and Routes

This is the recommended live demo order. It tells one coherent story: admin prepares accounts, instructor creates the academic opportunity, student joins and works, instructor reviews and grades, student sees the result.

### Demo prerequisites

- SQL Server database `DoAnSinhVien` is reachable.
- Rails API is running on `http://localhost:3000`.
- Spring Boot is running on `http://localhost:8080`.
- Demo accounts exist for at least one admin, one instructor, and one student.
- Use simple test records so deletes/updates are safe during the presentation.

### Shared entry routes

- `/` routes authenticated users to `/dashboard`, otherwise to `/login`.
- `/login` authenticates user credentials.
- `/signup` creates a student or instructor account.
- `/logout` clears the login cookie.
- `/dashboard` sends the user to the correct role dashboard.

### Admin actor workflow

Purpose: show that the system can manage accounts and monitor the academic data surface.

1. Log in as admin at `/login`.
2. Open `/admin/dashboard` and show academic users.
3. Use the dashboard pagination to show that the user list displays 5 accounts per page with normal row numbers.
4. Open `/admin/users/new` and create a student or instructor.
5. Open `/admin/users/{ndId}/edit` and update a user.
6. Use `POST /admin/users/{ndId}/status/{active}` to activate or deactivate a user.
7. Open `/admin/topics` to inspect topics.
8. Open `/admin/registrations` to inspect student registrations.
9. Open `/admin/thesis` to inspect thesis records.
10. Use `POST /admin/users/{ndId}/delete` only on a disposable demo account.

Showcase line: Admin controls the people and can inspect the global academic state, while SQL Server handles linked profile creation and delete cleanup.

### Instructor actor workflow

Purpose: show that instructors own topic creation, student approval, review, and final evaluation.

1. Log in as instructor at `/login`.
2. Open `/instructor/topics`.
3. Use the pagination controls to show 4 topics per page in each topic section.
4. Open `/instructor/topics/new` and create a topic.
5. Open `/instructor/topics/{tlId}/edit` if you want to show editing.
6. After a student registers, open `/instructor/students`.
7. Use `POST /instructor/students/{dkId}/approve` to approve the registration.
8. Open `/instructor/thesis` to show supervised thesis records.
9. Open `/instructor/progress` and add feedback with `POST /instructor/progress/{tdId}/feedback`.
10. Open `/instructor/submissions` to review student submissions.
11. Open `/instructor/evaluation`.
12. Use `POST /instructor/evaluation/{daId}` to create an evaluation.
13. Optionally use `POST /instructor/evaluation/{dgId}/update` or `/delete` to show evaluation lifecycle.

Showcase line: The instructor turns a topic into a supervised thesis by approving the student, then closes the loop with feedback and evaluation.

### Student actor workflow

Purpose: show the student journey from choosing a topic to seeing the final result.

1. Log in as student at `/login`.
2. Open `/student/topics` and browse open/closed topics.
3. Open `/student/topics/{tlId}` to inspect a topic.
4. Use `POST /student/topics/{tlId}/register` to register.
5. Open `/student/my-thesis` to show registration and thesis state.
6. If registration is pending, use `POST /student/registrations/{dkId}/update` or `/delete`.
7. After instructor approval, open `/student/progress`.
8. Use `POST /student/progress/{daId}` to create a progress entry.
9. Use `POST /student/progress/{tdId}/update` or `/delete` if needed.
10. Open `/student/submissions`.
11. Use `POST /student/submissions/{daId}` to create a submission.
12. Use `POST /student/submissions/{bdId}/update` or `/delete` if needed.
13. Open `/student/result` to show instructor evaluation.

Showcase line: The student registers, works through progress and submissions, then reads the result after the instructor evaluates the thesis.

### Fastest happy-path demo

Use this if time is short:

1. Admin logs in and shows `/admin/dashboard`.
2. Instructor logs in and creates a topic at `/instructor/topics/new`.
3. Student logs in and registers from `/student/topics`.
4. Instructor approves from `/instructor/students`.
5. Student adds progress at `/student/progress`.
6. Student adds submission at `/student/submissions`.
7. Instructor reviews and evaluates from `/instructor/evaluation`.
8. Student opens `/student/result`.

This path demonstrates all three actors and the full thesis lifecycle with minimal switching.

## End-to-End Workflow File Map

This section explains the whole system from start to finish and names the exact files involved at each step. Spring files render pages, receive forms, call Rails, and read SQL Server directly where needed. Rails API files live under `ruby_api/`.

### 0. Application startup

What happens:

1. The Spring Boot application starts.
2. SQL Server native authentication support is loaded.
3. Spring registers controllers, services, repositories, interceptors, and templates.
4. The app becomes available at `http://localhost:8080`.

Main files:

- `src/main/java/com/webappfinal/final_webapp/FinalWebappApplication.java`: Spring Boot entry point.
- `src/main/java/com/webappfinal/final_webapp/util/SqlServerNativeAuthLoader.java`: loads SQL Server native authentication DLL.
- `src/main/java/com/webappfinal/final_webapp/config/SqlServerNativeAuthEnvironmentPostProcessor.java`: prepares native auth environment early.
- `src/main/java/com/webappfinal/final_webapp/config/WebConfig.java`: registers web configuration and interceptors.
- `src/main/java/com/webappfinal/final_webapp/config/PasswordConfig.java`: provides password hashing support.
- `src/main/resources/application.properties`: database URL, Rails API URL, demo auth settings.
- `db/QLDASV_UTF8.sql`: SQL Server schema, seed data, ID counters, profile trigger, and delete cascade support.
- `ruby_api/config/routes.rb`: Rails API route map.
- `ruby_api/config/database.yml`: Rails database connection configuration.
- `pom.xml`: Maven dependencies and build plugins.

### 1. User enters the system

What actor does:

1. Any actor opens `/`.
2. If not logged in, the app sends them to `/login`.
3. User enters username and password.
4. App validates credentials against SQL Server.
5. App writes a `loggedInUser` cookie.
6. App redirects to `/dashboard`.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/HomeController.java`: handles `/`.
- `src/main/java/com/webappfinal/final_webapp/controller/AuthController.java`: handles `/login`, `/signup`, `/logout`.
- `src/main/java/com/webappfinal/final_webapp/service/AuthSessionService.java`: reads/writes the login cookie and checks demo/database credentials.
- `src/main/java/com/webappfinal/final_webapp/service/DatabaseAuthenticationService.java`: validates username/password against `NguoiDung`.
- `src/main/java/com/webappfinal/final_webapp/util/CookieUtility.java`: helper for cookie operations.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: loads the user account.
- `src/main/java/com/webappfinal/final_webapp/entity/NguoiDung.java`: maps the user table.
- `src/main/resources/templates/auth/login.html`: login screen.
- `src/main/resources/templates/auth/signup.html`: signup screen.

### 2. New account signup

What actor does:

1. Student or instructor opens `/signup`.
2. They fill in account information.
3. Spring validates the form.
4. Spring inserts `NguoiDung`.
5. SQL Server trigger creates `SinhVien` or `GiangVien`.
6. User returns to login.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/AuthController.java`: receives signup form.
- `src/main/java/com/webappfinal/final_webapp/service/SignupService.java`: validates signup and creates account/profile records.
- `src/main/java/com/webappfinal/final_webapp/service/IdGenerationService.java`: creates IDs such as `ND0001`, `SV0001`, `GV0001`.
- `src/main/java/com/webappfinal/final_webapp/dto/SignupForm.java`: carries signup form data.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: saves `NguoiDung`.
- `src/main/java/com/webappfinal/final_webapp/repository/SinhVienRepository.java`: saves or reads student profile.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: saves or reads instructor profile.
- `src/main/java/com/webappfinal/final_webapp/repository/VaiTroRepository.java`: checks that role exists.
- `src/main/java/com/webappfinal/final_webapp/entity/NguoiDung.java`: account entity.
- `src/main/java/com/webappfinal/final_webapp/entity/SinhVien.java`: student entity.
- `src/main/java/com/webappfinal/final_webapp/entity/GiangVien.java`: instructor entity.
- `src/main/java/com/webappfinal/final_webapp/entity/VaiTro.java`: role entity.
- `src/main/resources/templates/auth/signup.html`: signup form.

### 3. Dashboard routing

What actor does:

1. Logged-in user opens `/dashboard`.
2. Spring reads username from cookie.
3. Spring loads user role from SQL Server.
4. User is sent to the correct dashboard.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/DashboardController.java`: handles `/dashboard`.
- `src/main/java/com/webappfinal/final_webapp/service/DashboardService.java`: builds dashboard user data.
- `src/main/java/com/webappfinal/final_webapp/service/AuthSessionService.java`: reads logged-in username.
- `src/main/java/com/webappfinal/final_webapp/dto/DashboardUserDTO.java`: dashboard display data.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: loads account/role.
- `src/main/java/com/webappfinal/final_webapp/repository/SinhVienRepository.java`: loads student details.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: loads instructor details.
- `src/main/resources/templates/student/dashboard.html`: student dashboard.
- `src/main/resources/templates/instructor/dashboard.html`: instructor dashboard.
- `src/main/resources/templates/admin/dashboard.html`: admin dashboard.
- `src/main/resources/templates/fragments/student-sidebar.html`: student navigation.
- `src/main/resources/templates/fragments/instructor-sidebar.html`: instructor navigation.
- `src/main/resources/templates/fragments/admin-sidebar.html`: admin navigation.

### 4. Admin manages users and monitors data

What admin does:

1. Admin opens `/admin/dashboard`.
2. Admin views academic users in a paginated table, 5 users per page.
3. Admin creates a student/instructor.
4. Admin edits username, email, password, status, or profile name.
5. Admin opens topics, registrations, and thesis pages for monitoring.
6. Admin can delete a disposable account.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/AdminController.java`: all admin routes.
- `src/main/java/com/webappfinal/final_webapp/service/AdminUserApiService.java`: calls Rails admin user API.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: loads topics for admin topic view.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: loads registrations for admin registration view.
- `src/main/java/com/webappfinal/final_webapp/service/DoAnApiService.java`: loads theses for admin thesis view.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminUserForm.java`: admin create/edit form data.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminUserApiItem.java`: one user returned from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminUserCatalogView.java`: user list plus API availability state.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminTopicView.java`: topic display data for admin.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminRegistrationView.java`: registration display data for admin.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminThesisView.java`: thesis display data for admin.
- `src/main/resources/templates/admin/dashboard.html`: admin dashboard/user list, including 5-per-page pagination and visible row numbering.
- `src/main/resources/templates/admin/user-form.html`: create/edit user form.
- `src/main/resources/templates/admin/topics.html`: admin topic monitor.
- `src/main/resources/templates/admin/registrations.html`: admin registration monitor.
- `src/main/resources/templates/admin/thesis.html`: admin thesis monitor.

### 5. Instructor creates and manages topics

What instructor does:

1. Instructor opens `/instructor/topics`.
2. Instructor uses topic pagination, 4 topics per page for each section.
3. Instructor opens `/instructor/topics/new`.
4. Instructor creates topic.
5. Instructor can edit or delete own topic.
6. Rails stores the topic and enforces API-side rules.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: instructor topic routes.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: calls Rails topic API.
- `src/main/java/com/webappfinal/final_webapp/dto/TheLoaiForm.java`: topic form data.
- `src/main/java/com/webappfinal/final_webapp/dto/TheLoaiApiItem.java`: one topic from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/TheLoaiCatalogView.java`: list of topics plus API availability.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: finds logged-in account.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: translates account to instructor profile.
- `src/main/java/com/webappfinal/final_webapp/entity/GiangVien.java`: instructor profile entity.
- `src/main/resources/templates/instructor/topics.html`: instructor topic list, including 4-per-page pagination for managed and completed topics.
- `src/main/resources/templates/instructor/topic-form.html`: create/edit topic form.

### 6. Student browses topics and registers

What student does:

1. Student opens `/student/topics`.
2. Student uses topic pagination, 4 topics per page for open and closed sections.
3. Student opens `/student/topics/{tlId}`.
4. Student submits registration.
5. Spring checks current student identity.
6. Spring calls Rails to create `DangKy`.
7. Rails prevents invalid duplicate/closed-topic registrations.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/StudentFeatureController.java`: student topic/detail/register routes.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: fetches topics and topic detail from Rails.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: creates and reads registrations through Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/TheLoaiApiItem.java`: topic returned by Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/TopicDetailView.java`: topic detail prepared for screen display.
- `src/main/java/com/webappfinal/final_webapp/dto/DangKyForm.java`: registration note form.
- `src/main/java/com/webappfinal/final_webapp/dto/DangKyApiItem.java`: one registration from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/DangKyCatalogView.java`: registration list plus API availability.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: finds logged-in account.
- `src/main/java/com/webappfinal/final_webapp/repository/SinhVienRepository.java`: translates account to student profile.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: loads instructor owner for topic display.
- `src/main/java/com/webappfinal/final_webapp/entity/SinhVien.java`: student profile entity.
- `src/main/resources/templates/student/topics.html`: topic list, including 4-per-page pagination for open and closed topics.
- `src/main/resources/templates/student/topic-detail.html`: topic detail/register screen.
- `ruby_api/app/controllers/api/the_loai_controller.rb`: Rails topic API.
- `ruby_api/app/controllers/api/dang_ky_controller.rb`: Rails registration API.
- `ruby_api/app/models/the_loai.rb`: Rails topic model and topic ID allocation.
- `ruby_api/app/models/dang_ky.rb`: Rails registration model and registration rules.

### 7. Instructor approves or rejects registrations

What instructor does:

1. Instructor opens `/instructor/students`.
2. Instructor reviews registrations for owned topics.
3. Instructor approves or rejects registration.
4. Rails updates `DangKy`.
5. Approved registration leads into the thesis lifecycle.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: `/instructor/students`, approve, reject.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: fetches registrations and sends approve/reject decisions to Rails.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: loads instructor topics for matching registrations.
- `src/main/java/com/webappfinal/final_webapp/dto/DangKyApiItem.java`: registration item.
- `src/main/java/com/webappfinal/final_webapp/dto/DangKyCatalogView.java`: registration catalog.
- `src/main/java/com/webappfinal/final_webapp/dto/InstructorRegistrationView.java`: combines registration, topic, and student for display.
- `src/main/java/com/webappfinal/final_webapp/repository/SinhVienRepository.java`: loads student profile for each registration.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: confirms instructor identity.
- `src/main/resources/templates/instructor/students.html`: registration approval page.

### 8. Thesis records become visible

What actors do:

1. Student opens `/student/my-thesis`.
2. Instructor opens `/instructor/thesis`.
3. Admin opens `/admin/thesis`.
4. Spring asks Rails for thesis records.
5. Spring joins thesis data with registration, topic, student, or instructor data for screen display.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/StudentFeatureController.java`: student thesis page.
- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: instructor thesis page.
- `src/main/java/com/webappfinal/final_webapp/controller/AdminController.java`: admin thesis monitor.
- `src/main/java/com/webappfinal/final_webapp/service/DoAnApiService.java`: fetches thesis data from Rails.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: fetches registrations related to theses.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: fetches topics related to theses.
- `src/main/java/com/webappfinal/final_webapp/dto/DoAnApiItem.java`: one thesis from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/DoAnCatalogView.java`: thesis list plus API availability.
- `src/main/java/com/webappfinal/final_webapp/dto/StudentThesisView.java`: student thesis display model.
- `src/main/java/com/webappfinal/final_webapp/dto/InstructorThesisView.java`: instructor thesis display model.
- `src/main/java/com/webappfinal/final_webapp/dto/AdminThesisView.java`: admin thesis display model.
- `src/main/resources/templates/student/my-thesis.html`: student thesis/registration screen.
- `src/main/resources/templates/instructor/thesis.html`: instructor supervised thesis screen.
- `src/main/resources/templates/admin/thesis.html`: admin thesis monitor.

### 9. Student updates progress, instructor gives feedback

What actors do:

1. Student opens `/student/progress`.
2. Student creates, updates, or deletes progress entries.
3. Instructor opens `/instructor/progress`.
4. Instructor reviews entries and adds feedback.
5. Rails stores `TienDo` records and feedback.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/StudentFeatureController.java`: student progress routes.
- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: instructor progress/feedback routes.
- `src/main/java/com/webappfinal/final_webapp/service/TienDoApiService.java`: calls Rails progress API.
- `src/main/java/com/webappfinal/final_webapp/service/DoAnApiService.java`: loads related thesis records.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: connects thesis to registration.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: connects registration to topic.
- `src/main/java/com/webappfinal/final_webapp/dto/TienDoForm.java`: progress form data.
- `src/main/java/com/webappfinal/final_webapp/dto/TienDoApiItem.java`: one progress entry from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/TienDoCatalogView.java`: progress list plus API availability.
- `src/main/java/com/webappfinal/final_webapp/dto/StudentProgressView.java`: student progress display model.
- `src/main/java/com/webappfinal/final_webapp/dto/InstructorProgressView.java`: instructor progress display model.
- `src/main/resources/templates/student/progress.html`: student progress page.
- `src/main/resources/templates/instructor/progress.html`: instructor progress feedback page.

### 10. Student submits work, instructor reviews submissions

What actors do:

1. Student opens `/student/submissions`.
2. Student creates, updates, or deletes a submission/post.
3. Instructor opens `/instructor/submissions`.
4. Instructor reviews submitted work.
5. Rails stores `BaiDang` records.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/StudentFeatureController.java`: student submission routes.
- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: instructor submission review route.
- `src/main/java/com/webappfinal/final_webapp/service/BaiDangApiService.java`: calls Rails submission/post API.
- `src/main/java/com/webappfinal/final_webapp/service/DoAnApiService.java`: loads thesis context.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: connects thesis to registration.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: connects registration to topic.
- `src/main/java/com/webappfinal/final_webapp/dto/BaiDangForm.java`: submission form data.
- `src/main/java/com/webappfinal/final_webapp/dto/BaiDangApiItem.java`: one submission from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/BaiDangCatalogView.java`: submission list plus API availability.
- `src/main/java/com/webappfinal/final_webapp/dto/StudentSubmissionView.java`: student submission display model.
- `src/main/java/com/webappfinal/final_webapp/dto/InstructorSubmissionView.java`: instructor submission display model.
- `src/main/resources/templates/student/submissions.html`: student submission page.
- `src/main/resources/templates/instructor/submissions.html`: instructor review page.

### 11. Instructor evaluates thesis, student views result

What actors do:

1. Instructor opens `/instructor/evaluation`.
2. Instructor creates, updates, or deletes evaluation.
3. Rails stores `DanhGia`.
4. Evaluation affects thesis completion state.
5. Student opens `/student/result`.
6. Student reads grade/evaluation.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/InstructorFeatureController.java`: instructor evaluation routes.
- `src/main/java/com/webappfinal/final_webapp/controller/StudentFeatureController.java`: student result route.
- `src/main/java/com/webappfinal/final_webapp/service/DanhGiaApiService.java`: calls Rails evaluation API.
- `src/main/java/com/webappfinal/final_webapp/service/DoAnApiService.java`: loads thesis context.
- `src/main/java/com/webappfinal/final_webapp/service/DangKyApiService.java`: connects thesis to registration.
- `src/main/java/com/webappfinal/final_webapp/service/TheLoaiApiService.java`: connects registration to topic.
- `src/main/java/com/webappfinal/final_webapp/dto/DanhGiaForm.java`: evaluation form data.
- `src/main/java/com/webappfinal/final_webapp/dto/DanhGiaApiItem.java`: one evaluation from Rails.
- `src/main/java/com/webappfinal/final_webapp/dto/DanhGiaCatalogView.java`: evaluation list plus API availability.
- `src/main/java/com/webappfinal/final_webapp/dto/InstructorEvaluationView.java`: instructor evaluation display model.
- `src/main/java/com/webappfinal/final_webapp/dto/StudentEvaluationView.java`: student result display model.
- `src/main/resources/templates/instructor/evaluation.html`: instructor grading page.
- `src/main/resources/templates/student/result.html`: student result page.

### 12. Profile update and self-delete

What actors do:

1. Student opens `/student/profile`.
2. Instructor opens `/instructor/profile`.
3. User updates profile data.
4. User may self-delete their account.
5. SQL delete procedure/trigger performs cleanup.

Main files:

- `src/main/java/com/webappfinal/final_webapp/controller/ProfileController.java`: profile view, update, and delete routes.
- `src/main/java/com/webappfinal/final_webapp/service/ProfileService.java`: reads/updates/deletes account profile.
- `src/main/java/com/webappfinal/final_webapp/dto/UserProfileForm.java`: profile form data.
- `src/main/java/com/webappfinal/final_webapp/repository/NguoiDungRepository.java`: account persistence.
- `src/main/java/com/webappfinal/final_webapp/repository/SinhVienRepository.java`: student profile persistence.
- `src/main/java/com/webappfinal/final_webapp/repository/GiangVienRepository.java`: instructor profile persistence.
- `src/main/java/com/webappfinal/final_webapp/entity/NguoiDung.java`: account entity.
- `src/main/java/com/webappfinal/final_webapp/entity/SinhVien.java`: student entity.
- `src/main/java/com/webappfinal/final_webapp/entity/GiangVien.java`: instructor entity.
- `src/main/resources/templates/student/profile.html`: student profile page.
- `src/main/resources/templates/instructor/profile.html`: instructor profile page.
- `db/QLDASV_UTF8.sql`: canonical SQL script containing schema, profile trigger, and delete cascade support.

### 13. Error and layout support files

These files support the whole workflow rather than one actor only:

- `src/main/java/com/webappfinal/final_webapp/controller/ErrorController.java`: test/simple error route.
- `src/main/java/com/webappfinal/final_webapp/interceptor/AuthInterceptor.java`: protects routes by checking authentication.
- `src/main/java/com/webappfinal/final_webapp/interceptor/GlobalErrorInterceptor.java`: shared error handling.
- `src/main/resources/templates/common/error.html`: error screen.
- `src/main/resources/templates/fragments/head.html`: shared HTML head.
- `src/main/resources/templates/fragments/scripts.html`: shared scripts.
- `src/main/resources/static/css/main.css`: common styling.
- `src/main/resources/static/css/layout.css`: layout styling.
- `src/main/resources/static/css/auth.css`: login/signup styling.
- `src/main/resources/static/js/main.js`: browser-side helper behavior.

### One-line mental model

Every feature follows this rhythm:

`HTML template -> Controller -> Service -> Rails API or SQL Repository -> DTO/View model -> HTML template`

For example, student registration is:

`student/topic-detail.html -> StudentFeatureController.java -> DangKyApiService.java -> Rails /api/dang-ky -> DangKyApiItem.java/DangKyCatalogView.java -> student/my-thesis.html`

## Completion Estimate

### By module

- Authentication/session/dashboard/profile: **92%**
- `TheLoai`: **95%**
- `DangKy`: **95%**
- `DoAn`: **88%**
- `TienDo`: **90%**
- `BaiDang`: **90%**
- `DanhGia`: **90%**
- Admin CRUD: **92%**
- Delete lifecycle and self-delete: **88%**
- Test/integration reliability: **72%**

### Overall project

**Current overall completion: 91% for class-project/demo scope**

That number reflects:

- all major role workflows implemented
- academic CRUD surface complete for class-project scope
- admin CRUD implemented
- demo usability improved with pagination on admin users, instructor topics, and student topics
- SQL lifecycle logic implemented
- remaining gaps concentrated in hardening, execution reliability, security depth, and integration confidence

For production-grade use, the practical rating would be lower, around **70-75%**, because Rails authentication, formal contract tests, deployment hardening, audit logging, and repeatable integration-test infrastructure are still limited.

## What Still Remains

### Highest priority

1. Run the Rails integration suite successfully against a stable SQL Server test database.
2. Add more explicit Spring-to-Rails contract tests for the riskiest write flows.
3. Normalize password handling so signup and admin-created accounts follow one policy.

### Lower priority

1. Add a DB reachability endpoint distinct from Rails process health.
2. Improve API documentation maintenance discipline.
3. Reduce duplicated assumptions across Spring and Rails where possible.

## Recommended Ownership Rules

To keep the project stable, these rules should stay true:

- Spring owns:
  - login
  - session
  - dashboards
  - profile pages
  - HTML rendering

- Rails owns:
  - academic CRUD/workflow rules
  - admin user CRUD API

- SQL Server owns:
  - persistence
  - ID counters
  - explicitly DB-side lifecycle logic

## Final Verdict

The project is no longer missing its core thesis lifecycle. It now demonstrates:

- multi-role login
- Rails-backed academic CRUD
- Spring-backed UI shell
- admin CRUD
- trigger/procedure integration
- SQL-backed self-delete and admin delete

The remaining work is mostly hardening work:

- make Rails test execution consistently available
- improve cross-layer contract confidence
- clean up the few remaining operational risks around SQL Server availability
