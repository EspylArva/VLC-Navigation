package com.vlcnavigation.module.trilateration;

import android.util.Pair;

import java.util.Map;

public class Light {

    private Light(double x, double y, double distance, String description, Floor floor, double lambda) {
        this.posX = x;
        this.posY = y;
        this.floor = floor;
        this.lambda = lambda;
        this.distance = distance;
        this.description = description;
    }

    private Pair<Double, Double> sort(Pair<Double, Double> unsortedPair) { return (unsortedPair.first < unsortedPair.second) ? unsortedPair : new Pair<Double, Double>( unsortedPair.second, unsortedPair.first ); }
    private Pair<Double, Double> sort(double x, double y) { return (x < y) ? new Pair<Double, Double>(x, y) : new Pair<Double, Double>(y, x); }

    @Override
    public String toString()
    {
        return String.format("Light \"%s\" is at position (%s, %s)",
                this.description,
                this.posX, this.posY);
    }

    private double posX;
    private double posY;
    private double distance;
    private String description;
    private Floor floor;
    private double lambda;

    public String getDescription() { return this.description; }
    public double getPosX() { return this.posX; }
    public double getPosY() { return this.posY; }
    public double getDistance() { return this.distance; }
    public double getLambda() { return this.lambda; }
    public Floor getFloor() { return this.floor; }

    public void setPosX(double newPosX) { this.posX = newPosX; }
    public void setPosY(double newPosY) { this.posY = newPosY; }
    public void setLambda(double newLambda) { this.lambda = newLambda; }
    public void setFloor(Floor newFloor) { this.floor = newFloor; }

    public static class Builder{
        // Mandatory
        private double x, y;
        private Floor floor;
        private double lambda;
        // Optional
        private double distance = -1;
        private String description = "";
        // Methods
        public Builder(double x, double y, Floor floor, double lambda) { this.x = x; this.y = y; this.floor = floor; this.lambda = lambda; }
        public Builder setDistance(double distance) { this.distance = distance; return this; }
        public Builder setDescription(String label) { this.description = label; return this; }
        public Light build() { return new Light(x, y, distance, description, floor, lambda); }
    }
}
