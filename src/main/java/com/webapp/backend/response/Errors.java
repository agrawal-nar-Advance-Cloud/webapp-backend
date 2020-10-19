package com.webapp.backend.response;

import java.util.List;

public class Errors {
    private List<Message> errors;

    public Errors(List<Message> e) {
        this.errors = e;
    }

    public List<Message> getError() {
        return errors;
    }

    public void setError(List<Message> error) {
        this.errors = error;
    }
}
