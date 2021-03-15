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

    public static final double CONST_HEIGHT = 2.5;
    private static List<Map.Entry<Light, Double>> ADDED_LIGHTS = new ArrayList<Map.Entry<Light, Double>>();

    public static double[] triangulate() throws InsufficientLightsException {
        int nbLights = ADDED_LIGHTS.size();
        if(nbLights < 3)
        {
            throw new InsufficientLightsException(String.format("Not enough lights were registered: currently %s.", nbLights));
        }
        else
        {
            double[][] positions = new double[nbLights][];
            for(int it=0; it<nbLights; it++)
            {
                Light light = ADDED_LIGHTS.get(it).getKey();
                positions[it] = new double[] { light.getPosX(), light.getPosY() };
            }
            double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            // the answer
            double[] centroid = optimum.getPoint().toArray();

            Timber.d("Position: %s", Arrays.toString(centroid));

            // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
            RealVector standardDeviation = optimum.getSigma(0);
            RealMatrix covarianceMatrix = optimum.getCovariances(0);

            return centroid;
        }
    }

    private static class InsufficientLightsException extends Exception {
        public InsufficientLightsException(String message) {
            super(message);
        }
    }
}
