package com.example.umansia.beacon.ui.main;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 * Created by Umansia on 16.03.2017.
 */

public class Positioning
{

    /**
     * lemmingapex.trilateration projesindeki Trilateration fonksiyonu ile pozisyon hesaplama.
     */
   public static double[] getPositionM1(double[][] positions, double[] distances ) {

    try {
        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        double[] calculatedPosition = optimum.getPoint().toArray();

        return calculatedPosition;
    }
    catch (Exception ex)
    {
        return  new double[]{0,0};
    }
}


    /**
     * lemmingapex.trilateration projesindeki Trilateration fonksiyonunun en yakın 3 beacon ile işleme sokularak pozisyonun hesaplanması..
     */
    public static double[] getPositionM2(double[][] positions, double[] distances ) {

        try {

            double min1 = Integer.MAX_VALUE;
            double min2 = Integer.MAX_VALUE;
            double min3 = Integer.MAX_VALUE;

            // En yakın 3 beacon'ın indexleri min1,min2,min3 e atanıyor.
            for (int i = 0; i < distances.length; i++)
            {
                if (distances[i] < min1)
                {
                    min3 = min2; min2 = min1; min1 = i;
                }
                else if (distances[i] < min2)
                {
                    min3 = min2; min2 = i;
                }
                else if (distances[i] < min3)
                {
                    min3 = i;
                }
            }

            List<double[]> nearestPositions = new ArrayList<>();
            List<Double> nearestDistances = new ArrayList<>();

            //En yakın beacon pozisyonları ve uzaklıklar yeni listelere ekleniyor.
            for (int i = 0; i < distances.length; i++){
                if(i == min1 || i == min2 || i == min3)
                {
                    nearestPositions.add(positions[i]);
                    nearestDistances.add(distances[i]);
                }
            }


            double[] target = new double[nearestDistances.size()];
            for (int i = 0; i < target.length; i++) {
                target[i] = nearestDistances.get(i);
            }

            //Yeni listeler NonLinearLeastSquaresSolver'a gönderilerek pozisyon hesaplanıyor.
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                    new TrilaterationFunction(
                            nearestPositions.toArray(new double[3][2]),
                            target
                            ), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();

            double[] calculatedPosition = optimum.getPoint().toArray();

            return calculatedPosition;
        }
        catch (Exception ex)
        {
            return  new double[]{0,0};
        }
    }

    /**
     * 4 beacon'ın 3'lü kombinasyonlarından 4 farklı eşleşme yaratıp bu eşleşmelerin trilateration sonuçlarının ortalamasının alındığı hesaplama.
     */
    public static double[] getPositionM3(double[][] positions, double[] distances ) {

        try {

            // 4 farklı, üçlü beacon grubunun verileri ayrı ayrı getMeetingPoints fonksyionun gönderilerek pozisyon hesaplanıyor.
            double[] result1 = getMeetingPoints(new double[][]{positions[0],positions[1],positions[2]},new double[]{distances[0],distances[1],distances[2]});
            double[] result2 = getMeetingPoints(new double[][]{positions[0],positions[1],positions[3]},new double[]{distances[0],distances[1],distances[3]});
            double[] result3 = getMeetingPoints(new double[][]{positions[0],positions[2],positions[3]},new double[]{distances[0],distances[2],distances[3]});
            double[] result4 = getMeetingPoints(new double[][]{positions[1],positions[2],positions[3]},new double[]{distances[1],distances[2],distances[3]});




            //4 pozisyonun ortalaması alınarak son pozisyon hesaplanıyor.
            double[] result = new double[]{
                    (result1[0] + result2[0] + result3[0] + result4[0]) / 4,
                    (result1[1] + result2[1] + result3[1] + result4[1]) / 4
            };

            return result;
        }
        catch (Exception ex)
        {
            return  new double[]{0,0};
        }
    }


    /**
     * verilen beacon lokasyonları ve uzaklıktan trilateration yöntemi ile pozisyon hesaplayan fonksiyon.
     * http://www.tothenew.com/blog/indoor-positioning-systemtrilateration/ adresindeki algoritmanın Java'ya uyarlanmış hali.
     * Algoritmanın asıl kaynağı ise şu adreste, https://en.wikipedia.org/wiki/Trilateration
     */
    private static double[] getMeetingPoints(double[][] positions, double[] distances) {

        double i,d,j,x,y;

        Vector2D B1 = new Vector2D(positions[0][0],positions[0][1]);
        Vector2D B2 = new Vector2D(positions[1][0],positions[1][1]);
        Vector2D B3 = new Vector2D(positions[2][0],positions[2][1]);


        Vector2D ex,ey,eResult;

        ex = (B2.subtract(B1)).normalize();
        i = (B3.subtract(B1)).dotProduct(ex);
        ey = ((B3.subtract(B1)).subtract(ex.scalarMultiply(i))).normalize();

        d = (B2.subtract(B1)).getNorm();
        j = (B3.subtract(B1)).dotProduct(ey);

        x = (Math.pow(distances[0],2) - Math.pow(distances[1],2) + Math.pow(d,2))/(2*d);
        y = ((Math.pow(distances[0],2) - Math.pow(distances[2],2) + Math.pow(i,2) + Math.pow(j,2))/(2*j)) - ((i/j)*x);

        eResult = B1.add(x,ex).add(y,ey);

        return new double[]{eResult.getX(),eResult.getY()};

    }


