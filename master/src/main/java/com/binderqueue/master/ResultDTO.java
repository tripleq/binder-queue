package com.binderqueue.master;

import java.util.List;

import java.util.List;

public class ResultDTO {
    private List<Double> results;
    private String status;

    public void setResult(List<Double> results) {
        this.results = results;
    }
    public void setStatus(String status) { this.status = status; }

    public List<Double> getResult() {
        return results;
    }
    public String getStatus() { return status; };

    @Override public String toString() {
        return "ResultDTO{" +
            "results=" + results +
            ", status='" + status + '\'' +
            '}';
    }
}
