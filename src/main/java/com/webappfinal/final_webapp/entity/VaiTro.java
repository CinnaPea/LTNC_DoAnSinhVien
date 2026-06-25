package com.webappfinal.final_webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "VaiTro")
public class VaiTro {
    @Id
    @Column(name = "VT_ID", nullable = false)
    private String vtId;

    @Column(name = "TenVT", nullable = false)
    private String tenVt;

    public String getVtId() {
        return vtId;
    }

    public void setVtId(String vtId) {
        this.vtId = vtId;
    }

    public String getTenVt() {
        return tenVt;
    }

    public void setTenVt(String tenVt) {
        this.tenVt = tenVt;
    }
}
