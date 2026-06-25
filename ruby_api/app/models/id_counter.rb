class IdCounter < ApplicationRecord
  self.table_name = "IdCounter"
  self.primary_key = "EntityName"

  def self.allocate!(entity_name)
    transaction do
      counter = lock.find(entity_name)
      next_value = counter[:CurrentValue].to_i + 1
      counter.update!(CurrentValue: next_value)
      "#{counter[:Prefix]}#{next_value.to_s.rjust(4, '0')}"
    end
  end
end
