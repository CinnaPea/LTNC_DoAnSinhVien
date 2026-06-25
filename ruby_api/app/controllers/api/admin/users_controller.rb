module Api
  module Admin
    class UsersController < ApplicationController
      before_action :set_user, only: [:show, :update, :destroy]

      def index
        render json: NguoiDung.filtered(filter_params).map { |user| serialize_user(user) }
      end

      def show
        render json: serialize_user(@user)
      end

      def create
        user = NguoiDung.new
        assign_user_attributes(user, create_params)

        if user.save
          user.ensure_profile_linked!
          user.sync_profile_name!(create_params[:ProfileName])
          render json: serialize_user(user.reload), status: :created
        else
          render json: { errors: user.errors.full_messages }, status: :unprocessable_entity
        end
      rescue ActiveRecord::ActiveRecordError => e
        render json: { errors: [e.message] }, status: :unprocessable_entity
      end

      def update
        assign_user_attributes(@user, update_params)

        if @user.save
          @user.ensure_profile_linked!
          @user.sync_profile_name!(update_params[:ProfileName])
          render json: serialize_user(@user.reload)
        else
          render json: { errors: @user.errors.full_messages }, status: :unprocessable_entity
        end
      rescue ActiveRecord::ActiveRecordError => e
        render json: { errors: [e.message] }, status: :unprocessable_entity
      end

      def destroy
        @user.destroy_with_linked_profile!
        head :no_content
      rescue ActiveRecord::ActiveRecordError => e
        render json: { errors: [e.message] }, status: :unprocessable_entity
      end

      private

      def set_user
        @user = NguoiDung.find(params[:id])
      end

      def filter_params
        params.permit(:role, :status, :q)
      end

      def create_params
        params.require(:user).permit(:Username, :Email, :Password, :VT_ID, :TrangThai, :ProfileName)
      end

      def update_params
        params.require(:user).permit(:Username, :Email, :Password, :TrangThai, :ProfileName)
      end

      def assign_user_attributes(user, permitted)
        user[:Username] = permitted[:Username] if permitted.key?(:Username)
        user[:Email] = permitted[:Email] if permitted.key?(:Email)
        user[:VT_ID] = normalized_role(permitted[:VT_ID]) if permitted.key?(:VT_ID)
        user[:TrangThai] = permitted[:TrangThai] unless permitted[:TrangThai].nil?
        user.assign_password(permitted[:Password]) if permitted.key?(:Password)
      end

      def normalized_role(raw_value)
        role_id = raw_value.to_s.strip.upcase
        return role_id if NguoiDung::ACADEMIC_ROLE_IDS.include?(role_id)

        nil
      end

      def serialize_user(user)
        {
          ND_ID: user[:ND_ID],
          Username: user[:Username],
          Email: user[:Email],
          VT_ID: user[:VT_ID],
          RoleName: user.role_name,
          TrangThai: user[:TrangThai],
          NgayLap: user[:NgayLap],
          CapNhat: user[:CapNhat],
          ProfileType: user.profile_type,
          ProfileId: user.profile_id,
          ProfileName: user.profile_name
        }
      end
    end
  end
end
