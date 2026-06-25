module Api
  class TienDoController < ApplicationController
    before_action :set_tien_do, only: [:show, :update, :destroy]

    def index
      render json: TienDo.filtered(params)
    end

    def show
      render json: @tien_do
    end

    def create
      progress = TienDo.new(create_params)
      progress.save!
      render json: progress, status: :created
    rescue ActiveRecord::RecordInvalid => e
      render json: { errors: e.record.errors.full_messages }, status: :unprocessable_entity
    end

    def update
      if @tien_do.update(update_params)
        render json: @tien_do
      else
        render json: { errors: @tien_do.errors.full_messages }, status: :unprocessable_entity
      end
    end

    def destroy
      @tien_do.destroy
      head :no_content
    end

    private

    def set_tien_do
      @tien_do = TienDo.find(params[:id])
    end

    def create_params
      params.require(:tien_do).permit(:DA_ID, :TieuDe, :NoiDung, :TienDoHienTai, :NhanXet)
    end

    def update_params
      params.require(:tien_do).permit(:TieuDe, :NoiDung, :TienDoHienTai, :NhanXet)
    end
  end
end
