-- ============================================================================
-- DoAnSinhVien Database - UTF-8 Enabled Version
-- Simple UTF-8 support for Vietnamese characters
-- ============================================================================

CREATE DATABASE DoAnSinhVien
COLLATE Latin1_General_100_CI_AS_SC_UTF8
GO

USE DoAnSinhVien
GO

CREATE TABLE IdCounter (
    EntityName VARCHAR(50) PRIMARY KEY,
    Prefix VARCHAR(10) NOT NULL,
    CurrentValue INT NOT NULL
)
GO

CREATE TABLE VaiTro (
	VT_ID VARCHAR(10) PRIMARY KEY,
	TenVT NVARCHAR(50) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL UNIQUE
)
GO

CREATE TABLE NguoiDung (
	ND_ID VARCHAR(10) PRIMARY KEY,
	Username NVARCHAR(50) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL UNIQUE,
	PassHash NVARCHAR(255) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	Email VARCHAR(100) NOT NULL UNIQUE,
	VT_ID VARCHAR(10) NOT NULL REFERENCES VaiTro(VT_ID),
	TrangThai BIT NOT NULL DEFAULT 1,
	NgayLap DATETIME NOT NULL DEFAULT GETDATE(),
	CapNhat DATETIME NULL
)
GO

CREATE TABLE SinhVien (
	SV_ID VARCHAR(10) PRIMARY KEY,
	ND_ID VARCHAR(10) UNIQUE NOT NULL REFERENCES NguoiDung(ND_ID),
	MaSV NVARCHAR(20) COLLATE Latin1_General_100_CI_AS_SC_UTF8 UNIQUE,
	HoTen NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	TenLop NVARCHAR(50) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	ChuyenNganh NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	NienKhoa NVARCHAR(20) COLLATE Latin1_General_100_CI_AS_SC_UTF8
)
GO

CREATE TABLE GiangVien (
	GV_ID VARCHAR(10) PRIMARY KEY,
	ND_ID VARCHAR(10) UNIQUE NOT NULL REFERENCES NguoiDung(ND_ID),
	MaGV NVARCHAR(20) COLLATE Latin1_General_100_CI_AS_SC_UTF8 UNIQUE,
	HoTen NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	ThuocVien NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	HocVi NVARCHAR(50) COLLATE Latin1_General_100_CI_AS_SC_UTF8
)
GO

CREATE TABLE TheLoai (
	TL_ID VARCHAR(10) PRIMARY KEY,
	MaTL NVARCHAR(20) COLLATE Latin1_General_100_CI_AS_SC_UTF8 UNIQUE,
	TenTL NVARCHAR(255) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	MoTa NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	GV_ID VARCHAR(10) NOT NULL REFERENCES GiangVien(GV_ID),
	TrangThai NVARCHAR(20) NOT NULL DEFAULT N'Mở' CHECK (TrangThai IN (N'Mở', N'Đóng')),
	NgayLap DATETIME NOT NULL DEFAULT GETDATE()
)
GO

CREATE TABLE DangKy (
	DK_ID VARCHAR(10) PRIMARY KEY,
	NgayDangKy DATETIME NOT NULL DEFAULT GETDATE(),
	SV_ID VARCHAR(10) NOT NULL REFERENCES SinhVien(SV_ID),
	TL_ID VARCHAR(10) NOT NULL REFERENCES TheLoai(TL_ID),
	NguoiChapThuan VARCHAR(10) NULL REFERENCES GiangVien(GV_ID),
	TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang kiểm duyệt' CHECK (TrangThai IN (N'Đang kiểm duyệt', N'Đã duyệt', N'Từ chối')),
	NgayChapThuan DATETIME NULL,
	GhiChu NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	UNIQUE (SV_ID, TL_ID)
)
GO

CREATE TABLE DoAn (
	DA_ID VARCHAR(10) PRIMARY KEY,
	DK_ID VARCHAR(10) NOT NULL UNIQUE REFERENCES DangKy(DK_ID),
	GV_ID VARCHAR(10) NOT NULL REFERENCES GiangVien(GV_ID),
	TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang hoàn thành' CHECK (TrangThai IN (N'Đang hoàn thành', N'Hoàn thành')),
	NgayThucHien DATETIME NOT NULL DEFAULT GETDATE()
)
GO

CREATE TABLE TienDo (
	TD_ID VARCHAR(10) PRIMARY KEY,
	DA_ID VARCHAR(10) NOT NULL REFERENCES DoAn(DA_ID),
	TieuDe NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	NoiDung NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	TienDoHienTai INT CHECK (TienDoHienTai BETWEEN 0 AND 100),
	NgayGui DATETIME NOT NULL DEFAULT GETDATE(),
	NhanXet NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8
)
GO

