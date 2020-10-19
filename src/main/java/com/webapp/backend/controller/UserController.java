package com.webapp.backend.controller;

import com.webapp.backend.Exception.AuthorizationException;
import com.webapp.backend.config.Authentication;
import com.webapp.backend.model.User;
import com.webapp.backend.response.Errors;
import com.webapp.backend.response.Message;
import com.webapp.backend.service.UserService;
import com.webapp.backend.validator.UserValidator;
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


    @RequestMapping(value="/register", method = RequestMethod.POST,  produces = "application/json")
    public ResponseEntity<Object> createUser( @RequestBody User user, BindingResult result) {

            validator.validate(user, result);

            if (result.hasErrors()) {
                final List<Message> errors = new ArrayList<>();
                result.getFieldErrors().stream()
                        .forEach(new Consumer<FieldError>() {
                            @Override
                            public void accept(FieldError action) {
                                errors.add(new Message(action.getDefaultMessage()));
                            }
                        });
                return new ResponseEntity<>(new Errors(errors), HttpStatus.BAD_REQUEST);
            }else{
                    user.setAccountCreated(new Date());
                    user.setAccountUpdate(new Date());
                    User u = userService.saveUser(user);
                    return new ResponseEntity<>(user,HttpStatus.CREATED);
            }
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> getUserBYID(@PathVariable("id") Long id){
            User user =userService.userBYID(id);
            if(user!= null)
                return new ResponseEntity<>(user,HttpStatus.OK);
            else
                return new ResponseEntity<>(new Message("User not found"),HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="/self", method = RequestMethod.GET,  produces = "application/json")
    public ResponseEntity<Object> userInfo(@RequestHeader HttpHeaders headers){

            User user = null;
            try {
                user = auth.authenticate(headers);

            } catch (AuthorizationException e) {
                return new ResponseEntity<>(new Message(e.getMessage()),HttpStatus.BAD_REQUEST);
            }
            if (user != null)
                return new ResponseEntity<>(user,HttpStatus.OK);
            else
                return new ResponseEntity<>(new Message("Invalid email/password"),HttpStatus.NOT_FOUND);

    }

    @RequestMapping(value="/self", method = RequestMethod.PUT,  produces = "application/json")
    public  ResponseEntity<Object> updateUser(@RequestBody User user,@RequestHeader HttpHeaders headers) {
        User u = null;
            try {
                 u = auth.authenticate(headers);
            }catch (AuthorizationException e) {
                return new ResponseEntity<>(new Message(e.getMessage()),HttpStatus.BAD_REQUEST);
            }
            if (u != null){
                    if(user.getFirstName().isEmpty() || user.getFirstName()==null )
                        return new ResponseEntity<>(new Message("Please enter first name"), HttpStatus.BAD_REQUEST);
                    if(user.getLastName().isEmpty() || user.getLastName()==null )
                        return new ResponseEntity<>(new Message("Please enter last name"), HttpStatus.BAD_REQUEST);
                    if (user.getPassword().isEmpty() || user.getPassword()==null || !validatePassword(user.getPassword())) {
                        return new ResponseEntity<>(new Message("Use Strong Password"), HttpStatus.BAD_REQUEST);
                    }else{
                        user.setAccountUpdate(new Date());
                        User newUser = userService.updateUser(user,u.getEmail());
                        return new ResponseEntity<>(newUser, HttpStatus.OK);
                    }
            }
            else
                return new ResponseEntity<>(new Message("Invalid email/password"), HttpStatus.UNAUTHORIZED);
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
