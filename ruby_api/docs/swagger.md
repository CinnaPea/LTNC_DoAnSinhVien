# Swagger Docs

The Rails project exposes Swagger from static files:

- UI: `/swagger/index.html`
- Spec: `/swagger/openapi.json`

Examples:

- `http://localhost:3000/swagger/index.html`
- `http://localhost:3000/swagger/openapi.json`

This is still a low-friction setup. The file is hand-maintained, which is acceptable for the current size of the API surface.

## Current coverage

The checked-in spec now covers:

- `GET /up`
- `Admin` user list/detail/create/update/delete
- `TheLoai` CRUD
- `DangKy` CRUD
- `DangKy` approve and reject actions
- `DoAn` read endpoints
- `TienDo` CRUD
- `BaiDang` CRUD
- `DanhGia` CRUD

The spec also documents the current workflow rule:

- approving a `DangKy` creates a `DoAn` in the same Rails transaction
- creating an admin-managed academic user expects the SQL trigger to create the linked `SinhVien` or `GiangVien` row

## Updating the docs

When routes, request bodies, status values, or workflow rules change, update:

- `public/swagger/openapi.json`

The highest-risk places to keep in sync are:

- `Admin` create/update behavior, because only `SV` and `GV` are allowed and the trigger-linked profile is part of the flow
- `TheLoai` create requirements, because `TL_ID` is now optional and auto-generated
- `DangKy` approve behavior, because it has a side effect on `DoAn`
- `DoAn` status values, because they must match the SQL Server check constraint exactly
- `DanhGia` behavior, because create and update complete the thesis while delete reopens it
- `TienDo` and `BaiDang` request payloads, because Spring and Rails now use them as full student and instructor workflows

## Notes

- Swagger UI uses the checked-in OpenAPI file, so no extra gem is required.
- The UI points at the local Rails server by default: `http://localhost:3000`.
- CORS is still disabled in the Rails initializer. That is fine because Spring is acting as the frontend gateway.
- If the API surface keeps growing, moving to generated docs from request specs with `rswag` becomes worthwhile.
