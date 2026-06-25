require "test_helper"

class AccountDeleteCascadeTest < ActiveSupport::TestCase
  include AcademicWorkflowHelper

  test "deleting a student account cascades to registration and thesis" do
    workflow = create_approved_workflow

    student_user = workflow[:student_user]
    student = workflow[:student]
    registration = workflow[:registration]
    thesis = workflow[:thesis]

    student_user.destroy_with_linked_profile!

    assert_not NguoiDung.exists?(ND_ID: student_user[:ND_ID])
    assert_not SinhVien.exists?(SV_ID: student[:SV_ID])
    assert_not DangKy.exists?(DK_ID: registration[:DK_ID])
    assert_not DoAn.exists?(DA_ID: thesis[:DA_ID])

    cleanup_academic_records(workflow[:lecturer_user])
  end

  test "deleting a lecturer account cascades to topic registration and thesis" do
    workflow = create_approved_workflow

    lecturer_user = workflow[:lecturer_user]
    lecturer = workflow[:lecturer]
    topic = workflow[:topic]
    registration = workflow[:registration]
    thesis = workflow[:thesis]

    lecturer_user.destroy_with_linked_profile!

    assert_not NguoiDung.exists?(ND_ID: lecturer_user[:ND_ID])
    assert_not GiangVien.exists?(GV_ID: lecturer[:GV_ID])
    assert_not TheLoai.exists?(TL_ID: topic[:TL_ID])
    assert_not DangKy.exists?(DK_ID: registration[:DK_ID])
    assert_not DoAn.exists?(DA_ID: thesis[:DA_ID])

    cleanup_academic_records(workflow[:student_user])
  end
end
