package com.webapp.backend.service;

import com.webapp.backend.model.Alert;
import com.webapp.backend.model.Watch;
import com.webapp.backend.repository.WatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashSet;



@Service
public class WatchService {

    @Autowired
    WatchRepository repository;

    @Autowired
    AlertService alertService;

    public Watch save(Watch watch){
        watch.setWatchCreated(new Date());
        watch.setWatchUpdated(new Date());
        watch.setStatus("Created");

        for (Alert alert: watch.getAlerts()) {
            alert.setAlertCreated(new Date());
            alert.setAlertUpdated(new Date());
        }
        return repository.save(watch);
    }

    public Watch getWatch(Long id){
        return repository.findById(id).orElse(null);
    }

    public Watch updateWatch(Watch watch, Watch upWatch){
        watch.setZipcode(upWatch.getZipcode());
        watch.setAlerts(upWatch.getAlerts());
        watch.setStatus("Updated");

        //Cleanup
        alertService.deleteAlertsByWatchID(alertsByWatchID(watch.getWatchID()));

        watch.setWatchUpdated(new Date());
        for (Alert alert: watch.getAlerts()) {
            alert.setAlertCreated(new Date());
            alert.setAlertUpdated(new Date());
        }
        return  repository.save(watch);
    }

    public void deleteWatch(Long id){
        repository.deleteById(id);
    }

    public LinkedHashSet<Watch> getUserWatches(Long userID){
        return repository.watchesByUserID(userID);
    }

    public LinkedHashSet<Long> alertsByWatchID(Long id){
         return repository.alertsByWatchID(id);
    }

    public Boolean checkUserWatch(Long watchID, Long userID){
        Watch watch  = getWatch(watchID);
        if(watch!=null) {
            LinkedHashSet<Watch> watchList = getUserWatches(userID);
            for (Watch w : watchList) {
                if (w.getWatchID() == watchID)
                    return true;
            }
        }
        return false;
    }

}

