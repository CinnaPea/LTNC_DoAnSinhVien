require "test_helper"

class TheLoaiApiTest < ActionDispatch::IntegrationTest
  test "lists all the loai ordered by TL_ID" do
    get "/api/the-loai"

    assert_response :success

    response_body = JSON.parse(response.body)
    assert_equal ["TL0001", "TL0002", "TL0003"], response_body.map { |item| item["TL_ID"] }
  end

  test "filters by status" do
    get "/api/the-loai", params: { status: "Mở" }

    assert_response :success

    response_body = JSON.parse(response.body)
    assert_equal ["TL0001", "TL0003"], response_body.map { |item| item["TL_ID"] }
  end

  test "filters by lecturer" do
    get "/api/the-loai", params: { gv_id: "GV0002" }

    assert_response :success

    response_body = JSON.parse(response.body)
    assert_equal ["TL0002"], response_body.map { |item| item["TL_ID"] }
  end

  test "searches by keyword across id code and name" do
    get "/api/the-loai", params: { q: "API" }

    assert_response :success

    response_body = JSON.parse(response.body)
    assert_equal ["TL0003"], response_body.map { |item| item["TL_ID"] }
  end

  test "creates the loai with valid payload" do
    assert_difference("TheLoai.count", 1) do
      post "/api/the-loai",
        params: {
          the_loai: {
            MaTL: "DT09",
            TenTL: "De tai moi",
            MoTa: "Mo ta moi",
            GV_ID: "GV0009",
            TrangThai: "Mở"
          }
        },
        as: :json
    end

    assert_response :created

    response_body = JSON.parse(response.body)
    assert_match(/\ATL\d{4}\z/, response_body["TL_ID"])
    assert_equal "De tai moi", response_body["TenTL"]
  end

  test "rejects invalid status on create" do
    assert_no_difference("TheLoai.count") do
      post "/api/the-loai",
        params: {
          the_loai: {
            TL_ID: "TL0010",
            MaTL: "DT10",
            TenTL: "Trang thai sai",
            GV_ID: "GV0001",
            TrangThai: "Tam dung"
          }
        },
        as: :json
    end

    assert_response :unprocessable_entity

    response_body = JSON.parse(response.body)
    assert_includes response_body["errors"].join(" "), "TrangThai"
  end

  test "does not allow changing primary key on update" do
    patch "/api/the-loai/TL0001",
      params: {
        the_loai: {
          TL_ID: "TL9999",
          TenTL: "Ten da cap nhat"
        }
      },
      as: :json

    assert_response :success

    assert TheLoai.exists?(TL_ID: "TL0001")
    assert_not TheLoai.exists?(TL_ID: "TL9999")
    assert_equal "Ten da cap nhat", TheLoai.find("TL0001").TenTL
  end

  test "returns 404 when the loai is missing" do
    get "/api/the-loai/TL9999"

    assert_response :not_found

    response_body = JSON.parse(response.body)
    assert response_body["errors"].present?
  end
end
