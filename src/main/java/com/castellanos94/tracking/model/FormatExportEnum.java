package com.castellanos94.tracking.model;

public enum FormatExportEnum {

    JSON("JSON", ".json"),
    XLSX("XLSX", ".xlsx"),
    CSV("CSV", ".csv"),
    CUSTOM("Custom", ".csv");

    private final String name;
    private final String extension;

    FormatExportEnum(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }
}
