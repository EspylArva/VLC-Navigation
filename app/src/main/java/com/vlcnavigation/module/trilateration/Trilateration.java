package com.vlcnavigation.module.trilateration;

import android.util.Pair;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

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

    /**
     * Uses trilateration to locate the user.
     * Trilateration uses distances from an object A to at least 3 objects Li and Li XY positions.
     * We will get the distances for A to Li using Li.getDistance() and Li position using Li.getPosX() and Li.getPosY()
     *
     * @param lights Lights used for trilateration
     * @return double[2] array reflecting the XY position of the user
     * @throws InsufficientLightsException if the number of lights is inferior to 3
     */
    public static double[] trilaterate(List<Light> lights) throws InsufficientLightsException, SingularMatrixException {
        if(lights.size() < 3) { throw new InsufficientLightsException(String.format("Not enough lights were registered: currently %s.", lights.size())); }
        else
        {
            double[][] positions = new double[lights.size()][];
            double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };
            for(int it=0; it<lights.size(); it++)
            {
                positions[it] = new double[] { lights.get(it).getPosX(), lights.get(it).getPosY() };
                // distances[it] = lights.get(it).getDistance(); // FIXME When working on trilateration with real data, this line should be uncommented
            }

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
