package com.vlcnavigation.module.trilateration;

import android.util.Pair;

import java.util.Map;

public class Light {

//    private Light(Pair<Double, Double> posXY, Pair<Double, Double> receiverXPos, Pair<Double, Double> receiverYPos)
//    {
//        this.posXY = posXY;
//        this.receiverXPos = sort(receiverXPos);
//        this.receiverYPos = sort(receiverYPos);
//    }
//
//    private Light(double x, double y, double receiverXPosFloor, double receiverXPosCeil, double receiverYPosFloor, double receiverYPosCeil)
//    {
//        this.posXY = new Pair<Double, Double>(x,y);
//        this.receiverXPos = sort(receiverXPosFloor, receiverXPosCeil);
//        this.receiverYPos = sort(receiverYPosFloor, receiverYPosCeil);
//    }

    private Light(double x, double y, double distance, String description) {
        this.posX = x;
        this.posY = y;
        this.distance = distance;
        this.description = description;
    }

    private Pair<Double, Double> sort(Pair<Double, Double> unsortedPair) { return (unsortedPair.first < unsortedPair.second) ? unsortedPair : new Pair<Double, Double>( unsortedPair.second, unsortedPair.first ); }
    private Pair<Double, Double> sort(double x, double y) { return (x < y) ? new Pair<Double, Double>(x, y) : new Pair<Double, Double>(y, x); }

    @Override
    public String toString()
    {
        return String.format("Light %s is at position (%s, %s)", // has the following ranges: [X (%s - %s), Y (%s - %s)]",
                this.description,
                this.posX, this.posY//,
//                receiverXPos.first, receiverXPos.second,
//                receiverYPos.first, receiverYPos.second
                );
    }

    private double posX;
    private double posY;
//    Pair<Double, Double> posXY;
    private double distance;
    private String description;
    // FIXME: We'll need one more parameter: lambda (wave-length associated from FFT) + getter only

//    public Pair<Double, Double> getPosXY() { return posXY; }
    public double getPosX() { return this.posX; }
    public double getPosY() { return this.posY; }
    public double getDistance() { return this.distance; }
    public String getLabel() { return this.description; }

    public void setPosX(double newPosX) { this.posX = newPosX; }
    public void setPosY(double newPosY) { this.posY = newPosY; }

    public static class Builder{
        // Mandatory
        private double x, y;
        // Optional
        private double distance = -1;
        private String description = "";
        // Methods
        public Builder(double x, double y) { this.x = x; this.y = y; }
        public Builder setDistance(double distance) { this.distance = distance; return this; }
        public Builder setDescription(String label) { this.description = label; return this; }
        // FIXME: We'll need one more setter for lambda (wave-length associated from FFT)
        public Light build() { return new Light(x, y, distance, description); }
    }
}
