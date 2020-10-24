package com.webapp.backend.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Alert {

    public Alert(){

    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long alertID;

    public enum fieldType{
                            temp,
                            feels_like,
                            temp_min,
                            temp_max,
                            pressure,
                            humidity;
                          }

    @Enumerated(EnumType.STRING)
    private fieldType fieldType;

    private Date alertCreated;

    private Date alertUpdated;

    public enum operator{
                            gt,
                            gte,
                            eq,
                            lt,
                            lte;
                         }

    @Enumerated(EnumType.STRING)
    private operator operator;

    private int value;

    public Long getAlertID() {
        return alertID;
    }

    public void setAlertID(Long alertID) {
        this.alertID = alertID;
    }

    public Date getAlertCreated() {
        return alertCreated;
    }

    public void setAlertCreated(Date alertCreated) {
        this.alertCreated = alertCreated;
    }

    public Date getAlertUpdated() {
        return alertUpdated;
    }

    public void setAlertUpdated(Date alertUpdated) {
        this.alertUpdated = alertUpdated;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Alert.fieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(Alert.fieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Alert.operator getOperator() {
        return operator;
    }

    public void setOperator(Alert.operator operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "alertID=" + alertID +
                ", fieldType=" + fieldType +
                ", alertCreated=" + alertCreated +
                ", alertUpdated=" + alertUpdated +
                ", operator=" + operator +
                ", value='" + value + '\'' +
                '}';
    }
}
