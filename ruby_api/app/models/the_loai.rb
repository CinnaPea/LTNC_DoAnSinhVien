class TheLoai < ApplicationRecord
  self.table_name = "TheLoai"
  self.primary_key = "TL_ID"

  OPEN_STATUS = "M\u1EDF"
  CLOSED_STATUS = "\u0110\u00F3ng"
  VALID_STATUSES = [OPEN_STATUS, CLOSED_STATUS].freeze

  before_validation :normalize_attributes
  before_validation :assign_tl_id, on: :create

  validates :TL_ID, presence: true, length: { maximum: 10 }
  validates :MaTL, presence: true, length: { maximum: 20 }
  validates :TenTL, presence: true, length: { maximum: 255 }
  validates :MoTa, length: { maximum: 255 }, allow_blank: true
  validates :GV_ID, presence: true, length: { maximum: 10 }
  validates :TrangThai, presence: true, inclusion: { in: VALID_STATUSES }
  validates :TL_ID, uniqueness: { message: "da ton tai" }
  validates :MaTL, uniqueness: { message: "da ton tai" }

  scope :open_topics, -> { where(TrangThai: OPEN_STATUS) }
  scope :for_lecturer, ->(gv_id) { where(GV_ID: gv_id) }
  scope :with_status, ->(status) { where(TrangThai: status) }
  scope :search, lambda { |query|
    sanitized = sanitize_sql_like(query.to_s.strip)
    where("TL_ID LIKE :q OR MaTL LIKE :q OR TenTL LIKE :q", q: "%#{sanitized}%")
  }

  def self.filtered(params)
    records = all
    records = records.with_status(params[:status]) if params[:status].present?
    records = records.for_lecturer(params[:gv_id].to_s.strip) if params[:gv_id].present?
    records = records.search(params[:q]) if params[:q].present?
    records.order(:TL_ID)
  end

  def open?
    self[:TrangThai] == OPEN_STATUS
  end

  private

  def normalize_attributes
    self[:TL_ID] = self[:TL_ID].to_s.strip.presence
    self[:MaTL] = self[:MaTL].to_s.strip.presence
    self[:TenTL] = self[:TenTL].to_s.strip.presence
    self[:MoTa] = self[:MoTa].to_s.strip.presence
    self[:GV_ID] = self[:GV_ID].to_s.strip.presence
    self[:TrangThai] = self[:TrangThai].to_s.strip.presence
  end

  def assign_tl_id
    self[:TL_ID] ||= allocate_tl_id
  end

  def allocate_tl_id
    IdCounter.allocate!("THELOAI")
  rescue ActiveRecord::RecordNotFound
    next_value = self.class.where("TL_ID LIKE ?", "TL%")
      .pluck(:TL_ID)
      .filter_map { |id| id.to_s[/\ATL(\d+)\z/, 1]&.to_i }
      .max.to_i + 1

    format("TL%04d", next_value)
  end
end
