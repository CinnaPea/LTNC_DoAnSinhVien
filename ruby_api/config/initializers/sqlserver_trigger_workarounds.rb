adapter = ActiveRecord::ConnectionAdapters::SQLServerAdapter

# NguoiDung has enabled SQL Server triggers for role/profile linking.
# The SQL Server adapter must avoid the simple OUTPUT INSERTED path here.
adapter.exclude_output_inserted_table_names["NguoiDung"] = "nvarchar(10)"
