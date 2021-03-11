package com.vlcnavigation.module.trilateration;

public class Floor {
    private String description;
    private String filePath;

    public String getDescription() { return this.description; }
    public String getFilePath() { return this.filePath; }

    public void setDescription(String description) { this.description = description; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Floor(String description, String filePath)
    {
        this.description = description;
        this.filePath = filePath;
    }
}
