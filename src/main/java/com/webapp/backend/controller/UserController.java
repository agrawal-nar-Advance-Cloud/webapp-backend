package com.webapp.backend.controller;

import com.webapp.backend.Exception.AuthorizationException;
import com.webapp.backend.config.Authentication;
import com.webapp.backend.model.User;
import com.webapp.backend.response.Errors;
import com.webapp.backend.response.Message;
import com.webapp.backend.service.UserService;
import com.webapp.backend.validator.UserValidator;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    UserValidator validator;

    @Autowired
    UserService userService;

    @Autowired
    Authentication auth;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static final Counter userCount = Counter.build().name("user_operations_count").labelNames("request").help("Total request to user endpoint .").register();
    static final Summary userMetric = Summary.build().name("user_operations_time").labelNames("request","operation").help("All user operations time.").register();

    @RequestMapping(value="/register", method = RequestMethod.POST,  produces = "application/json")
    public ResponseEntity<Object> createUser( @RequestBody User user, BindingResult result) {
        try {
            validator.validate(user, result);

            if (result.hasErrors()) {
                final List<Message> errors = new ArrayList<>();
                result.getFieldErrors().stream()
                        .forEach(new Consumer<FieldError>() {
                            @Override
                            public void accept(FieldError action) {
                                errors.add(new Message(action.getDefaultMessage()));
                                log.warn(String.valueOf(action.getDefaultMessage()));
                            }
                        });
                return new ResponseEntity<>(new Errors(errors), HttpStatus.BAD_REQUEST);
            } else {
                userCount.labels("GET/register").inc();
                Summary.Timer requestTimer = userMetric.labels("GET/register","create").startTimer();
                user.setAccountCreated(new Date());
                user.setAccountUpdated(new Date());
                userService.saveUser(user);
                requestTimer.observeDuration();
                log.info("User created, UserID:" + user.getUserID());
                return new ResponseEntity<>(user, HttpStatus.CREATED);
            }
        }
        catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> getUserBYID(@PathVariable("id") Long id){
            User user =userService.userBYID(id);
            if(user!= null) {
                log.info("get user, UserID:"+user.getUserID());
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                userCount.labels("GET/{id}").inc();
                log.info("User not found, UserID:"+id);
                return new ResponseEntity<>(new Message("User not found"), HttpStatus.NOT_FOUND);
            }
    }

    @RequestMapping(value="/self", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> userInfo(@RequestHeader HttpHeaders headers){

            User user = null;
            try {
                user = auth.authenticate(headers);
                userCount.labels("GET/self").inc();
                log.info("Get/self UserID:"+user.getUserID());
                return new ResponseEntity<>(user,HttpStatus.OK);

            } catch (AuthorizationException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(new Message(e.getMessage()),HttpStatus.UNAUTHORIZED);
            }catch (Exception e){
                log.error(e.getMessage());
                return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
            }


    }

    @RequestMapping(value="/self", method = RequestMethod.PUT,  produces = "application/json")
    public  ResponseEntity<Object> updateUser(@RequestBody User user,@RequestHeader HttpHeaders headers) {
        User u = null;
            try {
                 u = auth.authenticate(headers);
                if(user.getFirstName().isEmpty() || user.getFirstName()==null ){
                    log.warn("Put/self Please enter first name, UserID:"+user.getUserID());
                    return new ResponseEntity<>(new Message("Please enter first name"), HttpStatus.BAD_REQUEST);
                }
                if(user.getLastName().isEmpty() || user.getLastName()==null ) {
                    log.warn("Put/self Please enter last name, UserID:"+user.getUserID());
                    return new ResponseEntity<>(new Message("Please enter last name"), HttpStatus.BAD_REQUEST);
                }
                if (user.getPassword().isEmpty() || user.getPassword()==null || !validatePassword(user.getPassword())) {
                    log.warn("Put/self Use Strong Password, UserID:"+user.getUserID());
                    return new ResponseEntity<>(new Message("Use Strong Password"), HttpStatus.BAD_REQUEST);
                }else{
                    Summary.Timer requestTimer = userMetric.labels("PUT/self","update").startTimer();
                    user.setAccountUpdated(new Date());
                    User newUser = userService.updateUser(user,u.getEmail());
                    requestTimer.observeDuration();
                    userCount.labels("PUT/self").inc();
                    log.info("User updated, UserID:"+user.getUserID());
                    return new ResponseEntity<>(newUser, HttpStatus.ACCEPTED);
                }
            }catch (AuthorizationException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(new Message(e.getMessage()),HttpStatus.UNAUTHORIZED);
            }catch (Exception e){
                log.error(e.getMessage());
                return new ResponseEntity<>(new Message(e.getMessage()), HttpStatus.BAD_REQUEST);
            }

    }

    public Boolean validatePassword(String password) {
            if (password != null || (!password.equalsIgnoreCase(""))) {
                String pattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,15}$";
                return (password.matches(pattern));
            } else {
                return Boolean.FALSE;
            }
    }

}
