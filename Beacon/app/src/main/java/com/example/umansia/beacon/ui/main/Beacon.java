package com.example.umansia.beacon.ui.main;

import android.support.annotation.NonNull;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Umansia on 15.03.2017.
 */

public class Beacon {

    private static final int TIME_TO_AVERAGE = 10000 ;
    private  static  final double A = 0.5718430992;
    private  static  final double B = 5.2320059280;
    public  static  final double C = 0.1843440066;
    public  double TX_POWER;
    private Date SignalResetTime;

    private Timer timer = new Timer();
    private  double avereageSignal;

    public String MacAdress;
    public int Major;
    public int Minor;
    public Map<Date,Integer > Signals = new ConcurrentHashMap<>();
    public Vector3D Position;
    public int floor;


    public Beacon()
    {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
//                for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
//                    Map.Entry<Date, Integer> item = it.next();
//                    if(((new Date()).getTime() - item.getKey().getTime()) > TIME_TO_AVERAGE) {
//                        it.remove();
//                    }
//                }

                int counter = 0;
                int sum = 0;

                List<Integer> signals = new ArrayList<>();

                for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Date, Integer> item = it.next();
                    int timeDifference = (int)((new Date()).getTime() - item.getKey().getTime());

                    // If time difference between signal time and now is lesser then TIME_TO_AVEREGE
                    if(timeDifference < TIME_TO_AVERAGE) {
                        signals.add((item.getValue()));
                    }
                }

                //Average of last 10 seconds calculated
                for(Iterator<Integer> it = signals.iterator(); it.hasNext(); ) {
                    double tempSignal = it.next();
                    sum += tempSignal;
                    counter++;
                }

                if(counter == 0)
                {
                    avereageSignal = 0;
                }
                else
                {
                    avereageSignal = ((double) sum) / (counter);
                }
            }
        }, 1000, 1000);
    }

    public void AddSignal(int rssi,long clockTime)
    {
//        if(Signals.size() == 0)
//        {
//            SignalResetTime = new Date();
//        }
        Signals.put(new Date(clockTime), rssi);
//        if((new Date()).getTime() - SignalResetTime.getTime() > TIME_TO_AVERAGE)
//        {
//            for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
//                Map.Entry<Date, Integer> item = it.next();
//                if(((new Date()).getTime() - item.getKey().getTime()) > TIME_TO_AVERAGE) {
//                    it.remove();
//                }
//            }
//            SignalResetTime = new Date(SignalResetTime.getTime() + TIME_TO_AVERAGE);
//        }
    }

    public double getCurrentSignalAverage()
    {
        return  avereageSignal;
    }

    public double getSignalCount()
    {
        List<Integer> signals = new ArrayList<>();

        for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Date, Integer> item = it.next();
            int timeDifference = (int)((new Date()).getTime() - item.getKey().getTime());
            if(timeDifference < TIME_TO_AVERAGE) {
                signals.add((item.getValue() * (TIME_TO_AVERAGE - timeDifference)));
            }
        }
        return signals.size();
    }

//    public double getCurrentSignalStandartDeviation()
//    {
//        List<Double> signals = new ArrayList<>();
//        for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry<Date, Integer> item = it.next();
//            if(((new Date()).getTime() - item.getKey().getTime()) < TIME_TO_AVERAGE) {
//                signals.add(Double.valueOf((item.getValue())));
//            }
//        }
//
//        int counter = 0;
//        Collections.sort(signals);
//        int signalCount = signals.size();
//
//        List<Double> fileterdSignals = new ArrayList<>();
//
//        for(Iterator<Double> it = signals.iterator(); it.hasNext(); ) {
//            if(counter > (signalCount * 0.2) || counter < (signalCount * 0.2))
//            {
//                fileterdSignals.add(it.next().doubleValue());
//
//            }
//            counter++;
//        }
//
//        double[] signalArray = new double[fileterdSignals.size()];
//        for (int i = 0; i < signalArray.length; i++) {
//            signalArray[i] = fileterdSignals.get(i);
//        }
//
//        Statistics stat = new Statistics(signalArray);
//
//        return  stat.getStdDev();
//
//
//    }
//
//    public double getCurrentSignalTimeAverage()
//    {
//
//        int sum = 0;
//
//        List<Integer> signals = new ArrayList<>();
//
//
//        long lastVisitedDate = 0;
//        for(Iterator<Map.Entry<Date, Integer>> it = Signals.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry<Date, Integer> item = it.next();
//            if(((new Date()).getTime() - item.getKey().getTime()) < TIME_TO_AVERAGE) {
//                if(lastVisitedDate > 0)
//                {
//                    signals.add((int) (item.getKey().getTime() - lastVisitedDate));
//                }
//                lastVisitedDate = item.getKey().getTime();
//            }
//        }
//        int signalCount = signals.size();
//        for(Iterator<Integer> it = signals.iterator(); it.hasNext(); ) {
//            sum += it.next();
//        }
//
//        if(signalCount == 0)
//        {
//            return  0;
//        }
//        else
//        {
//            return ((double) sum) / signalCount;
//        }
//    }


    public  double getDistance()
    {
        return  A * Math.pow(getCurrentSignalAverage() / TX_POWER,B) + C;
    }
}
