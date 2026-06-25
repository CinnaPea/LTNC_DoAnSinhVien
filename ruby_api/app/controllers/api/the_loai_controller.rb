module Api
  class TheLoaiController < ApplicationController
    before_action :set_the_loai, only: [:show, :update, :destroy]

    def index
      render json: TheLoai.filtered(filter_params)
    end

    def show
      render json: @the_loai
    end

    def create
      the_loai = TheLoai.new(the_loai_create_params)
      the_loai.NgayLap ||= Time.current

      if the_loai.save
        render json: the_loai, status: :created
      else
        render json: { errors: the_loai.errors.full_messages }, status: :unprocessable_entity
      end
    end

    def update
      if @the_loai.update(the_loai_update_params)
        render json: @the_loai
      else
        render json: { errors: @the_loai.errors.full_messages }, status: :unprocessable_entity
      end
    end

    def destroy
      @the_loai.destroy
      head :no_content
    end

    private

    def set_the_loai
      @the_loai = TheLoai.find(params[:id])
    end

    def filter_params
      params.permit(:status, :gv_id, :q)
    end

    def the_loai_create_params
      params.require(:the_loai).permit(:TL_ID, :MaTL, :TenTL, :MoTa, :GV_ID, :TrangThai, :NgayLap)
    end

    def the_loai_update_params
      params.require(:the_loai).permit(:MaTL, :TenTL, :MoTa, :GV_ID, :TrangThai, :NgayLap)
    end
  end
end
