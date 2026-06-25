require "test_helper"

class BaiDangApiTest < ActionDispatch::IntegrationTest
  include AcademicWorkflowHelper

  setup do
    @workflow = create_approved_workflow
  end

  teardown do
    cleanup_academic_records(@workflow[:student_user], @workflow[:lecturer_user])
  end

  test "creates and lists submissions" do
    post "/api/bai-dang",
      params: {
        bai_dang: {
          DA_ID: @workflow[:thesis][:DA_ID],
          TieuDe: "Bao cao lan 1",
          MoTa: "Noi dung nop bai",
          Link: "https://example.test/submission-1"
        }
      },
      as: :json

    assert_response :created
    response_body = JSON.parse(response.body)
    assert_match(/\ABD\d{4}\z/, response_body["BD_ID"])

    get "/api/bai-dang", params: { da_id: @workflow[:thesis][:DA_ID] }
    assert_response :success
    assert_equal 1, JSON.parse(response.body).size
  end

  test "updates and deletes a submission" do
    submission = BaiDang.create!(
      DA_ID: @workflow[:thesis][:DA_ID],
      TieuDe: "Bao cao lan 2",
      MoTa: "Noi dung cu",
      Link: "https://example.test/old"
    )

    patch "/api/bai-dang/#{submission[:BD_ID]}",
      params: {
        bai_dang: {
          TieuDe: "Bao cao lan 2 cap nhat",
          MoTa: "Noi dung moi",
          Link: "https://example.test/new"
        }
      },
      as: :json

    assert_response :success
    submission.reload
    assert_equal "Bao cao lan 2 cap nhat", submission[:TieuDe]

    assert_difference("BaiDang.count", -1) do
      delete "/api/bai-dang/#{submission[:BD_ID]}"
    end

    assert_response :no_content
  end
end