CREATE TABLE BaiDang (
	BD_ID VARCHAR(10) PRIMARY KEY,
	DA_ID VARCHAR(10) NOT NULL REFERENCES DoAn(DA_ID),
	TieuDe NVARCHAR(100) COLLATE Latin1_General_100_CI_AS_SC_UTF8 NOT NULL,
	NgayDang DATETIME NOT NULL DEFAULT GETDATE(),
	MoTa NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	Link NVARCHAR(500) COLLATE Latin1_General_100_CI_AS_SC_UTF8
)
GO

CREATE TABLE DanhGia (
	DG_ID VARCHAR(10) PRIMARY KEY,
	DA_ID VARCHAR(10) NOT NULL REFERENCES DoAn(DA_ID),
	GV_ID VARCHAR(10) NOT NULL REFERENCES GiangVien(GV_ID),
	DiemSo DECIMAL(4,2) CHECK (DiemSo BETWEEN 0 AND 10),
	NhanXet NVARCHAR(MAX) COLLATE Latin1_General_100_CI_AS_SC_UTF8,
	NgayNX DATETIME NOT NULL DEFAULT GETDATE()
)
GO

CREATE OR ALTER PROCEDURE sp_CreateNguoiDung
    @Username NVARCHAR(50),
    @PassHash NVARCHAR(255),
    @Email VARCHAR(100),
    @VT_ID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;
    BEGIN TRAN;
    BEGIN TRY
        DECLARE @NextValue INT;
        DECLARE @Prefix VARCHAR(10);
        DECLARE @NewND_ID VARCHAR(10);
        -- Check duplicate username
        IF EXISTS (SELECT 1 FROM NguoiDung WHERE Username = @Username)
        BEGIN
            RAISERROR(N'Username đã tồn tại.', 16, 1);
            ROLLBACK TRAN;
            RETURN;
        END
        -- Check duplicate email
        IF EXISTS (SELECT 1 FROM NguoiDung WHERE Email = @Email)
        BEGIN
            RAISERROR(N'Email đã tồn tại.', 16, 1);
            ROLLBACK TRAN;
            RETURN;
        END
        -- Check valid role
        IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE VT_ID = @VT_ID)
        BEGIN
            RAISERROR(N'VT_ID không hợp lệ.', 16, 1);
            ROLLBACK TRAN;
            RETURN;
        END
        -- Lock row in IdCounter for NGUOIDUNG
        SELECT 
            @NextValue = CurrentValue + 1,
            @Prefix = Prefix
        FROM IdCounter WITH (UPDLOCK, HOLDLOCK)
        WHERE EntityName = 'NGUOIDUNG';
        IF @Prefix IS NULL
        BEGIN
            RAISERROR(N'Không tìm thấy cấu hình IdCounter cho NGUOIDUNG.', 16, 1);
            ROLLBACK TRAN;
            RETURN;
        END
        -- Generate ND_ID
        SET @NewND_ID = @Prefix + RIGHT('0000' + CAST(@NextValue AS VARCHAR(10)), 4)
        -- Insert into NguoiDung
        INSERT INTO NguoiDung (ND_ID, Username, PassHash, Email, VT_ID)
        VALUES (@NewND_ID, @Username, @PassHash, @Email, @VT_ID)
        -- Update counter
        UPDATE IdCounter
        SET CurrentValue = @NextValue
        WHERE EntityName = 'NGUOIDUNG'
        COMMIT TRAN;

        SELECT * 
        FROM NguoiDung
        WHERE ND_ID = @NewND_ID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRAN
        THROW;
    END CATCH
END
GO

