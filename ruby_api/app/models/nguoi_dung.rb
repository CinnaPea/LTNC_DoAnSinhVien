class NguoiDung < ApplicationRecord
  self.table_name = "NguoiDung"
  self.primary_key = "ND_ID"

  ACADEMIC_ROLE_IDS = %w[SV GV].freeze

  before_validation :normalize_admin_fields
  before_validation :assign_generated_id, on: :create
  before_validation :apply_default_status, on: :create
  before_validation :apply_timestamps

  validate :validate_required_fields
  validate :validate_unique_username
  validate :validate_unique_email
  validate :validate_role

  scope :with_role, ->(vt_id) { where(VT_ID: vt_id) }
  scope :with_status, ->(active) { where(TrangThai: active) }
  scope :search, lambda { |query|
    sanitized = sanitize_sql_like(query.to_s.strip)
    where("ND_ID LIKE :q OR Username LIKE :q OR Email LIKE :q", q: "%#{sanitized}%")
  }

  def self.filtered(params)
    records = all
    records = records.with_role(params[:role].to_s.strip) if params[:role].present?

    if params[:status].present?
      normalized = params[:status].to_s.strip.downcase
      active = %w[true 1 active enabled].include?(normalized)
      inactive = %w[false 0 inactive disabled].include?(normalized)
      records = records.with_status(active) if active || inactive
    end

    records = records.search(params[:q]) if params[:q].present?
    records.order(:Username, :ND_ID)
  end

  def role_name
    @role_name ||= VaiTro.find_by(VT_ID: self[:VT_ID])&.[](:TenVT)
  end

  def profile_type
    case self[:VT_ID].to_s.strip.upcase
    when "SV"
      "SinhVien"
    when "GV"
      "GiangVien"
    else
      nil
    end
  end

  def profile_id
    case profile_type
    when "SinhVien"
      student_profile&.[](:SV_ID)
    when "GiangVien"
      lecturer_profile&.[](:GV_ID)
    end
  end

  def profile_name
    case profile_type
    when "SinhVien"
      student_profile&.[](:HoTen)
    when "GiangVien"
      lecturer_profile&.[](:HoTen)
    end
  end

  def active?
    self[:TrangThai] != false
  end

  def assign_password(raw_password)
    return if raw_password.nil?

    self[:PassHash] = raw_password.to_s
  end

  def ensure_profile_linked!
    return unless ACADEMIC_ROLE_IDS.include?(self[:VT_ID].to_s.upcase)
    return if profile_record.present?

    raise ActiveRecord::RecordNotFound, "Ho so lien ket khong duoc trigger tao ra."
  end

  def sync_profile_name!(profile_name)
    return if profile_name.to_s.strip.blank?

    linked_profile = profile_record
    raise ActiveRecord::RecordNotFound, "Ho so lien ket khong duoc trigger tao ra." if linked_profile.nil?

    linked_profile.update!(HoTen: profile_name.to_s.strip)
  end

  def destroy_with_linked_profile!
    transaction do
      destroy!
    end
  end

  private

  def student_profile
    @student_profile ||= SinhVien.find_by(ND_ID: self[:ND_ID])
  end

  def lecturer_profile
    @lecturer_profile ||= GiangVien.find_by(ND_ID: self[:ND_ID])
  end

  def profile_record
    case profile_type
    when "SinhVien"
      student_profile
    when "GiangVien"
      lecturer_profile
    end
  end

  def normalize_admin_fields
    self[:Username] = self[:Username].to_s.strip.presence
    self[:Email] = self[:Email].to_s.strip.presence
    self[:VT_ID] = self[:VT_ID].to_s.strip.upcase.presence
    self[:PassHash] = self[:PassHash].to_s.presence
  end

  def assign_generated_id
    self[:ND_ID] = IdCounter.allocate!("NGUOIDUNG") if self[:ND_ID].blank?
  end

  def apply_default_status
    self[:TrangThai] = true if self[:TrangThai].nil?
  end

  def apply_timestamps
    now = Time.current
    self[:NgayLap] ||= now
    self[:CapNhat] = now
  end

  def validate_required_fields
    errors.add(:Username, "khong duoc de trong") if self[:Username].blank?
    errors.add(:Email, "khong duoc de trong") if self[:Email].blank?
    errors.add(:PassHash, "khong duoc de trong") if self[:PassHash].blank?
  end

  def validate_unique_username
    return if self[:Username].blank?

    duplicate = self.class.where("LOWER(Username) = ?", self[:Username].downcase)
    duplicate = duplicate.where.not(ND_ID: self[:ND_ID]) if self[:ND_ID].present?
    errors.add(:Username, "da ton tai") if duplicate.exists?
  end

  def validate_unique_email
    return if self[:Email].blank?

    duplicate = self.class.where("LOWER(Email) = ?", self[:Email].downcase)
    duplicate = duplicate.where.not(ND_ID: self[:ND_ID]) if self[:ND_ID].present?
    errors.add(:Email, "da ton tai") if duplicate.exists?
  end

  def validate_role
    return if self[:VT_ID].blank?
    return if ACADEMIC_ROLE_IDS.include?(self[:VT_ID].to_s.upcase) || self[:VT_ID].to_s.upcase == "AD"

    errors.add(:VT_ID, "khong hop le")
  end
end
