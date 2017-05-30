package com.example.umansia.beacon.ui.main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.v4.app.INotificationSideChannel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.Handler;
import android.util.TimingLogger;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.sql.Connection;
import java.sql.DriverManager;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.example.umansia.beacon.R;
import com.example.umansia.beacon.containers.BluetoothLeDeviceStore;
import com.example.umansia.beacon.util.BluetoothLeScanner;
import com.example.umansia.beacon.util.BluetoothUtils;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.w3c.dom.Text;


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;


import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {


    private BluetoothUtils mBluetoothUtils;
    private BluetoothLeScanner mScanner;
    private BluetoothLeDeviceStore mDeviceStore;

    private int deviceID;

    private double ratio;


    private Random rnd = new Random();
    private  boolean isScanStarted = false;

    private double FreeScreenPlaceX;
    private double FreeScreenPlaceY;

    private Button startbutton;

    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            UpdateBeaconInfo();
            timerHandler.postDelayed(timerRunnable, 500);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            runOnUiThread(() -> {
                /*Log.i(TAG,deviceLe.getAddress() + " " + deviceLe.getRssi() + " " + deviceLe.getBluetoothDeviceClassName() +  " " + deviceLe.getBluetoothDeviceMajorClassName());*/

                if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                    final IBeaconDevice iBeacon = new IBeaconDevice(deviceLe);

                    for(Iterator<Beacon> i = Positioner.BEACONS.iterator(); i.hasNext(); ) {
                        Beacon item = i.next();
                        if(item.Minor == iBeacon.getMinor())
                        {
                            item.AddSignal(iBeacon.getRssi(),iBeacon.getFirstTimestamp());
                        }
                    }
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startbutton = (Button) findViewById(R.id.startButton);
        startbutton.setOnClickListener(this);
        Button startLogButton = (Button) findViewById(R.id.stopLogButton);
        startLogButton.setOnClickListener(this);
        Button stopLogButton = (Button) findViewById(R.id.startLogButton);
        stopLogButton.setOnClickListener(this);

        deviceID = R.id.deviceImage;
        double[][] positions = new double[][] {
                { 0.0, 11.0, 0.18 },
                { 8.0, 8.5 , 6.67 },
                { 4.2, 0.0 , 2.35 },
                { 2.7, 2.1 , 1.2  },
                { 0.0, 0.0 , 0.81 },
                { 4.2, 6.8 , 1.58 },
                { 0.0, 6.8 , 3.09 },
                { 8.5, 11.0, 3.1 }
        };

        mDeviceStore = new BluetoothLeDeviceStore();
        mBluetoothUtils = new BluetoothUtils(this);
        mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
        startScanPrepare();

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void startScanPrepare() {
        //
        // The COARSE_LOCATION permission is only needed after API 23 to do a BTLE scan
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, new PermissionsResultAction() {

                        @Override
                        public void onGranted() {
                            startScan();
                        }

                        @Override
                        public void onDenied(String permission) {

                        }
                    });
        } else {
            startScan();
        }
    }

    private void startScan() {
        final boolean isBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean isBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mDeviceStore.clear();



        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (isBluetoothOn && isBluetoothLePresent) {
            mScanner.scanLeDevice(-1, true);
            invalidateOptionsMenu();
        }
    }

    private  void UpdateBeaconInfo()
    {
        Vector2D position = Positioner.GetPosition();
        //Vector3D positionTri = Positioner.GetPositionTri();
        start();

        int counter = 0;
        double signalSum = 0;

        for(Iterator<Beacon> i = Positioner.getCurrentFlootBeacons().iterator(); i.hasNext(); )
        {
            Beacon item = i.next();
           TextView v = ((TextView) ((RelativeLayout) findViewById(R.id.relativeLayout)).findViewWithTag(item.Minor));

            if(v != null) {

                if(item.Minor != 18)
                {
                    counter++;
                    signalSum += item.getCurrentSignalAverage();
                }

                ((TextView) ((RelativeLayout) findViewById(R.id.relativeLayout)).findViewWithTag(item.Minor)).setText(
                        "\nM: " + item.Minor + "\n" + "S: " + String.format("%.2f", item.getCurrentSignalAverage()) + "\nD: " + String.format("%.2f", item.getDistance()));
            }
        }



        String text=
                //String.format("%.2f",positionTri.getX()) + " - " + String.format("%.2f",positionTri.getY()) + " - " + String.format("%.2f",positionTri.getZ())+ "\n" +
                        String.format("%.2f",position.getX()) + " - " + String.format("%.2f",position.getY()) + " - " + String.format("%.2f",signalSum / counter);

        if(isScanStarted) {

            ((Button) findViewById(R.id.startButton)).setText(text);

            ImageView item = (ImageView) findViewById(deviceID);
            if(item != null) {
                deviceID = item.getId();
                item.setAdjustViewBounds(true);
                item.setTag("device");
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.width = 50;
                params.height = 50;
                params.setMargins((int) (position.getX() * ratio), (int) (FreeScreenPlaceY - position.getY() * ratio), 0, 0);
                item.setLayoutParams(params);
            }
            else
            {
                addImage((int) (position.getX() * ratio), (int) (FreeScreenPlaceY - position.getY() * ratio),R.mipmap.ic_device,deviceID,"device");
            }
        }
       /* RelativeLayout layout = (RelativeLayout)findViewById(R.id.relativeLayout);
        layout.addView(item);*/
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.startButton:
                try {
                    Positioner.BEACONS = (new DatabaseConnection().execute()).get();
                }catch (Exception ex){}
                start();
                break;

            case R.id.startLogButton:
                Positioner.LogStartDate = new Date();
                break;

            case R.id.stopLogButton:
                try {
                    Positioner.SendSignals();
                } catch (ExecutionException e) {
                   ;
                } catch (InterruptedException e) {

                }
                break;

            default:
                break;
        }

    }

    public void start()
    {
        try
        {
            List<Double> beaconXs = new ArrayList<>();
            List<Double> beaconYs = new ArrayList<>();

            for (Beacon item:
                    Positioner.getCurrentFlootBeacons() ) {
                beaconXs.add(item.Position.getX());
                beaconYs.add(item.Position.getY());
            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            FreeScreenPlaceY = 1000;
            FreeScreenPlaceX = 800;

            double beaconXPositionRange = 0;
            double beaconYPositionRange = 0;

            if(beaconXs.size() > 0) {
                beaconXPositionRange = Collections.max(beaconXs) - Collections.min(beaconXs);
            }

            if(beaconYs.size() > 0) {
                beaconYPositionRange = Collections.max(beaconYs) - Collections.min(beaconYs);
            }

            double beaconPositionRatio = 0;
            double screenPositionRatio = 0;

            if (beaconYPositionRange > 0) {
                beaconPositionRatio = beaconXPositionRange / beaconYPositionRange;
            }

            if (FreeScreenPlaceY > 0) {
                screenPositionRatio = FreeScreenPlaceX / FreeScreenPlaceY;
            }

            if (screenPositionRatio > beaconPositionRatio) {
                ratio = FreeScreenPlaceY / beaconYPositionRange;
            } else {
                ratio = FreeScreenPlaceX / beaconXPositionRange;
            }

            RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);

            layout.removeAllViewsInLayout();

            for (Beacon item:
                    Positioner.getCurrentFlootBeacons() ) {
                addImage((int) (item.Position.getX() * ratio), (int) (FreeScreenPlaceY - item.Position.getY() * ratio) + 25,R.mipmap.ic_beacon,Math.abs(rnd.nextInt()),item.Minor);
            }

            ImageView item = (ImageView) findViewById(R.id.deviceImage);
            ((ViewGroup)item.getParent()).removeView(item);
            layout.addView(item);
            isScanStarted = true;
        }
        catch (Exception ex)
        {

        }
    }

    private void addImage(int left, int top,int imageSource,int id,Object tag){


        ImageView item = new ImageView(this);
        item.setImageResource( imageSource );
        item.setAdjustViewBounds(true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
        params.width = 25;
        params.height = 25;
        params.setMargins(left,top,0,0);

        item.setLayoutParams(params);
        item.setId(id);

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.relativeLayout);


        TextView text = new TextView(this);
        text.setTag(tag);
        //text.setText(left + " - " + top);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
        //textParams.addRule(RelativeLayout.BELOW, item.getId());
        textParams.setMargins(left,top - 30,0,0);

        text.setLayoutParams(textParams);

        layout.addView(item);
        layout.addView(text,textParams);

    }
}
