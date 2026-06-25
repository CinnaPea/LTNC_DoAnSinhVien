class DoAn < ApplicationRecord
  self.table_name = "DoAn"
  self.primary_key = "DA_ID"

  ACTIVE_STATUS = "\u0110ang ho\u00E0n th\u00E0nh"
  COMPLETED_STATUS = "Ho\u00E0n th\u00E0nh"

  before_validation :normalize_attributes
  before_validation :assign_defaults, on: :create

  validates :DA_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da ton tai" }
  validates :DK_ID, presence: true, length: { maximum: 10 }, uniqueness: { message: "da ton tai" }
  validates :GV_ID, presence: true, length: { maximum: 10 }
  validates :TrangThai, presence: true, length: { maximum: 50 }
  validates :NgayThucHien, presence: true

  validate :registration_must_exist
  validate :registration_must_be_approved
  validate :lecturer_must_match_registration

  scope :for_student, ->(sv_id) { where(DK_ID: DangKy.where(SV_ID: sv_id).select(:DK_ID)) }
  scope :for_lecturer, ->(gv_id) { where(GV_ID: gv_id) }
  scope :with_status, ->(status) { where(TrangThai: status) }

  def self.filtered(params)
    records = all
    records = records.for_student(params[:sv_id].to_s.strip) if params[:sv_id].present?
    records = records.for_lecturer(params[:gv_id].to_s.strip) if params[:gv_id].present?
    records = records.with_status(params[:status].to_s.strip) if params[:status].present?
    records.order(:NgayThucHien, :DA_ID)
  end

  def self.create_from_registration!(registration:, approved_by:)
    existing = find_by(DK_ID: registration[:DK_ID])
    return existing if existing.present?

    create!(
      DK_ID: registration[:DK_ID],
      GV_ID: approved_by,
      TrangThai: ACTIVE_STATUS,
      NgayThucHien: Time.current
    )
  end

  def self.sync_from_approved_registrations!(params = {})
    registrations = DangKy.where(TrangThai: DangKy::APPROVED_STATUS)
    registrations = registrations.where(SV_ID: params[:sv_id].to_s.strip) if params[:sv_id].present?
    registrations = registrations.where(NguoiChapThuan: params[:gv_id].to_s.strip) if params[:gv_id].present?

    registrations.find_each do |registration|
      topic = TheLoai.find_by(TL_ID: registration[:TL_ID])
      approved_by = registration[:NguoiChapThuan].presence || topic&.[](:GV_ID)
      next if approved_by.blank?

      create_from_registration!(registration: registration, approved_by: approved_by)
    end
  end

  def completed?
    self[:TrangThai] == COMPLETED_STATUS
  end

  def registration
    @registration ||= DangKy.find_by(DK_ID: self[:DK_ID])
  end

  private

  def normalize_attributes
    self[:DA_ID] = self[:DA_ID].to_s.strip.presence
    self[:DK_ID] = self[:DK_ID].to_s.strip.presence
    self[:GV_ID] = self[:GV_ID].to_s.strip.presence
    self[:TrangThai] = self[:TrangThai].to_s.strip.presence
  end

  def assign_defaults
    self[:DA_ID] ||= allocate_da_id
    self[:TrangThai] ||= ACTIVE_STATUS
    self[:NgayThucHien] ||= Time.current
  end

  def allocate_da_id
    IdCounter.allocate!("DOAN")
  rescue ActiveRecord::RecordNotFound
    next_value = self.class.where("DA_ID LIKE ?", "DA%")
      .pluck(:DA_ID)
      .filter_map { |id| id.to_s[/\ADA(\d+)\z/, 1]&.to_i }
      .max.to_i + 1

    format("DA%04d", next_value)
  end

  def registration_must_exist
    errors.add(:DK_ID, "khong ton tai") unless registration.present?
  end

  def registration_must_be_approved
    return if registration.blank?

    errors.add(:DK_ID, "chua duoc duyet") unless registration[:TrangThai] == DangKy::APPROVED_STATUS
  end

  def lecturer_must_match_registration
    return if registration.blank?

    approved_by = registration[:NguoiChapThuan].to_s.strip
    errors.add(:GV_ID, "khong khop voi dang ky da duyet") unless approved_by.present? && approved_by == self[:GV_ID]
  end
end
