package com.example.umansia.beacon.ui.main;

import android.util.Log;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.DoubleArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

/**
 * Created by Umansia on 12.04.2017.
 */

public class Positioner {

    public static List<Beacon> BEACONS = new ArrayList<>();
    public static Map<Date,Vector2D > POSITIONS = new HashMap<>();
    public static Map<Date,Vector3D > POSITIONS3D = new HashMap<>();

    public static List<Beacon> getCurrentFlootBeacons()
    {
        List<Beacon> floorBeacons = new ArrayList<>();
        for (Beacon item:
                BEACONS ) {
            if(item.floor == CurrentFloor)
            {
                floorBeacons.add(item);
            }
        }

        return  floorBeacons;
    }

    public static Date LogStartDate;

    public static int CurrentFloor;

    public static  Vector3D GetPositionTri()
    {
        try {


            double[][] allpositions = new double[BEACONS.size()][4];


            Integer counter = 0;
            for(Iterator<Beacon> i = BEACONS.iterator(); i.hasNext(); )
            {
                Beacon item = i.next();
                allpositions[counter] = new double[]{item.Position.getX(), item.Position.getY(),item.Position.getZ(), item.getDistance()};
                counter++;
            }

            Arrays.sort(allpositions, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {

                    if(o1[3] < o2[3]) {
                        return -1;
                    }
                    else
                    {
                        return  1;
                    }
                }
            });

            List<double[]> truePositions = new ArrayList<>();


            for (int i = 0;i <allpositions.length;i++) {
                if(allpositions[i][3] > Beacon.C  && truePositions.size() < 4
                        )
                    truePositions.add(allpositions[i]);

            }

            double[][] positions = new double[truePositions.size()][4];
            truePositions.toArray(positions);


            ArrayList<Vector3D> positionV = new ArrayList<>();

            double averageX = 0;
            double averageY = 0;
            double averageZ = 0;


            //Öncelikle beacon'ların orta noktası hesaplanıp averageVector değişkenine atanıyor.
            for (int i = 0;i <positions.length;i++) {

                positionV.add(new Vector3D(positions[i][0],positions[i][1],positions[i][2]));
                averageX += positions[i][0];
                averageY += positions[i][1];
                averageZ += positions[i][2];
            }


            double[][] Tripositions = new double[positions.length][3];
            double[] Tridistances = new double[positions.length];

            Integer Tricounter = 0;
            for (int i = 0;i <positions.length;i++) {
                Tripositions[Tricounter] = new double[]{positions[i][0],positions[i][1],positions[i][2]};
                Tridistances[Tricounter] = positions[i][3];
                Tricounter++;
            }

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(Tripositions, Tridistances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            double[] calculatedPosition = optimum.getPoint().toArray();

            //POSITIONS.put(new Date(), new Vector2D(calculatedPosition[0],calculatedPosition[1]));

            return new Vector3D(calculatedPosition[0],calculatedPosition[1],calculatedPosition[2]);
        }
        catch (Exception ex)
        {
            return new Vector3D(0,0,0);
        }
    }