    /**
     * En yakındaki 3 beacon'ın verileriyle ve getMeetingPoints() fonksiyonundaki algoritmayla pozisyon hesaplaması.
     */
    public static double[] getPositionM4(double[][] positions, double[] distances ) {

        try {

            double min1 = Integer.MAX_VALUE;
            double min2 = Integer.MAX_VALUE;
            double min3 = Integer.MAX_VALUE;

            for (int i = 0; i < distances.length; i++)
            {
                if (distances[i] < min1)
                {
                    min3 = min2; min2 = min1; min1 = i;
                }
                else if (distances[i] < min2)
                {
                    min3 = min2; min2 = i;
                }
                else if (distances[i] < min3)
                {
                    min3 = i;
                }
            }

            List<double[]> nearestPositions = new ArrayList<>();
            List<Double> nearestDistances = new ArrayList<>();

            for (int i = 0; i < distances.length; i++){
                if(i == min1 || i == min2 || i == min3)
                {
                    nearestPositions.add(positions[i]);
                    nearestDistances.add(distances[i]);
                }
            }


            double[] target = new double[nearestDistances.size()];
            for (int i = 0; i < target.length; i++) {
                target[i] = nearestDistances.get(i);
            }

            double[] result = getMeetingPoints(nearestPositions.toArray(new double[3][2]),target);

            return result;
        }
        catch (Exception ex)
        {
            return  new double[]{0,0};
        }
    }


