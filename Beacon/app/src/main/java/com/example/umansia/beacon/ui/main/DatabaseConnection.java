package com.example.umansia.beacon.ui.main;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Umansia on 6.04.2017.
 */

public class DatabaseConnection extends AsyncTask<Void,Void,List<Beacon>> {

    @Override
    protected List<Beacon> doInBackground(Void... params) {
        List<Beacon> beacons = new ArrayList<>();
        try {

            // SET CONNECTIONSTRING
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            String username = "beacon";
            String password = "egplts8uqlweE";
            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://195.46.148.135:1433;databaseName=Beacon;instance=SQLEXPRESS;user=" + username + ";password=" + password);

            Log.w("Connection","open");
            Statement stmt = DbConn.createStatement();
            ResultSet rs = stmt.executeQuery(" select * from TBL_BEACON ");

            while (rs.next()) {
                int ID = rs.getInt("id");
                int major = rs.getInt("major");
                int minor = rs.getInt("minor");
                double positionX = rs.getDouble("positionX");
                double positionY = rs.getDouble("positionY");
                double positionZ = rs.getDouble("positionZ");
                double txpower = rs.getDouble("txpower");
                int floor= rs.getInt("floor");

                Beacon beacon = new Beacon();
                beacon.Major = major;
                beacon.Minor = minor;
                beacon.Position = new Vector3D(positionX,positionY,positionZ);
                beacon.TX_POWER = txpower;
                beacon.floor = floor;
                beacons.add(beacon);
            }



            DbConn.close();

        } catch (Exception e)
        {
            Log.w("Error connection","" + e.getMessage());
        }

        return  beacons;
    }
}
