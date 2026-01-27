package com.castellanos94.tracking.model;

import java.util.Objects;
import java.util.UUID;

public class Category {
    private String id;
    private String name;
    private String color; // Hex code for UI representation
    private double hourlyRate;

    public Category() {
        this.id = UUID.randomUUID().toString();
    }

    public Category(String name, String color) {
        this(name, color, 10.0);
    }

    public Category(String name, String color, double hourlyRate) {
        this();
        this.name = name;
        this.color = color;
        this.hourlyRate = hourlyRate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
