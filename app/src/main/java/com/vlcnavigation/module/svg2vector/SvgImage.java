package com.vlcnavigation.module.svg2vector;

import android.util.Pair;

public class SvgImage {

    public SvgImage(String description, String svg, Pair<Integer, Integer> posXY)
    {
        this.description = description;
        this.svg = svg;
        this.posXY = posXY;
    }

    private String description;
    private String svg;
    private Pair<Integer, Integer> posXY;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSvg() { return svg; }
    public void setSvg(String svg) { this.svg = svg; }
    public Pair<Integer, Integer> getPosXY() { return posXY; }
    public void setPosXY(Pair<Integer, Integer> posXY) { this.posXY = posXY; }
}
