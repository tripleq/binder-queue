package com.binderqueue.master;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class TaskDTO {
    private List<Double> params = new ArrayList<Double>();

    public TaskDTO() {}


    public void addParam(Double param) {
        params.add(param);
    }

    public List<Double> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "params=" + params +
                '}';
    }
}
