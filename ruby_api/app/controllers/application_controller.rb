class ApplicationController < ActionController::API
  rescue_from ActiveRecord::RecordNotFound, with: :render_not_found
  rescue_from ActiveRecord::RecordNotUnique, with: :render_record_not_unique
  rescue_from ActiveRecord::ConnectionNotEstablished, with: :render_database_unavailable
  rescue_from ActiveRecord::StatementInvalid, with: :handle_statement_invalid

  private

  def render_not_found(exception)
    render json: { errors: [exception.message] }, status: :not_found
  end

  def render_record_not_unique(exception)
    message = exception.message.include?("TL_ID") ? "TL_ID da ton tai." : "Du lieu da ton tai."
    render json: { errors: [message] }, status: :unprocessable_entity
  end

  def handle_statement_invalid(exception)
    raise exception unless database_unavailable?(exception)

    render_database_unavailable(exception)
  end

  def render_database_unavailable(_exception)
    render json: { errors: ["SQL Server tam thoi khong kha dung. Vui long thu lai sau."] }, status: :service_unavailable
  end

  def database_unavailable?(exception)
    current = exception

    while current
      return true if current.class.name == "TinyTds::Error"

      message = current.message.to_s
      return true if message.include?("TDS server connection failed")
      return true if message.include?("Adaptive Server connection failed")
      return true if message.include?("Login timeout expired")

      current = current.cause
    end

    false
  end
end
