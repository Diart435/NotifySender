package com.notify.processor.exception;

public class NotDeliveredException extends RuntimeException{
    public NotDeliveredException(String message){
        super(message);
    }
}
