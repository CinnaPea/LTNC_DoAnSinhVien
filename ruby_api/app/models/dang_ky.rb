class DangKy < ApplicationRecord
  self.table_name = "DangKy"
  self.primary_key = "DK_ID"

  PENDING_STATUSES = ["Ch\u1EDD duy\u1EC7t", "\u0110ang ki\u1EC3m duy\u1EC7t"].freeze
  APPROVED_STATUS = "\u0110\u00E3 duy\u1EC7t"
  REJECTED_STATUS = "T\u1EEB ch\u1ED1i"

  validates :SV_ID, presence: true
  validates :TL_ID, presence: true

  validate :student_must_exist
  validate :topic_must_exist
  validate :topic_must_be_open, on: :create
  validate :no_duplicate_topic_registration, on: :create

  before_validation :assign_defaults, on: :create

  def self.register!(sv_id:, tl_id:, ghi_chu: nil)
    transaction do
      registration = new(SV_ID: sv_id, TL_ID: tl_id, GhiChu: ghi_chu)
      registration.save!
      registration.reload
    end
  end

  def approve!(approved_by:)
    ensure_pending!
    ensure_topic_owned_by!(approved_by)

    transaction do
      update!(
        TrangThai: APPROVED_STATUS,
        NguoiChapThuan: approved_by,
        NgayChapThuan: Time.current
      )

      DoAn.create_from_registration!(registration: self, approved_by: approved_by)
      reload
    end
  end

  def reject!(approved_by:)
    ensure_pending!
    ensure_topic_owned_by!(approved_by)

    update!(
      TrangThai: REJECTED_STATUS,
      NguoiChapThuan: approved_by,
      NgayChapThuan: Time.current
    )
  end

  def pending?
    PENDING_STATUSES.include?(self[:TrangThai])
  end

  private

  def assign_defaults
    self[:DK_ID] ||= IdCounter.allocate!("DANGKY")
    self[:NgayDangKy] ||= Time.current
  end

  def student_must_exist
    student_id = self[:SV_ID]
    errors.add(:SV_ID, "khong ton tai") unless student_id.present? && SinhVien.exists?(SV_ID: student_id)
  end

  def topic_must_exist
    errors.add(:TL_ID, "khong ton tai") unless topic.present?
  end

  def topic_must_be_open
    return if topic.blank?
    errors.add(:TL_ID, "khong mo dang ky") unless topic.open?
  end

  def no_duplicate_topic_registration
    student_id = self[:SV_ID]
    topic_id = self[:TL_ID]
    registration_id = self[:DK_ID]

    return unless student_id.present? && topic_id.present?
    return unless self.class.where(SV_ID: student_id, TL_ID: topic_id).where.not(DK_ID: registration_id).exists?

    errors.add(:base, "Sinh vien da dang ky de tai nay")
  end

  def topic
    @topic ||= TheLoai.find_by(TL_ID: self[:TL_ID])
  end

  def ensure_pending!
    raise StandardError, "Dang ky khong con o trang thai cho duyet" unless pending?
  end

  def ensure_topic_owned_by!(gv_id)
    raise StandardError, "Giang vien khong duoc phep xu ly dang ky nay" unless topic&.[](:GV_ID) == gv_id
  end
end
