package com.webapp.backend.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.*;

@Entity
public class Watch {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long watchID;

    private Date watchCreated;

    private Date watchUpdated;

    @ManyToOne
    @JoinColumn()
    private User userID;

    private String zipcode;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String status;

    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Alert> alerts = new LinkedHashSet<Alert>();


    public Long getWatchID() {
        return watchID;
    }

    public void setWatchID(Long watchID) {
        this.watchID = watchID;
    }

    public Date getWatchCreated() {
        return watchCreated;
    }

    public void setWatchCreated(Date watchCreated) {
        this.watchCreated = watchCreated;
    }

    public Date getWatchUpdated() {
        return watchUpdated;
    }

    public void setWatchUpdated(Date watchUpdated) {
        this.watchUpdated = watchUpdated;
    }

    public User getUserID() {
        return userID;
    }

    public void setUserID(User userID) {
        this.userID = userID;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public Set<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Set<Alert> alerts) {
        this.alerts = alerts;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Watch{" +
                "watchID=" + watchID +
                ", watchCreated=" + watchCreated +
                ", watchUpdated=" + watchUpdated +
                ", userID=" + userID +
                ", zipcode='" + zipcode + '\'' +
                ", alerts=" + alerts +
                '}';
    }
}