    public static Vector3D GetPosition3D() {

        try {


            double[][] allpositions = new double[BEACONS.size()][4];


            Integer counter = 0;
            for(Iterator<Beacon> i = BEACONS.iterator(); i.hasNext(); )
            {
                Beacon item = i.next();
                allpositions[counter] = new double[]{item.Position.getX(), item.Position.getY(),item.Position.getZ(), item.getDistance()};
                counter++;
            }

            Arrays.sort(allpositions, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {

                    if(o1[3] < o2[3]) {
                        return -1;
                    }
                    else
                    {
                        return  1;
                    }
                }
            });

            List<double[]> truePositions = new ArrayList<>();


            for (int i = 0;i <allpositions.length;i++) {
                if(allpositions[i][3] > Beacon.C  && truePositions.size() < 4
                        )
                    truePositions.add(allpositions[i]);

            }

            double[][] positions = new double[truePositions.size()][4];
            truePositions.toArray(positions);


            ArrayList<Vector3D> positionV = new ArrayList<>();

            double averageX = 0;
            double averageY = 0;
            double averageZ = 0;


            //Öncelikle beacon'ların orta noktası hesaplanıp averageVector değişkenine atanıyor.
            for (int i = 0;i <positions.length;i++) {

                positionV.add(new Vector3D(positions[i][0],positions[i][1],positions[i][2]));
                averageX += positions[i][0];
                averageY += positions[i][1];
                averageZ += positions[i][2];
            }


            double[][] Tripositions = new double[positions.length][3];
            double[] Tridistances = new double[positions.length];

            Integer Tricounter = 0;
            for (int i = 0;i <positions.length;i++) {
                Tripositions[Tricounter] = new double[]{positions[i][0],positions[i][1],positions[i][2]};
                Tridistances[Tricounter] = positions[i][3];
                Tricounter++;
            }

            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(Tripositions, Tridistances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            double[] calculatedPosition = optimum.getPoint().toArray();



            Vector3D averageVector = new Vector3D(averageX / positionV.size(),averageY / positionV.size(),averageZ / positionV.size());

            double calculatedPosition_x  = 0;
            double calculatedPosition_y  = 0;
            double calculatedPosition_z  = 0;

            double powerValue = 2;
            double totalPowerValue = 0;

            double summer = 0;

            //Beacon'ın pozisyonu merkez, uzaklık değeri de yarıçap olacak şekilde çember çizildiğinde, çemberin orta noktaya (averegeVector) en yakın noktası ile
            // averageVector arasında karşılaştırma yapılıyor. Eğer çemberin en yakın noktası X ekseni üzerinde averageVector'un solunda kalıyorsa negatif etki, sağında kalıyorsa pozitif etki yaparak
            // summer değişkenine etki ediyor.
            // Bu yarattığı etki ise averageVector ile çembere en yakın nokta arasındaki uzaklığı X eksenine izdüşümünün uzunluğunun karesi.
            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][3] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][3]);
                double xDifference = Math.abs(averageVector.getX() - positionV.get(i).getX());


                if((averageVector.distance(positionV.get(i)) > positions[i][3] && positionV.get(i).getX() > averageVector.getX()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][3] && positionV.get(i).getX() < averageVector.getX())
                        ) {
                    summer += Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
            }

            //Summer değeri karekökü averageVector noktasına etki ettirilerek sonuç pozisyonunun X değeri bulunuyor.
            if(summer > 0) {
                calculatedPosition_x = averageVector.getX() + (Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())));// / positionV.size();
            }
            else
            {
                calculatedPosition_x = averageVector.getX() - (Math.pow(Math.abs(summer), 1  / (totalPowerValue / positionV.size())));// / positionV.size();
            }


            //Benzer hesaplama Y ekseni için de geçerli.
            summer = 0;
            powerValue = 2;
            totalPowerValue = 0;
            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][3] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][3]);
                double yDifference = Math.abs(averageVector.getY() - positionV.get(i).getY());

                if((averageVector.distance(positionV.get(i)) > positions[i][3] && positionV.get(i).getY() > averageVector.getY()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][3] && positionV.get(i).getY() < averageVector.getY())
                        ) {
                    summer += Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition_y = averageVector.getY()   + Math.pow(Math.abs(summer),1 / (totalPowerValue / positionV.size()));//  / positionV.size();
            }
            else
            {
                calculatedPosition_y = averageVector.getY() - Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())) ;// / positionV.size();
            }

            summer = 0;
            powerValue = 2;
            totalPowerValue = 0;

            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][3] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][3]);
                double zDifference = Math.abs(averageVector.getZ() - positionV.get(i).getZ());

                if((averageVector.distance(positionV.get(i)) > positions[i][3] && positionV.get(i).getZ() > averageVector.getZ()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][3] && positionV.get(i).getZ() < averageVector.getZ())
                        ) {
                    summer += Math.pow(vectorLength * zDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * zDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition_z = averageVector.getZ()   + Math.pow(Math.abs(summer),1 / (totalPowerValue / positionV.size()));//  / positionV.size();
            }
            else
            {
                calculatedPosition_z = averageVector.getZ() - Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())) ;// / positionV.size();
            }

            calculatedPosition_z = calculatedPosition[2];
            POSITIONS3D.put(new Date(), new Vector3D(calculatedPosition_x,calculatedPosition_y,calculatedPosition_z));

            return new Vector3D(calculatedPosition_x,calculatedPosition_y,calculatedPosition_z);
        }
        catch (Exception ex)
        {
            return new Vector3D(0,0);
        }
    }

    public static Vector2D GetPosition() {

        try {
            //BEACONS list contains information about all beacons found in the system.
            double[][] allBeacons = new double[BEACONS.size()][4];

            Integer counter = 0;
            for(Iterator<Beacon> i = BEACONS.iterator(); i.hasNext(); )
            {
                Beacon item = i.next();
                allBeacons[counter] = new double[]{item.Position.getX(), item.Position.getY(), item.getDistance(),item.floor};
                counter++;
            }

            //Beacon array sorted according to their distance values in ascending order
            Arrays.sort(allBeacons, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {

                    if(o1[2] < o2[2]) {
                        return -1;
                    }
                    else
                    {
                        return  1;
                    }
                }
            });

            List<double[]> closestBeacons = new ArrayList<>();


            //Beacons have floor property that gives informatino about their placement on z-axis. For testing purpose, assumend that there is only two floor in the system.
            double firstFloorCount = 0;
            double secondFloorCount = 1;


            //4 closest beacons found and added to closestBecons list. Alse first and second floor beacon counts are counted on these 4 beacons.
            for (int i = 0;i <allBeacons.length;i++) {
                if(allBeacons[i][2] > Beacon.C  && closestBeacons.size() < 4) {
                    closestBeacons.add(allBeacons[i]);
                    if (allBeacons[i][3] == 1) {
                        firstFloorCount++;
                    } else {
                        secondFloorCount++;
                    }
                }

            }

            //closestBeacons are stored in array for calculation again.
            double[][] positions = new double[closestBeacons.size()][4];
            closestBeacons.toArray(positions);


            ArrayList<Vector2D> positionV = new ArrayList<>();

            double averageX = 0;
            double averageY = 0;

            //CurretFloor is assigned according to beacon count of the first and second floor. If both floor have same beacon count on  beacon list,
            // closest beacon's floot is assumed as CurrentFloor
            if(firstFloorCount > secondFloorCount)
            {
                CurrentFloor = 1;
            }
            else if(secondFloorCount > firstFloorCount)
            {
                CurrentFloor = 2;
            }
            else
            {
                CurrentFloor = (int) positions[0][3];
            }


            //Average Point of 4 beacons calculated.
            for (int i = 0;i <positions.length;i++) {

                positionV.add(new Vector2D(positions[i][0],positions[i][1]));
                averageX += positions[i][0];
                averageY += positions[i][1];
            }

            Vector2D averageVector = new Vector2D(averageX / positionV.size(),averageY / positionV.size());

            double calculatedPosition_x  = 0;
            double calculatedPosition_y  = 0;

            double powerValue = 2;
            double totalPowerValue = 0;
            double summer = 0;


            //For every vector, their contribution to x value of calculated position is calculated and added to summer variable.
            for (int i = 0;i <positionV.size();i++) {

                //Power value calculated.
                powerValue = (0.18) / positions[i][2] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][2]);
                double xDifference = Math.abs(averageVector.getX() - positionV.get(i).getX());

                //x component of the vector  calculated and it's power taken. Then, added to summer variable.
                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getX() > averageVector.getX()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getX() < averageVector.getX())
                        ) {
                    summer += Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
            }

            //result of the vector contributions added to average vector to find, x component of the final result.
                if(summer > 0) {
                calculatedPosition_x = averageVector.getX() + (Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())));
            }
            else
            {
                calculatedPosition_x = averageVector.getX() - (Math.pow(Math.abs(summer), 1  / (totalPowerValue / positionV.size())));
            }


            //Similiar calculation is done for y-axis
            summer = 0;
            powerValue = 2;
            totalPowerValue = 0;
            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][2] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][2]);
                double yDifference = Math.abs(averageVector.getY() - positionV.get(i).getY());

                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getY() > averageVector.getY()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getY() < averageVector.getY())
                        ) {
                    summer += Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition_y = averageVector.getY()   + Math.pow(Math.abs(summer),1 / (totalPowerValue / positionV.size()));
            }
            else
            {
                calculatedPosition_y = averageVector.getY() - Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())) ;
            }

            //Calculated position added to POSITIONS list for logging.
            POSITIONS.put(new Date(), new Vector2D(calculatedPosition_x,calculatedPosition_y));

            return new Vector2D(calculatedPosition_x,calculatedPosition_y);
        }
        catch (Exception ex)
        {
            return new Vector2D(0,0);
        }
    }

    public static Vector2D GetPositionNear() {

        try {


            double[][] allpositions = new double[BEACONS.size()][3];


            Integer counter = 0;
            for(Iterator<Beacon> i = BEACONS.iterator(); i.hasNext(); )
            {
                Beacon item = i.next();
                allpositions[counter] = new double[]{item.Position.getX(), item.Position.getY(), item.getDistance()};
                counter++;
            }

            Arrays.sort(allpositions, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {

                    if(o1[2] < o2[2]) {
                        return -1;
                    }
                    else
                    {
                        return  1;
                    }
                }
            });

            Vector2D tempVector = new Vector2D(allpositions[0][0],allpositions[0][1]);

            Arrays.sort(allpositions, new Comparator<double[]>() {
                @Override
                public int compare(double[] o1, double[] o2) {

                    if(Vector2D.distance(tempVector,new Vector2D(o1[0],o1[1])) < Vector2D.distance(tempVector,new Vector2D(o2[0],o2[1]))) {
                        return -1;
                    }
                    else
                    {
                        return  1;
                    }
                }
            });

            List<double[]> truePositions = new ArrayList<>();


            for (int i = 0;i <allpositions.length;i++) {
                if(allpositions[i][2] > Beacon.C  && truePositions.size() < 4
                        )
                    truePositions.add(allpositions[i]);
            }

            double[][] positions = new double[truePositions.size()][3];
            truePositions.toArray(positions);

            truePositions.toArray(positions);

            ArrayList<Vector2D> positionV = new ArrayList<>();

            double averageX = 0;
            double averageY = 0;


            //Öncelikle beacon'ların orta noktası hesaplanıp averageVector değişkenine atanıyor.
            for (int i = 0;i <positions.length;i++) {

                positionV.add(new Vector2D(positions[i][0],positions[i][1]));
                averageX += positions[i][0];
                averageY += positions[i][1];
            }

            Vector2D averageVector = new Vector2D(averageX / positionV.size(),averageY / positionV.size());

            double calculatedPosition_x  = 0;
            double calculatedPosition_y  = 0;

            double powerValue = 2;
            double totalPowerValue = 0;

            double summer = 0;

            //Beacon'ın pozisyonu merkez, uzaklık değeri de yarıçap olacak şekilde çember çizildiğinde, çemberin orta noktaya (averegeVector) en yakın noktası ile
            // averageVector arasında karşılaştırma yapılıyor. Eğer çemberin en yakın noktası X ekseni üzerinde averageVector'un solunda kalıyorsa negatif etki, sağında kalıyorsa pozitif etki yaparak
            // summer değişkenine etki ediyor.
            // Bu yarattığı etki ise averageVector ile çembere en yakın nokta arasındaki uzaklığı X eksenine izdüşümünün uzunluğunun karesi.
            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][2] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][2]);
                double xDifference = Math.abs(averageVector.getX() - positionV.get(i).getX());


                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getX() > averageVector.getX()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getX() < averageVector.getX())
                        ) {
                    summer += Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * xDifference / averageVector.distance(positionV.get(i)), powerValue);
                }
            }

            //Summer değeri karekökü averageVector noktasına etki ettirilerek sonuç pozisyonunun X değeri bulunuyor.
            if(summer > 0) {
                calculatedPosition_x = averageVector.getX() + (Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())));// / positionV.size();
            }
            else
            {
                calculatedPosition_x = averageVector.getX() - (Math.pow(Math.abs(summer), 1  / (totalPowerValue / positionV.size())));// / positionV.size();
            }


            //Benzer hesaplama Y ekseni için de geçerli.
            summer = 0;
            powerValue = 2;
            totalPowerValue = 0;
            for (int i = 0;i <positionV.size();i++) {
                powerValue = (0.18) / positions[i][2] + 1;
                totalPowerValue += powerValue;

                double vectorLength = Math.abs(averageVector.distance(positionV.get(i)) - positions[i][2]);
                double yDifference = Math.abs(averageVector.getY() - positionV.get(i).getY());

                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getY() > averageVector.getY()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getY() < averageVector.getY())
                        ) {
                    summer += Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
                else
                {
                    summer -= Math.pow(vectorLength * yDifference / (averageVector.distance(positionV.get(i))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition_y = averageVector.getY()   + Math.pow(Math.abs(summer),1 / (totalPowerValue / positionV.size()));//  / positionV.size();
            }
            else
            {
                calculatedPosition_y = averageVector.getY() - Math.pow(Math.abs(summer), 1 / (totalPowerValue / positionV.size())) ;// / positionV.size();
            }

            POSITIONS.put(new Date(), new Vector2D(calculatedPosition_x,calculatedPosition_y));

            return new Vector2D(calculatedPosition_x,calculatedPosition_y);
        }
        catch (Exception ex)
        {
            return new Vector2D(0,0);
        }
    }

//    public static Vector2D GetPosition() {
//
//        try {
//
//            double[][] positions = new double[BEACONS.size()][];
//            double[] distances = new double[BEACONS.size()];
//
//            int counter = 0;
//            for(Iterator<Beacon> i = BEACONS.iterator(); i.hasNext(); )
//            {
//                Beacon item = i.next();
//                positions[counter] = new double[]{item.Position.getX(), item.Position.getY()};
//                distances[counter] = item.getDistance();
//                counter++;
//            }
//
//            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
//            LeastSquaresOptimizer.Optimum optimum = solver.solve();
//
//            double[] calculatedPosition = optimum.getPoint().toArray();
//
//            return new Vector2D(calculatedPosition[0],calculatedPosition[1]);
//        }
//        catch (Exception ex)
//        {
//            return  new Vector2D(0,0);
//        }
//    }


    public static void SendSignals() throws ExecutionException, InterruptedException {
        String query = "";

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");

        for (Beacon item:
             BEACONS) {

            for(Iterator<Map.Entry<Date, Integer>> it = item.Signals.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Date, Integer> signal = it.next();

                if((signal.getKey().getTime() - LogStartDate.getTime()) > 0)
                {
                    String reportDate = df.format(signal.getKey());
                    query += String.format("insert into TBL_SIGNALS values(%1$d,'" + reportDate + "',%2$d);", item.Minor, signal.getValue());
                }
            }
        }

        Integer positionGroupID = (new CreatePositionGroup()).execute().get();

        for(Iterator<Map.Entry<Date, Vector2D>> it = POSITIONS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Date, Vector2D> position = it.next();
            String reportDate = df.format(position.getKey());

            query += String.format("insert into TBL_POSITIONS select '" + reportDate + "',%1$,.2f,%2$,.2f," + positionGroupID + ";", position.getValue().getX(), position.getValue().getY());
        }

        query = query.replace("NaN","0");



        (new SendSignal()).execute(query);
    }
}
