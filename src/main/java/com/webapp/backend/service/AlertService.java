package com.webapp.backend.service;

import com.webapp.backend.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.LinkedHashSet;

@Service
public class AlertService {

    @Autowired
    AlertRepository repository;

    public void deleteAlertsByWatchID(LinkedHashSet<Long> alerts){
        for (Long alertID: alerts) {
            repository.deleteById(alertID);
        }
    }
}
