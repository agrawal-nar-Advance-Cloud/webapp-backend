package com.webapp.backend.controller;

import com.google.gson.Gson;
import com.webapp.backend.Exception.AuthorizationException;
import com.webapp.backend.config.Authentication;
import com.webapp.backend.model.User;
import com.webapp.backend.model.Watch;
import com.webapp.backend.response.Message;
import com.webapp.backend.service.AlertService;
import com.webapp.backend.service.WatchService;

import org.apache.kafka.common.internals.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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



    @RequestMapping(value="/", method = RequestMethod.POST,  produces = "application/json")
    public ResponseEntity<Object> createWatch(@RequestBody Watch watch, @RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);

            watch.setUserID(user);
            watchService.save(watch);
            //publish watch on kafka
            kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
            return new ResponseEntity<>(watch, HttpStatus.OK);
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
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
                Watch watch  =watchService.getWatch(watchID);
                return new ResponseEntity<>(watch, HttpStatus.OK);
            }
            else
                return new ResponseEntity<>(new Message("Watch not found"),HttpStatus.NOT_FOUND);
        }catch (AuthorizationException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
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
                Watch watch = watchService.getWatch(watchID);
                watch = watchService.updateWatch(watch, updateWatch);
                //publish watch on kafka
                kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
                return new ResponseEntity<>(watch, HttpStatus.ACCEPTED);
            }
            else
                return new ResponseEntity<>(new Message("Watch not found"), HttpStatus.NOT_FOUND);
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
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
                Watch watch = watchService.getWatch(watchID);
                watch.setStatus("Deleted");
                //publish watch on kafka
                kafkaTemplate.send(TOPIC, jsonConverter.toJson(watch));
                watchService.deleteWatch(watchID);
                return new ResponseEntity<>( HttpStatus.NO_CONTENT);
            } else
                return new ResponseEntity<>(new Message("Watch not found"), HttpStatus.NOT_FOUND);
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/all", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> getUserWatches(@RequestHeader HttpHeaders headers ) {
        User user = null;

        try {
            user = auth.authenticate(headers);

            LinkedHashSet<Watch> watch = watchService.getUserWatches(user.getUserID());
            if (watch != null) {
                return new ResponseEntity<>(watch, HttpStatus.OK);
            } else
                return new ResponseEntity<>(new Message("No watch available"), HttpStatus.NO_CONTENT);
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
