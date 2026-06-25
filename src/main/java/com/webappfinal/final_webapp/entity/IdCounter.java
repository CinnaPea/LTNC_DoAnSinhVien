package com.webappfinal.final_webapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "IdCounter")
public class IdCounter {
    @Id
    @Column(name = "EntityName", nullable = false)
    private String entityName;

    @Column(name = "Prefix", nullable = false)
    private String prefix;

    @Column(name = "CurrentValue", nullable = false)
    private Integer currentValue;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }
}
