package com.vlcnavigation.module.trilateration;

import android.util.Pair;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
//import org.apache.commons.math;

public class Trilateration {

    public static void addLight(double x, double y) {
        ADDED_LIGHTS.add(new AbstractMap.SimpleEntry<Light, Double>(new Light.Builder(x, y).build(), -1.0));
    }

    public static final double CONST_HEIGHT = 2.5;
    private static List<Map.Entry<Light, Double>> ADDED_LIGHTS = new ArrayList<Map.Entry<Light, Double>>();

    public static void triangulate(Pair<Double, Double> posXY)
    {
        double sqH = Math.pow(CONST_HEIGHT, 2);

        int nbLights = ADDED_LIGHTS.size();
        double[][] positions = new double[nbLights][];
//        for(Map.Entry<Light, Double> lightEntry : ADDED_LIGHTS.entrySet())
        for(int it=0; it<nbLights; it++)
        {
            Light light = ADDED_LIGHTS.get(it).getKey();
            positions[it] = new double[] { light.getPosX(), light.getPosY() };
//            double lowerBound = Math.sqrt(Math.pow(posXY.first - light.getReceiverXPos().first, 2) + Math.pow(posXY.second - light.getReceiverYPos().first, 2) + sqH);
//            double upperBound = Math.sqrt(Math.pow(posXY.first - light.getReceiverXPos().second, 2) + Math.pow(posXY.second - light.getReceiverYPos().second, 2) + sqH);
        }


//        double[][] positions = new double[][] { { 5.0, -6.0 }, { 13.0, -15.0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
        double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

// the answer
        double[] centroid = optimum.getPoint().toArray();

        Timber.d("Position: %s", Arrays.toString(centroid));

// error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);

        /**
         *
         *
         *
         */
    }
}