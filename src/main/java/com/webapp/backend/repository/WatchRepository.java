package com.webapp.backend.repository;

import com.webapp.backend.model.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedHashSet;

public interface WatchRepository extends JpaRepository<Watch,Long> {

    @Query(value= "SELECT * FROM watch w WHERE w.userid_userid= :userID",nativeQuery = true)
    LinkedHashSet<Watch> watchesByUserID(Long userID);

    @Query(value= "SELECT alerts_alertid FROM watch_alerts wa WHERE wa.watch_watchid= :watchID",nativeQuery = true)
    LinkedHashSet<Long> alertsByWatchID(Long watchID);
}
