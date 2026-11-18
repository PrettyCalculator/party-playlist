package com.partyplaylist.exception;

public class AccessDeniedException extends ServiceException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
