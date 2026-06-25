module Api
  class DangKyController < ApplicationController
    before_action :set_dang_ky, only: [:show, :update, :destroy, :approve, :reject]

    def index
      registrations = DangKy.all
      registrations = registrations.where(SV_ID: params[:sv_id]) if params[:sv_id].present?
      registrations = registrations.where(TrangThai: params[:status]) if params[:status].present?

      if params[:gv_id].present?
        topic_ids = TheLoai.where(GV_ID: params[:gv_id]).pluck(:TL_ID)
        registrations = registrations.where(TL_ID: topic_ids)
      end

      render json: registrations.order(:NgayDangKy, :DK_ID)
    end

    def show
      render json: @dang_ky
    end

    def create
      registration = DangKy.register!(
        sv_id: create_params[:SV_ID],
        tl_id: create_params[:TL_ID],
        ghi_chu: create_params[:GhiChu]
      )

      render json: registration, status: :created
    rescue ActiveRecord::RecordInvalid => e
      render json: { errors: e.record.errors.full_messages }, status: :unprocessable_entity
    end

    def update
      unless @dang_ky.pending?
        return render json: { errors: ["Chi duoc sua dang ky dang cho duyet"] }, status: :unprocessable_entity
      end

      if @dang_ky.update(update_params)
        render json: @dang_ky
      else
        render json: { errors: @dang_ky.errors.full_messages }, status: :unprocessable_entity
      end
    end

    def destroy
      unless @dang_ky.pending?
        return render json: { errors: ["Chi duoc xoa dang ky dang cho duyet"] }, status: :unprocessable_entity
      end

      @dang_ky.destroy
      head :no_content
    end

    def approve
      @dang_ky.approve!(approved_by: decision_params[:NguoiChapThuan])
      render json: @dang_ky
    rescue StandardError => e
      render json: { errors: [e.message] }, status: :unprocessable_entity
    end

    def reject
      @dang_ky.reject!(approved_by: decision_params[:NguoiChapThuan])
      render json: @dang_ky
    rescue StandardError => e
      render json: { errors: [e.message] }, status: :unprocessable_entity
    end

    private

    def set_dang_ky
      @dang_ky = DangKy.find(params[:id])
    end

    def create_params
      params.require(:dang_ky).permit(:SV_ID, :TL_ID, :GhiChu)
    end

    def update_params
      params.require(:dang_ky).permit(:GhiChu)
    end

    def decision_params
      params.require(:dang_ky).permit(:NguoiChapThuan)
    end
  end
end