CREATE OR ALTER TRIGGER trg_CreateProfile
ON NguoiDung
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON
    DECLARE @VT_ID VARCHAR(10), @ND_ID VARCHAR(10), @Next INT, @NewID VARCHAR(10);
    DECLARE cur_insert CURSOR FOR
    SELECT VT_ID, ND_ID
    FROM inserted;
    OPEN cur_insert;
    FETCH NEXT FROM cur_insert INTO @VT_ID, @ND_ID;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        IF @VT_ID = 'SV'
        BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM SinhVien
                WHERE ND_ID = @ND_ID
            )
            BEGIN
                UPDATE IdCounter
                SET @Next = CurrentValue = CurrentValue + 1
                WHERE EntityName = 'SINHVIEN';
                SET @NewID = 'SV' + RIGHT('0000' + CAST(@Next AS VARCHAR(4)), 4);
                INSERT INTO SinhVien (SV_ID, ND_ID, MaSV, HoTen)
                VALUES (@NewID, @ND_ID, @NewID, N'Sinh viên mới');
            END
        END
        ELSE IF @VT_ID = 'GV'
        BEGIN
            IF NOT EXISTS (
                SELECT 1
                FROM GiangVien
                WHERE ND_ID = @ND_ID
            )
            BEGIN
                UPDATE IdCounter
                SET @Next = CurrentValue = CurrentValue + 1
                WHERE EntityName = 'GIANGVIEN';
                SET @NewID = 'GV' + RIGHT('0000' + CAST(@Next AS VARCHAR(4)), 4);
                INSERT INTO GiangVien (GV_ID, ND_ID, MaGV, HoTen)
                VALUES (@NewID, @ND_ID, @NewID, N'Giảng viên mới');
            END
        END
        FETCH NEXT FROM cur_insert INTO @VT_ID, @ND_ID;
    END
    CLOSE cur_insert;
    DEALLOCATE cur_insert;
END;
GO

-- ============================================================================
-- Initialize Data
-- ============================================================================

INSERT INTO IdCounter (EntityName, Prefix, CurrentValue)
VALUES
('NGUOIDUNG', 'ND', 0),
('SINHVIEN', 'SV', 0),
('GIANGVIEN', 'GV', 0),
('THELOAI', 'TL', 7),
('BAIDANG', 'BD', 0),
('DANGKY', 'DK', 0),
('DANHGIA', 'DG', 0),
('DOAN', 'DA', 0),
('TIENDO', 'TD', 0);
GO

INSERT INTO VaiTro (VT_ID, TenVT)
VALUES
('SV', N'Sinh viên'),
('GV', N'Giảng viên'),
('AD', N'Quản trị viên');
GO

-- Create sample lecturers first
EXEC sp_CreateNguoiDung 'lecturer01', 'hash_lec01', 'lecturer01@university.edu', 'GV';
EXEC sp_CreateNguoiDung 'lecturer02', 'hash_lec02', 'lecturer02@university.edu', 'GV';
GO

INSERT INTO TheLoai (TL_ID, MaTL, TenTL, MoTa, GV_ID, TrangThai)
VALUES
-- ===== GV0001: More SYSTEM / BACKEND oriented =====
('TL0001', N'AI-THERMAL-01',
 N'Tăng cường chất lượng ảnh từ camera nhiệt',
 N'Nghiên cứu các kỹ thuật xử lý ảnh và AI nhằm cải thiện độ rõ nét và giảm nhiễu trong ảnh thu từ camera nhiệt, phục vụ giám sát quân sự.',
 'GV0001', N'Mở'),

('TL0002', N'WEB-EXAM-02',
 N'Hệ thống tổ chức kỳ thi trực tuyến thông minh',
 N'Xây dựng hệ thống web phân phòng, xếp chỗ và điểm danh tự động bằng QR code, hỗ trợ giám sát thi cử.',
 'GV0001', N'Mở'),

('TL0004', N'API-MICRO-04',
 N'Thiết kế hệ thống Microservices với Spring Boot',
 N'Xây dựng hệ thống backend phân tán sử dụng Spring Boot và Docker, tập trung vào scalability và fault tolerance.',
 'GV0001', N'Mở'),

-- ===== GV0002: More FRONTEND / UX / PRACTICAL =====
('TL0005', N'UI-UX-01',
 N'Thiết kế giao diện người dùng cho hệ thống giáo dục',
 N'Nghiên cứu UX/UI cho hệ thống học tập trực tuyến, tập trung vào trải nghiệm sinh viên và giảng viên.',
 'GV0002', N'Mở'),

('TL0007', N'MOBILE-IOT-03',
 N'Ứng dụng di động giám sát thiết bị IoT',
 N'Phát triển ứng dụng mobile kết nối và giám sát thiết bị IoT trong môi trường thực tế.',
 'GV0002', N'Đóng');
GO

-- ============================================================================
-- Account delete lifecycle support
-- ============================================================================

