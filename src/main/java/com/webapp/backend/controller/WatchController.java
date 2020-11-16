package com.webapp.backend.controller;

import com.google.gson.Gson;
import com.webapp.backend.Exception.AuthorizationException;
import com.webapp.backend.config.Authentication;
import com.webapp.backend.model.User;
import com.webapp.backend.model.Watch;
import com.webapp.backend.response.Message;
import com.webapp.backend.service.AlertService;
import com.webapp.backend.service.WatchService;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.LinkedHashSet;


@RestController
@RequestMapping("/v1/watch")
public class WatchController {


    @Autowired
    Authentication auth;

    @Autowired
    AlertService alertService;

    @Autowired
    WatchService watchService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Gson jsonConverter;

    private final String TOPIC= "watch";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static final Counter publishCount = Counter.build().name("message_published_total_watch").labelNames("status","topic").help("Total message published on watch topic.").register();
    static final Counter watchCount = Counter.build().name("watch_operations_count").labelNames("request").help("Total request to watch endpoint .").register();
    static final Summary watchMetric = Summary.build().name("watch_operation").labelNames("request","operation").help("All watch operations time.").register();
    static final Summary publishTime = Summary.build().name("publish_time_watch").labelNames("status","topic").help("Publish time on watch topic.").register();

    @RequestMapping(value="/", method = RequestMethod.POST,  produces = "application/json")
    public ResponseEntity<Object> createWatch(@RequestBody Watch watch, @RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);

            Summary.Timer requestTimer = watchMetric.labels("POST/","create").startTimer();
            watch.setUserID(user);
            watchService.save(watch);
            requestTimer.observeDuration();
            log.info("Watch created, WatchID:"+ watch.getWatchID());
            //publish watch on kafka
            publishCount.labels("create","watch").inc();

            Summary.Timer kafkaTimer = publishTime.labels("create","watch").startTimer();
            kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
            kafkaTimer.observeDuration();
            watchCount.labels("POST/").inc();
            log.info("Watch published(created), Topic: watch, WatchID:"+watch.getWatchID());
            return new ResponseEntity<>(watch, HttpStatus.OK);
        } catch (AuthorizationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/{watchID}", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> getWatch(@PathVariable("watchID") Long watchID, @RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);
            Boolean flag = watchService.checkUserWatch(watchID,user.getUserID());

            if( flag ) {
                Summary.Timer requestTimer = watchMetric.labels("GET/{watchID}","get_single_watch").startTimer();
                Watch watch  =watchService.getWatch(watchID);
                requestTimer.observeDuration();
                log.info("get watch, WatchID:"+watchID);
                watchCount.labels("GET/{watchID}").inc();
                return new ResponseEntity<>(watch, HttpStatus.OK);
            } else {
                log.info("Watch not found, WatchID:"+watchID);
                return new ResponseEntity<>(new Message("Watch not found"), HttpStatus.NOT_FOUND);
            }
        }catch (AuthorizationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/{watchID}", method = RequestMethod.PUT,  produces = "application/json")
    public ResponseEntity<Object> updateWatch(@RequestBody Watch updateWatch, @PathVariable("watchID") Long watchID, @RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);
            Boolean flag = watchService.checkUserWatch(watchID,user.getUserID());

            if (flag) {
                Summary.Timer requestTimer = watchMetric.labels("PUT/{watchID}","update").startTimer();
                Watch watch = watchService.getWatch(watchID);
                watch = watchService.updateWatch(watch, updateWatch);
                requestTimer.observeDuration();
                log.info("Watch updated, WatchID:"+ watchID);
                //publish watch on kafka
                publishCount.labels("update","watch").inc();
                watchCount.labels("PUT/{watchID}").inc();
                Summary.Timer kafkaTimer = publishTime.labels("update","watch").startTimer();
                kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
                kafkaTimer.observeDuration();

                log.info("Watch published(updated), Topic: watch, WatchID:"+watch.getWatchID());
                return new ResponseEntity<>(watch, HttpStatus.ACCEPTED);
            } else {
                log.info("Watch not found, WatchID:"+watchID);
                return new ResponseEntity<>(new Message("Watch not found"), HttpStatus.NOT_FOUND);
            }
        } catch (AuthorizationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/{watchID}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteWatch(@PathVariable("watchID") Long watchID, @RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);
            Boolean flag = watchService.checkUserWatch(watchID,user.getUserID());

            if (flag) {
                Summary.Timer requestTimer = watchMetric.labels("DELETE/{watchID}","delete").startTimer();
                Watch watch = watchService.getWatch(watchID);
                watch.setStatus("Deleted");
                //publish watch on kafka
                log.info("Watch published(deleted), Topic: watch, WatchID:"+watch.getWatchID());

                Summary.Timer kafkaTimer = publishTime.labels("delete","watch").startTimer();
                kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
                kafkaTimer.observeDuration();

                watchService.deleteWatch(watchID);
                requestTimer.observeDuration();
                publishCount.labels("delete","watch").inc();
                log.info("Watch deleted, WatchID:"+watchID);
                watchCount.labels("DELETE/{watchID}").inc();
                return new ResponseEntity<>( HttpStatus.NO_CONTENT);
            } else {
                log.info("Watch not found, WatchID"+watchID);
                return new ResponseEntity<>(new Message("Watch not found"), HttpStatus.NOT_FOUND);
            }
        } catch (AuthorizationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/all", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> getUserWatches(@RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);
            Summary.Timer requestTimer = watchMetric.labels("GET/all","get_all_watch").startTimer();
            LinkedHashSet<Watch> watch = watchService.getUserWatches(user.getUserID());
            requestTimer.observeDuration();
            if (watch != null) {
                log.info("get all watch of userID:"+user.getUserID());
                watchCount.labels("GET/all").inc();
                return new ResponseEntity<>(watch, HttpStatus.OK);
            } else {
                log.info("No watch available of userID:"+user.getUserID());
                return new ResponseEntity<>(new Message("No watch available"), HttpStatus.NO_CONTENT);
            }
        } catch (AuthorizationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
