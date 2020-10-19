package com.webapp.backend.validator;

import com.webapp.backend.model.User;
import com.webapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import java.util.regex.Pattern;

@Component
public class UserValidator implements Validator {

    @Autowired
    UserService userService;

    private final Pattern pattern= Pattern.compile("[a-zA-Z ]+");
    private final Pattern patternemail=Pattern.compile("[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    private final Pattern patternPassword=Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,15}$");

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(User.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "error.invalid.firstName", "First Name Required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "error.invalid.lastName", "Last Name Required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "error.invalid.password", "Password Required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.invalid.email","Email Required");


        try {

            if(!pattern.matcher(user.getFirstName()).matches()) {
                errors.rejectValue("firstName","error.invalid.firstName","First Name contains only Alphabet");
            }
            if(!pattern.matcher(user.getLastName()).matches()) {
                errors.rejectValue("lastName","error.invalid.lastName","Last Name contains only Alphabet");
            }
            if(!patternemail.matcher(user.getEmail()).matches()) {
                errors.rejectValue("email","error.invalid.email","Invalid Email ID Format");
            }

            if(!patternPassword.matcher(user.getPassword()).matches()) {
                errors.rejectValue("password","error.invalid.password","Password must be greater than 8 characters,\n must include at least one upper case letter, one lower case letter and one numeric digit");
            }

            User u = userService.findByEmail(user.getEmail());
            if (u != null){
                errors.rejectValue("email", "error.invalid.email", "Email ID already taken");
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
        }

    }


}
