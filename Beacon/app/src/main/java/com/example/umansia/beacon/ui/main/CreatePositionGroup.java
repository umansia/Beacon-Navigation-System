package com.example.umansia.beacon.ui.main;

import android.os.AsyncTask;
import android.util.Log;

import net.sourceforge.jtds.jdbc.JtdsResultSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by Umansia on 5.05.2017.
 */

public class CreatePositionGroup extends AsyncTask<Void,Void,Integer> {
    @Override
    protected Integer doInBackground(Void... params) {
        try {
            // SET CONNECTIONSTRING
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            String username = "eofis";
            String password = "4741KJU788";
            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://195.46.148.135:1433;databaseName=Beacon;instance=SQLEXPRESS;user=" + username + ";password=" + password);

            Log.w("Connection", "open");
            Statement stmt = DbConn.createStatement();
            ResultSet result = stmt.executeQuery("insert  TBL_POSITION_GROUP DEFAULT VALUES\n" +
                    "SELECT SCOPE_IDENTITY();");
            int ID = 0;
            while (result.next()) {

               ID = result.getInt(1);
            }

            DbConn.close();
            return  ID;

        } catch (Exception e) {
            Log.w("Error connection", "" + e.getMessage());
        }

        return 0;
    }
}
