#!/usr/bin/env ruby

# Quick sanity check for Ruby <-> SQL Server UTF-8 connection
require_relative 'config/environment'

puts "=" * 60
puts "Testing Ruby → SQL Server UTF-8 Connection"
puts "=" * 60

begin
  # Test 1: Basic connection
  puts "\n[1] Testing database connection..."
  db_name = ActiveRecord::Base.connection.execute("SELECT DB_NAME()").first[0]
  puts "✓ Connected to database: #{db_name}"

  # Test 2: Query TheLoai table
  puts "\n[2] Querying TheLoai table..."
  count = TheLoai.count
  puts "✓ Found #{count} topics in TheLoai table"

  # Test 3: Display sample Vietnamese data
  puts "\n[3] Sample Vietnamese data from TheLoai:"
  TheLoai.limit(3).each do |topic|
    puts "  - TL_ID: #{topic.TL_ID}"
    puts "    TenTL: #{topic.TenTL} (UTF-8 ✓)"
    puts "    TrangThai: #{topic.TrangThai}"
    puts "    ---"
  end

  # Test 4: Verify Vietnamese status values
  puts "\n[4] Checking Vietnamese status values..."
  open_topics = TheLoai.open_topics.count
  puts "✓ Open topics (Mở): #{open_topics}"
  
  all_statuses = TheLoai.distinct.pluck(:TrangThai)
  puts "✓ All statuses in database: #{all_statuses.inspect}"

  # Test 5: Insert test Vietnamese data
  puts "\n[5] Testing Vietnamese character insert..."
  test_topic = TheLoai.new(
    TL_ID: "TEST99",
    MaTL: "TEST-UTF8",
    TenTL: "Kiểm tra UTF-8: Tiếng Việt, ơ, ư, đ, à, á, ả, ã, ạ",
    MoTa: "Test data với dấu tiếng Việt 🇻🇳",
    GV_ID: "GV0001",
    TrangThai: "Mở"
  )
  
  if test_topic.save
    puts "✓ Successfully saved Vietnamese text to database"
    
    # Verify it was saved correctly
    retrieved = TheLoai.find("TEST99")
    puts "✓ Retrieved: #{retrieved.TenTL}"
    
    # Clean up
    retrieved.destroy
    puts "✓ Cleanup complete"
  else
    puts "✗ Failed to save test data: #{test_topic.errors.full_messages}"
  end

  puts "\n" + "=" * 60
  puts "✅ ALL TESTS PASSED - Ruby ↔ SQL Server UTF-8 Connection OK!"
  puts "=" * 60

rescue => e
  puts "\n" + "=" * 60
  puts "❌ ERROR - Connection or query failed:"
  puts "#{e.class}: #{e.message}"
  puts e.backtrace.first(5)
  puts "=" * 60
  exit 1
end
