package com.example.umansia.beacon.ui.main;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Umansia on 20.04.2017.
 */

public class SendSignal extends AsyncTask<String,Void,Void> {
    @Override
    protected Void doInBackground(String... query) {

        try {
            // SET CONNECTIONSTRING
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            String username = "eofis";
            String password = "4741KJU788";
            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://195.46.148.135:1433;databaseName=Beacon;instance=SQLEXPRESS;user=" + username + ";password=" + password);

            Log.w("Connection", "open");
            Statement stmt = DbConn.createStatement();
            int result = stmt.executeUpdate(query[0]);

            DbConn.close();

        } catch (Exception e) {
            Log.w("Error connection", "" + e.getMessage());
        }

        return  null;
    }


}
