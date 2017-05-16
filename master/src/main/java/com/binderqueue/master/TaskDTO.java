package com.binderqueue.master;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class TaskDTO {
    private List<Double> params = new ArrayList<Double>();
    private int optimizationId;

    public TaskDTO() {}

    public void addParam(Double param) {
        params.add(param);
    }
    public void setOptimizationId(int optimizationId) {
        this.optimizationId = optimizationId;
    }

    public List<Double> getParams() {
        return params;
    }
    public int getOptimizationId() {
        return optimizationId;
    }

    @Override public String toString() {
        return "TaskDTO{" +
            "params=" + params +
            ", optimizationId=" + optimizationId +
            '}';
    }
}
