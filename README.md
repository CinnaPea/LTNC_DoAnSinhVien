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
