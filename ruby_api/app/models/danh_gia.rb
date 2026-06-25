class DanhGia < ApplicationRecord
  self.table_name = "DanhGia"
  self.primary_key = "DG_ID"

  before_validation :normalize_attributes
  before_validation :assign_defaults, on: :create

  validates :DG_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da ton tai" }
  validates :DA_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da co danh gia" }
  validates :GV_ID, presence: true, length: { maximum: 10 }
  validates :DiemSo, numericality: { greater_than_or_equal_to: 0, less_than_or_equal_to: 10 }, allow_nil: true
  validates :NgayNX, presence: true

  validate :thesis_must_exist
  validate :lecturer_must_match_thesis

  scope :for_student, ->(sv_id) { where(DA_ID: DoAn.for_student(sv_id).select(:DA_ID)) }
  scope :for_lecturer, ->(gv_id) { where(GV_ID: gv_id) }
  scope :for_thesis, ->(da_id) { where(DA_ID: da_id) }

  def self.filtered(params)
    records = all
    records = records.for_student(params[:sv_id].to_s.strip) if params[:sv_id].present?
    records = records.for_lecturer(params[:gv_id].to_s.strip) if params[:gv_id].present?
    records = records.for_thesis(params[:da_id].to_s.strip) if params[:da_id].present?
    records.order(:NgayNX, :DG_ID)
  end

  def save_with_thesis_state!
    transaction do
      save!
      mark_thesis_completed!
      reload
    end
  end

  def destroy_with_thesis_state!
    transaction do
      associated_thesis = thesis
      destroy!
      associated_thesis&.update!(TrangThai: DoAn::ACTIVE_STATUS)
    end
  end

  def thesis
    @thesis ||= DoAn.find_by(DA_ID: self[:DA_ID])
  end

  private

  def normalize_attributes
    self[:DG_ID] = self[:DG_ID].to_s.strip.presence
    self[:DA_ID] = self[:DA_ID].to_s.strip.presence
    self[:GV_ID] = self[:GV_ID].to_s.strip.presence
    self[:NhanXet] = self[:NhanXet].to_s.strip.presence
  end

  def assign_defaults
    self[:DG_ID] ||= allocate_dg_id
    self[:NgayNX] ||= Time.current
  end

  def allocate_dg_id
    IdCounter.allocate!("DANHGIA")
  rescue ActiveRecord::RecordNotFound
    next_value = self.class.where("DG_ID LIKE ?", "DG%")
      .pluck(:DG_ID)
      .filter_map { |id| id.to_s[/\ADG(\d+)\z/, 1]&.to_i }
      .max.to_i + 1

    format("DG%04d", next_value)
  end

  def thesis_must_exist
    errors.add(:DA_ID, "khong ton tai") unless thesis.present?
  end

  def lecturer_must_match_thesis
    return if thesis.blank?

    errors.add(:GV_ID, "khong khop voi do an") unless thesis[:GV_ID] == self[:GV_ID]
  end

  def mark_thesis_completed!
    thesis&.update!(TrangThai: DoAn::COMPLETED_STATUS)
  end
end