CREATE OR ALTER TRIGGER trg_DeleteNguoiDungCascade
ON NguoiDung
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @DeletedStudents TABLE (SV_ID VARCHAR(10) PRIMARY KEY);
    DECLARE @DeletedLecturers TABLE (GV_ID VARCHAR(10) PRIMARY KEY);
    DECLARE @LecturerTopics TABLE (TL_ID VARCHAR(10) PRIMARY KEY);
    DECLARE @RegistrationsToDelete TABLE (DK_ID VARCHAR(10) PRIMARY KEY);
    DECLARE @ThesesToDelete TABLE (DA_ID VARCHAR(10) PRIMARY KEY);

    INSERT INTO @DeletedStudents (SV_ID)
    SELECT sv.SV_ID
    FROM SinhVien sv
    INNER JOIN deleted d ON d.ND_ID = sv.ND_ID;

    INSERT INTO @DeletedLecturers (GV_ID)
    SELECT gv.GV_ID
    FROM GiangVien gv
    INNER JOIN deleted d ON d.ND_ID = gv.ND_ID;

    INSERT INTO @LecturerTopics (TL_ID)
    SELECT tl.TL_ID
    FROM TheLoai tl
    INNER JOIN @DeletedLecturers dl ON dl.GV_ID = tl.GV_ID;

    INSERT INTO @RegistrationsToDelete (DK_ID)
    SELECT DISTINCT dk.DK_ID
    FROM DangKy dk
    LEFT JOIN @DeletedStudents ds ON ds.SV_ID = dk.SV_ID
    LEFT JOIN @LecturerTopics lt ON lt.TL_ID = dk.TL_ID
    LEFT JOIN @DeletedLecturers dl ON dl.GV_ID = dk.NguoiChapThuan
    WHERE ds.SV_ID IS NOT NULL
       OR lt.TL_ID IS NOT NULL
       OR dl.GV_ID IS NOT NULL;

    INSERT INTO @ThesesToDelete (DA_ID)
    SELECT DISTINCT da.DA_ID
    FROM DoAn da
    LEFT JOIN @RegistrationsToDelete rd ON rd.DK_ID = da.DK_ID
    LEFT JOIN @DeletedLecturers dl ON dl.GV_ID = da.GV_ID
    WHERE rd.DK_ID IS NOT NULL
       OR dl.GV_ID IS NOT NULL;

    DELETE dg
    FROM DanhGia dg
    WHERE dg.DA_ID IN (SELECT DA_ID FROM @ThesesToDelete)
       OR dg.GV_ID IN (SELECT GV_ID FROM @DeletedLecturers);

    DELETE bd
    FROM BaiDang bd
    WHERE bd.DA_ID IN (SELECT DA_ID FROM @ThesesToDelete);

    DELETE td
    FROM TienDo td
    WHERE td.DA_ID IN (SELECT DA_ID FROM @ThesesToDelete);

    DELETE da
    FROM DoAn da
    WHERE da.DA_ID IN (SELECT DA_ID FROM @ThesesToDelete);

    DELETE dk
    FROM DangKy dk
    WHERE dk.DK_ID IN (SELECT DK_ID FROM @RegistrationsToDelete);

    DELETE tl
    FROM TheLoai tl
    WHERE tl.TL_ID IN (SELECT TL_ID FROM @LecturerTopics);

    DELETE sv
    FROM SinhVien sv
    INNER JOIN deleted d ON d.ND_ID = sv.ND_ID;

    DELETE gv
    FROM GiangVien gv
    INNER JOIN deleted d ON d.ND_ID = gv.ND_ID;

    DELETE nd
    FROM NguoiDung nd
    INNER JOIN deleted d ON d.ND_ID = nd.ND_ID;
END
GO

CREATE OR ALTER PROCEDURE sp_DeleteNguoiDung
    @ND_ID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRAN;
    BEGIN TRY
        IF NOT EXISTS (
            SELECT 1
            FROM NguoiDung
            WHERE ND_ID = @ND_ID
        )
        BEGIN
            RAISERROR(N'Khong tim thay nguoi dung.', 16, 1);
            ROLLBACK TRAN;
            RETURN;
        END;

        DELETE FROM NguoiDung
        WHERE ND_ID = @ND_ID;

        COMMIT TRAN;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRAN;
        THROW;
    END CATCH
END
GO

-- ============================================================================
-- Verification
-- ============================================================================

SELECT '==== Database UTF-8 Configuration ====' AS Info;
SELECT name, collation_name FROM sys.databases WHERE name = 'DoAnSinhVien';
GO

SELECT '==== VaiTro ====' AS Info;
SELECT * FROM VaiTro;

SELECT '==== TheLoai ====' AS Info;
SELECT * FROM TheLoai;

SELECT '==== IdCounter ====' AS Info;
SELECT * FROM IdCounter;

PRINT N'✅ Database created with UTF-8 support!';
PRINT N'✅ Vietnamese characters fully supported';
GO
