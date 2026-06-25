module SqlServerAvailability
  def before_setup
    ensure_sql_server_available!
    super
  end

  private

  def ensure_sql_server_available!
    ActiveRecord::Base.connection_pool.with_connection do |connection|
      connection.select_value("SELECT 1")
    end
  rescue ActiveRecord::ConnectionNotEstablished, ActiveRecord::StatementInvalid => exception
    skip "SQL Server unavailable for Rails test run: #{root_database_error_message(exception)}"
  rescue StandardError => exception
    raise exception unless tiny_tds_error?(exception)

    skip "SQL Server unavailable for Rails test run: #{root_database_error_message(exception)}"
  end

  def tiny_tds_error?(exception)
    current = exception

    while current
      return true if current.class.name == "TinyTds::Error"

      current = current.cause
    end

    false
  end

  def root_database_error_message(exception)
    current = exception
    previous = exception

    while current
      previous = current
      current = current.cause
    end

    previous.message
  end
end
