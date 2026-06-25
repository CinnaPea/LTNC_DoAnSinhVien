require "securerandom"

module AcademicWorkflowHelper
  def create_academic_user(role_id:, username_prefix:)
    username = "#{username_prefix}_#{SecureRandom.hex(4)}"
    user = NguoiDung.create!(
      Username: username,
      Email: "#{username}@example.test",
      PassHash: "secret123",
      VT_ID: role_id,
      TrangThai: true
    )

    user.ensure_profile_linked!
    user.reload
  end

  def student_profile_for(user)
    SinhVien.find_by!(ND_ID: user[:ND_ID])
  end

  def lecturer_profile_for(user)
    GiangVien.find_by!(ND_ID: user[:ND_ID])
  end

  def create_topic_for(lecturer_profile)
    suffix = SecureRandom.hex(2).upcase
    TheLoai.create!(
      MaTL: "AUTO-#{suffix}",
      TenTL: "De tai #{suffix}",
      MoTa: "Mo ta tu dong",
      GV_ID: lecturer_profile[:GV_ID],
      TrangThai: TheLoai::OPEN_STATUS
    )
  end

  def create_approved_workflow
    lecturer_user = create_academic_user(role_id: "GV", username_prefix: "gv_workflow")
    student_user = create_academic_user(role_id: "SV", username_prefix: "sv_workflow")

    lecturer = lecturer_profile_for(lecturer_user)
    student = student_profile_for(student_user)
    topic = create_topic_for(lecturer)
    registration = DangKy.register!(sv_id: student[:SV_ID], tl_id: topic[:TL_ID], ghi_chu: "Dang ky tu dong")
    registration.approve!(approved_by: lecturer[:GV_ID])
    thesis = DoAn.find_by!(DK_ID: registration[:DK_ID])

    {
      lecturer_user: lecturer_user,
      student_user: student_user,
      lecturer: lecturer,
      student: student,
      topic: topic,
      registration: registration,
      thesis: thesis
    }
  end

  def cleanup_academic_records(*users)
    users.compact.each do |user|
      user.reload if user.persisted?
      user.destroy_with_linked_profile! if user.persisted?
    rescue ActiveRecord::RecordNotFound, ActiveRecord::StatementInvalid
      nil
    end
  end
end
