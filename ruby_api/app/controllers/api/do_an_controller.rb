module Api
  class DoAnController < ApplicationController
    before_action :sync_records, only: [:index]
    before_action :set_do_an, only: [:show]

    def index
      render json: DoAn.filtered(params)
    end

    def show
      render json: @do_an
    end

    private

    def set_do_an
      @do_an = DoAn.find(params[:id])
    end

    def sync_records
      DoAn.sync_from_approved_registrations!(params.slice(:sv_id, :gv_id))
    end
  end
end
