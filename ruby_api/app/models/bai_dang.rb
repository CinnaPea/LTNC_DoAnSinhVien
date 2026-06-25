class BaiDang < ApplicationRecord
  self.table_name = "BaiDang"
  self.primary_key = "BD_ID"

  before_validation :normalize_attributes
  before_validation :assign_defaults, on: :create

  validates :BD_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da ton tai" }
  validates :DA_ID, presence: true, length: { maximum: 10 }
  validates :TieuDe, presence: true, length: { maximum: 255 }
  validates :Link, length: { maximum: 500 }, allow_blank: true
  validates :NgayDang, presence: true

  validate :thesis_must_exist

  scope :for_student, ->(sv_id) { where(DA_ID: DoAn.for_student(sv_id).select(:DA_ID)) }
  scope :for_lecturer, ->(gv_id) { where(DA_ID: DoAn.for_lecturer(gv_id).select(:DA_ID)) }
  scope :for_thesis, ->(da_id) { where(DA_ID: da_id) }

  def self.filtered(params)
    records = all
    records = records.for_student(params[:sv_id].to_s.strip) if params[:sv_id].present?
    records = records.for_lecturer(params[:gv_id].to_s.strip) if params[:gv_id].present?
    records = records.for_thesis(params[:da_id].to_s.strip) if params[:da_id].present?
    records.order(:NgayDang, :BD_ID)
  end

  def thesis
    @thesis ||= DoAn.find_by(DA_ID: self[:DA_ID])
  end

  private

  def normalize_attributes
    self[:BD_ID] = self[:BD_ID].to_s.strip.presence
    self[:DA_ID] = self[:DA_ID].to_s.strip.presence
    self[:TieuDe] = self[:TieuDe].to_s.strip.presence
    self[:MoTa] = self[:MoTa].to_s.strip.presence
    self[:Link] = self[:Link].to_s.strip.presence
  end

  def assign_defaults
    self[:BD_ID] ||= allocate_bd_id
    self[:NgayDang] ||= Time.current
  end

  def allocate_bd_id
    IdCounter.allocate!("BAIDANG")
  rescue ActiveRecord::RecordNotFound
    next_value = self.class.where("BD_ID LIKE ?", "BD%")
      .pluck(:BD_ID)
      .filter_map { |id| id.to_s[/\ABD(\d+)\z/, 1]&.to_i }
      .max.to_i + 1

    format("BD%04d", next_value)
  end

  def thesis_must_exist
    errors.add(:DA_ID, "khong ton tai") unless thesis.present?
  end
end
