module Api
  class BaiDangController < ApplicationController
    before_action :set_bai_dang, only: [:show, :update, :destroy]

    def index
      render json: BaiDang.filtered(params)
    end

    def show
      render json: @bai_dang
    end

    def create
      submission = BaiDang.new(create_params)
      submission.save!
      render json: submission, status: :created
    rescue ActiveRecord::RecordInvalid => e
      render json: { errors: e.record.errors.full_messages }, status: :unprocessable_entity
    end

    def update
      if @bai_dang.update(update_params)
        render json: @bai_dang
      else
        render json: { errors: @bai_dang.errors.full_messages }, status: :unprocessable_entity
      end
    end

    def destroy
      @bai_dang.destroy
      head :no_content
    end

    private

    def set_bai_dang
      @bai_dang = BaiDang.find(params[:id])
    end

    def create_params
      params.require(:bai_dang).permit(:DA_ID, :TieuDe, :MoTa, :Link)
    end

    def update_params
      params.require(:bai_dang).permit(:TieuDe, :MoTa, :Link)
    end
  end
end
