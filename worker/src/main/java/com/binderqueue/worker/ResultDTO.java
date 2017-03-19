package com.binderqueue.worker;

public class ResultDTO {
    private Double result;

    public void setResult(Double result) {
        this.result = result;
    }

    public Double getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ResultDTO{" +
                "result=" + result +
                '}';
    }
}
