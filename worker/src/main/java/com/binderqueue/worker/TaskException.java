package com.binderqueue.worker;

/**
 * Created by dzivkovic on 5/14/17.
 */
public class TaskException extends Exception {
    String message;

    public TaskException(String message) {
        super(message);
        this.message = message;
    }

    @Override public String toString() {
        return "TaskException{" +
            "message='" + message + '\'' +
            '}';
    }
}
