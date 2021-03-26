package com.vlcnavigation.module.trilateration;

public class Floor implements Comparable<Floor> {
    private int order;
    private String description;
    private String filePath;

    public int getOrder() { return this.order; }
    public String getDescription() { return this.description; }
    public String getFilePath() { return this.filePath; }

    public void setOrder(int order) { this.order = order; }
    public void setDescription(String description) { this.description = description; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Floor(int order, String description, String filePath)
    {
        this.order = order;
        this.description = description;
        this.filePath = filePath;
    }

    @Override
    public int compareTo(Floor o) {
        return Integer.compare(this.order,  o.order);
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Floor)
        {
            return ((Floor) o).getOrder() == this.order;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return String.format("Floor %s (%s) can be found at %s", description, order, filePath);
    }
}
