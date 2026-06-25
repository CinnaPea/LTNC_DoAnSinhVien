require "test_helper"

class DanhGiaApiTest < ActionDispatch::IntegrationTest
  include AcademicWorkflowHelper

  setup do
    @workflow = create_approved_workflow
  end

  teardown do
    cleanup_academic_records(@workflow[:student_user], @workflow[:lecturer_user])
  end

  test "creates evaluation and marks thesis completed" do
    post "/api/danh-gia",
      params: {
        danh_gia: {
          DA_ID: @workflow[:thesis][:DA_ID],
          GV_ID: @workflow[:lecturer][:GV_ID],
          DiemSo: 8.75,
          NhanXet: "Dat yeu cau"
        }
      },
      as: :json

    assert_response :created
    response_body = JSON.parse(response.body)
    assert_match(/\ADG\d{4}\z/, response_body["DG_ID"])

    @workflow[:thesis].reload
    assert_equal DoAn::COMPLETED_STATUS, @workflow[:thesis][:TrangThai]
  end

  test "updates and deletes evaluation while syncing thesis status" do
    evaluation = DanhGia.new(
      DA_ID: @workflow[:thesis][:DA_ID],
      GV_ID: @workflow[:lecturer][:GV_ID],
      DiemSo: 7.0,
      NhanXet: "Can sua"
    )
    evaluation.save_with_thesis_state!

    patch "/api/danh-gia/#{evaluation[:DG_ID]}",
      params: {
        danh_gia: {
          DiemSo: 9.0,
          NhanXet: "Da cai thien"
        }
      },
      as: :json

    assert_response :success
    evaluation.reload
    assert_equal 9.0, evaluation[:DiemSo].to_f

    assert_difference("DanhGia.count", -1) do
      delete "/api/danh-gia/#{evaluation[:DG_ID]}"
    end

    assert_response :no_content
    @workflow[:thesis].reload
    assert_equal DoAn::ACTIVE_STATUS, @workflow[:thesis][:TrangThai]
  end
end