    /**
     * Kendi oluşturduğum algoritma burası.
     */
    public static double[] getPositionM5(List<Beacon> beacons ) {

        try {

            double[][] allpositions = new double[beacons.size()][3];



            Integer counter = 0;
            for(Iterator<Beacon> i = beacons.iterator(); i.hasNext(); )
            {
                Beacon item = i.next();
               // allpositions[counter] = new double[]{item.Position_X, item.Position_Y, item.getDistance()};
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



            List<double[]> truePositions = new ArrayList<>();


            for (int i = 0;i <allpositions.length;i++) {
                if(allpositions[i][2] > 0
                        && truePositions.size() < 4
                        )
                truePositions.add(allpositions[i]);

            }

            double[][] positions = new double[truePositions.size()][3];
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

            double[] calculatedPosition = new double[2];

            double powerValue = 2;

            double summer = 0;

            //Beacon'ın pozisyonu merkez, uzaklık değeri de yarıçap olacak şekilde çember çizildiğinde, çemberin orta noktaya (averegeVector) en yakın noktası ile
            // averageVector arasında karşılaştırma yapılıyor. Eğer çemberin en yakın noktası X ekseni üzerinde averageVector'un solunda kalıyorsa negatif etki, sağında kalıyorsa pozitif etki yaparak
            // summer değişkenine etki ediyor.
            // Bu yarattığı etki ise averageVector ile çembere en yakın nokta arasındaki uzaklığı X eksenine izdüşümünün uzunluğunun karesi.
            for (int i = 0;i <positionV.size();i++) {
                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getX() > averageVector.getX()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getX() < averageVector.getX())
                        ) {
                    summer += Math.pow((averageVector.distance(positionV.get(i)) - positions[i][2]) * Math.abs((averageVector.getX() - positionV.get(i).getX()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
                else
                {
                    summer -= Math.pow((averageVector.distance(positionV.get(i)) - positions[i][2]) * Math.abs((averageVector.getX() - positionV.get(i).getX()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
            }

            //Summer değeri karekökü averageVector noktasına etki ettirilerek sonuç pozisyonunun X değeri bulunuyor.
            if(summer > 0) {
                calculatedPosition[0] = averageVector.getX() + (Math.pow(Math.abs(summer), 1 / powerValue));// / positionV.size();
            }
            else
            {
                calculatedPosition[0] = averageVector.getX() - (Math.pow(Math.abs(summer), 1  /powerValue));// / positionV.size();
            }

            //Benzer hesaplama Y ekseni için de geçerli.
            summer = 0;
            for (int i = 0;i <positionV.size();i++) {
                if((averageVector.distance(positionV.get(i)) > positions[i][2] && positionV.get(i).getY() > averageVector.getY()) ||
                        (averageVector.distance(positionV.get(i)) < positions[i][2] && positionV.get(i).getY() < averageVector.getY())
                        ) {
                    summer += Math.pow((averageVector.distance(positionV.get(i)) - positions[i][2]) * Math.abs((averageVector.getY() - positionV.get(i).getY()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
                else
                {
                    summer -= Math.pow((averageVector.distance(positionV.get(i)) - positions[i][2]) * Math.abs((averageVector.getY() - positionV.get(i).getY()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition[1] = averageVector.getY()   + Math.pow(Math.abs(summer),1 / powerValue);//  / positionV.size();
            }
            else
            {
                calculatedPosition[1] = averageVector.getY() - Math.pow(Math.abs(summer), 1 / powerValue) ;// / positionV.size();
            }

            return calculatedPosition;
        }
        catch (Exception ex)
        {
            return  new double[]{0,0};
        }
    }

    public static double[] getPositionM6(double[][] positions, double[] distances ) {

        try {
            ArrayList<Vector3D> positionV = new ArrayList<>();

            double averageX = 0;
            double averageY = 0;
            double averageZ = 0;


            //Öncelikle 4 beacon'ın orta noktası hesaplanıp averageVector değişkenine atanıyor.
            for (int i = 0;i <positions.length;i++) {

                positionV.add(new Vector3D(positions[i][0],positions[i][1],positions[i][2]));
                averageX += positions[i][0];
                averageY += positions[i][1];
                averageZ += positions[i][2];
            }

            Vector3D averageVector = new Vector3D(averageX / positionV.size(),averageY / positionV.size(),averageZ / positionV.size());

            double[] calculatedPosition = new double[3];

            double powerValue = 2;

            double summer = 0;

            //Beacon'ın pozisyonu merkez, uzaklık değeri de yarıçap olacak şekilde çember çizildiğinde, çemberin orta noktaya (averegeVector) en yakın noktası ile
            // averageVector arasında karşılaştırma yapılıyor. Eğer çemberin en yakın noktası X ekseni üzerinde averageVector'un solunda kalıyorsa negatif etki, sağında kalıyorsa pozitif etki yaparak
            // summer değişkenine etki ediyor.
            // Bu yarattığı etki ise averageVector ile çembere en yakın nokta arasındaki uzaklığı X eksenine izdüşümünün uzunluğunun karesi.
            for (int i = 0;i <positionV.size();i++) {
                if((averageVector.distance(positionV.get(i)) > distances[i] && positionV.get(i).getX() > averageVector.getX()) ||
                        (averageVector.distance(positionV.get(i)) < distances[i] && positionV.get(i).getX() < averageVector.getX())
                        ) {
                    summer += Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getX() - positionV.get(i).getX()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
                else
                {
                    summer -= Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getX() - positionV.get(i).getX()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
            }

            //Summer değeri karekökü averageVector noktasına etki ettirilerek sonuç pozisyonunun X değeri bulunuyor.
            if(summer > 0) {
                calculatedPosition[0] = averageVector.getX() + (Math.pow(Math.abs(summer), 1 / powerValue));// / positionV.size();
            }
            else
            {
                calculatedPosition[0] = averageVector.getX() - (Math.pow(Math.abs(summer), 1  /powerValue));// / positionV.size();
            }

            //Benzer hesaplama Y ekseni için de geçerli.
            summer = 0;
            for (int i = 0;i <positionV.size();i++) {
                if((averageVector.distance(positionV.get(i)) > distances[i] && positionV.get(i).getY() > averageVector.getY()) ||
                        (averageVector.distance(positionV.get(i)) < distances[i] && positionV.get(i).getY() < averageVector.getY())
                        ) {
                    summer += Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getY() - positionV.get(i).getY()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
                else
                {
                    summer -= Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getY() - positionV.get(i).getY()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition[1] = averageVector.getY()   + Math.pow(Math.abs(summer),1 / powerValue);//  / positionV.size();
            }
            else
            {
                calculatedPosition[1] = averageVector.getY() - Math.pow(Math.abs(summer), 1 / powerValue) ;// / positionV.size();
            }


            summer = 0;
            for (int i = 0;i <positionV.size();i++) {
                if((averageVector.distance(positionV.get(i)) > distances[i] && positionV.get(i).getZ() > averageVector.getZ()) ||
                        (averageVector.distance(positionV.get(i)) < distances[i] && positionV.get(i).getZ() < averageVector.getZ())
                        ) {
                    summer += Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getZ() - positionV.get(i).getZ()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
                else
                {
                    summer -= Math.pow((averageVector.distance(positionV.get(i)) - distances[i]) * Math.abs((averageVector.getZ() - positionV.get(i).getZ()) / (averageVector.distance(positionV.get(i)))), powerValue);
                }
            }

            if(summer > 0) {
                calculatedPosition[2] = averageVector.getZ()   + Math.pow(Math.abs(summer),1 / powerValue);//  / positionV.size();
            }
            else
            {
                calculatedPosition[2] = averageVector.getZ() - Math.pow(Math.abs(summer), 1 / powerValue) ;// / positionV.size();
            }

            return calculatedPosition;
        }
        catch (Exception ex)
        {
            return  new double[]{0,0,0};
        }
    }



}

