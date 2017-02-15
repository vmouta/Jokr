package com.jokrapp.server;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestException extends WebApplicationException {
    public RestException(Response.Status status, String message) {
        super(Response.status(status)
            .entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
