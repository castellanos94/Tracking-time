package com.castellanos94.tracking.model;

import lombok.Data;

@Data
public class TimeReport {
    private String category;
    private String description;
    private String start;
    private String end;
    private double hours;
    private double rate;
    private double amount;

}
