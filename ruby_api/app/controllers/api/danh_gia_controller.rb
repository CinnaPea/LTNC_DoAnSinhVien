module Api
  class DanhGiaController < ApplicationController
    before_action :set_danh_gia, only: [:show, :update, :destroy]

    def index
      render json: DanhGia.filtered(params)
    end

    def show
      render json: @danh_gia
    end

    def create
      evaluation = DanhGia.new(create_params)
      evaluation.save_with_thesis_state!
      render json: evaluation, status: :created
    rescue ActiveRecord::RecordInvalid => e
      render json: { errors: e.record.errors.full_messages }, status: :unprocessable_entity
    end

    def update
      @danh_gia.assign_attributes(update_params)
      @danh_gia.save_with_thesis_state!
      render json: @danh_gia
    rescue ActiveRecord::RecordInvalid => e
      render json: { errors: e.record.errors.full_messages }, status: :unprocessable_entity
    end

    def destroy
      @danh_gia.destroy_with_thesis_state!
      head :no_content
    end

    private

    def set_danh_gia
      @danh_gia = DanhGia.find(params[:id])
    end

    def create_params
      params.require(:danh_gia).permit(:DA_ID, :GV_ID, :DiemSo, :NhanXet)
    end

    def update_params
      params.require(:danh_gia).permit(:GV_ID, :DiemSo, :NhanXet)
    end
  end
end
