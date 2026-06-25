require "test_helper"

class TienDoApiTest < ActionDispatch::IntegrationTest
  include AcademicWorkflowHelper

  setup do
    @workflow = create_approved_workflow
  end

  teardown do
    cleanup_academic_records(@workflow[:student_user], @workflow[:lecturer_user])
  end

  test "creates and lists progress entries for student and lecturer filters" do
    post "/api/tien-do",
      params: {
        tien_do: {
          DA_ID: @workflow[:thesis][:DA_ID],
          TieuDe: "Bao cao tien do 1",
          NoiDung: "Da hoan thanh phan phan tich",
          TienDoHienTai: 35
        }
      },
      as: :json

    assert_response :created
    response_body = JSON.parse(response.body)
    assert_match(/\ATD\d{4}\z/, response_body["TD_ID"])

    get "/api/tien-do", params: { sv_id: @workflow[:student][:SV_ID] }
    assert_response :success
    assert_equal 1, JSON.parse(response.body).size

    get "/api/tien-do", params: { gv_id: @workflow[:lecturer][:GV_ID] }
    assert_response :success
    assert_equal 1, JSON.parse(response.body).size
  end

  test "updates a progress entry with student fields and lecturer feedback" do
    progress = TienDo.create!(
      DA_ID: @workflow[:thesis][:DA_ID],
      TieuDe: "Moc 1",
      NoiDung: "Noi dung ban dau",
      TienDoHienTai: 20
    )

    patch "/api/tien-do/#{progress[:TD_ID]}",
      params: {
        tien_do: {
          TieuDe: "Moc 1 cap nhat",
          NoiDung: "Noi dung moi",
          TienDoHienTai: 45,
          NhanXet: "Can bo sung bieu do"
        }
      },
      as: :json

    assert_response :success

    progress.reload
    assert_equal "Moc 1 cap nhat", progress[:TieuDe]
    assert_equal 45, progress[:TienDoHienTai]
    assert_equal "Can bo sung bieu do", progress[:NhanXet]
  end

  test "deletes a progress entry" do
    progress = TienDo.create!(
      DA_ID: @workflow[:thesis][:DA_ID],
      TieuDe: "Moc 2",
      NoiDung: "Noi dung xoa",
      TienDoHienTai: 50
    )

    assert_difference("TienDo.count", -1) do
      delete "/api/tien-do/#{progress[:TD_ID]}"
    end

    assert_response :no_content
  end
end
