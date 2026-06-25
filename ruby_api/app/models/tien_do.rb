class TienDo < ApplicationRecord
  self.table_name = "TienDo"
  self.primary_key = "TD_ID"

  before_validation :normalize_attributes
  before_validation :assign_defaults, on: :create

  validates :TD_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da ton tai" }
  validates :DA_ID, presence: true, length: { maximum: 10 }
  validates :TieuDe, presence: true, length: { maximum: 255 }
  validates :TienDoHienTai, numericality: { greater_than_or_equal_to: 0, less_than_or_equal_to: 100 }, allow_nil: true
  validates :NgayGui, presence: true

  validate :thesis_must_exist

  scope :for_student, ->(sv_id) { where(DA_ID: DoAn.for_student(sv_id).select(:DA_ID)) }
  scope :for_lecturer, ->(gv_id) { where(DA_ID: DoAn.for_lecturer(gv_id).select(:DA_ID)) }
  scope :for_thesis, ->(da_id) { where(DA_ID: da_id) }

  def self.filtered(params)
    records = all
    records = records.for_student(params[:sv_id].to_s.strip) if params[:sv_id].present?
    records = records.for_lecturer(params[:gv_id].to_s.strip) if params[:gv_id].present?
    records = records.for_thesis(params[:da_id].to_s.strip) if params[:da_id].present?
    records.order(:NgayGui, :TD_ID)
  end

  def thesis
    @thesis ||= DoAn.find_by(DA_ID: self[:DA_ID])
  end

  private

  def normalize_attributes
    self[:TD_ID] = self[:TD_ID].to_s.strip.presence
    self[:DA_ID] = self[:DA_ID].to_s.strip.presence
    self[:TieuDe] = self[:TieuDe].to_s.strip.presence
    self[:NoiDung] = self[:NoiDung].to_s.strip.presence
    self[:NhanXet] = self[:NhanXet].to_s.strip.presence
  end

  def assign_defaults
    self[:TD_ID] ||= allocate_td_id
    self[:NgayGui] ||= Time.current
  end

  def allocate_td_id
    IdCounter.allocate!("TIENDO")
  rescue ActiveRecord::RecordNotFound
    next_value = self.class.where("TD_ID LIKE ?", "TD%")
      .pluck(:TD_ID)
      .filter_map { |id| id.to_s[/\ATD(\d+)\z/, 1]&.to_i }
      .max.to_i + 1

    format("TD%04d", next_value)
  end

  def thesis_must_exist
    errors.add(:DA_ID, "khong ton tai") unless thesis.present?
  end
end
