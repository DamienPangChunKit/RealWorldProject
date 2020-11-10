package com.example.damien.realworldproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class finalAppointmentInfo extends AppCompatActivity {

    TextView finalTVServiceID;
    TextView finalTVServiceType;
    TextView finalTVAddress;
    TextView finalTVDate;
    TextView finalTVTime;
    TextView finalTVDescription;
    TextView finalTVPrice;
    TextView finalTVStatus;

    String finalServiceID;
    String finalServiceType;
    String finalAddress;
    String finalDate;
    String finalTime;
    String finalDescription;
    String finalPrice;
    String finalStatus;

    Double finalLatitude;
    Double finalLongitude;

    Button btn1;
    Button btn2;

    private int customer_id;
    private float totalAmt;

    public static int REQUEST_CODE5 = 1000;

    public static final String PENDING = "pending assign staff";
    public static final String ONGOING = "pending service";
    public static final String SERVICING = "servicing";
    public static final String COMPLETED = "completed";

    public static final String EXTRA_SERVICE_ID = "com.example.damien.realworldproject.SERVICE_ID";
    public static final String EXTRA_LATITUDE = "com.example.damien.realworldproject.LATITUDE";
    public static final String EXTRA_LONGITUDE = "com.example.damien.realworldproject.LONGITUDE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_appointment_info);

        customer_id = getIntent().getIntExtra(login.EXTRA_ID, 0);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);

        finalTVServiceID = findViewById(R.id.tvFinalServiceID);
        finalTVServiceType = findViewById(R.id.tvFinalServiceType);
        finalTVAddress = findViewById(R.id.tvFinalAddress);
        finalTVDate = findViewById(R.id.tvFinalDate);
        finalTVTime = findViewById(R.id.tvFinalTime);
        finalTVDescription = findViewById(R.id.tvFinalDescription);
        finalTVPrice = findViewById(R.id.tvFinalPrice);
        finalTVStatus = findViewById(R.id.tvFinalStatus);

        Intent i = getIntent();
        finalServiceID = i.getStringExtra(EXTRA_SERVICE_ID);
        finalServiceType = i.getStringExtra("finalServices");
        finalAddress = i.getStringExtra("finalAddress");
        finalDate = i.getStringExtra("finalDate");
        finalTime = i.getStringExtra("finalTime");
        finalDescription = i.getStringExtra("finalDescription");
        finalPrice = i.getStringExtra("finalPrice");
        finalStatus = i.getStringExtra("finalStatus");
        finalLatitude = i.getDoubleExtra(EXTRA_LATITUDE,7);
        finalLongitude = i.getDoubleExtra(EXTRA_LONGITUDE,8);

        if (finalDescription == null) {
            finalDescription = "-";
        }

        finalTVServiceID.setText(finalServiceID + " ");
        finalTVServiceType.setText(finalServiceType + " ");
        finalTVAddress.setText(finalAddress + " ");
        finalTVDate.setText(finalDate + " ");
        finalTVTime.setText(finalTime + " ");
        finalTVDescription.setText(finalDescription + " ");
        finalTVPrice.setText(finalPrice + " ");
        finalTVStatus.setText(finalStatus + " ");

        if (finalStatus.equals(SERVICING) || finalStatus.equals(COMPLETED)){
            ((TextView)findViewById(R.id.textView2)).setVisibility(View.INVISIBLE);
        }
        updateView();
    }

    public void updateView() {

        btn1 = (Button) findViewById(R.id.btnCancelAppointment);
        btn2 = (Button) findViewById(R.id.btnPayment);

        if(finalStatus.equals(PENDING) || finalStatus.equals(ONGOING)) {
            btn1.setVisibility(View.INVISIBLE);
            btn2.setText("Cancel Appointment");
            btn2.setVisibility(View.VISIBLE);
//            btn2.setText("Assign Staff");
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Background().execute();

                }
            });
        }else if(finalStatus.equals(SERVICING)){
            btn1.setVisibility(View.VISIBLE);
            btn1.setText("Check Staff Location");
            btn2.setVisibility(View.VISIBLE);
            btn2.setText("Proceed to payment and submit");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(finalAppointmentInfo.this, staffLocation.class);
                    i.putExtra(EXTRA_SERVICE_ID, finalServiceID);
                    i.putExtra(EXTRA_LATITUDE, finalLatitude);
                    i.putExtra(EXTRA_LONGITUDE, finalLongitude);
                    startActivity(i);
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(finalAppointmentInfo.this, paymentReview.class);
                    i.putExtra("FINAL_SERVICE_ID", finalServiceID);
                    i.putExtra("FINAL_SERVICE_TYPE", finalServiceType);
                    i.putExtra("FINAL_ADDRESS", finalAddress);
                    i.putExtra("FINAL_DATE", finalDate);
                    i.putExtra("FINAL_TIME", finalTime);
                    i.putExtra("FINAL_DESCRIPTION", finalDescription);
                    i.putExtra("FINAL_PRICE", finalPrice);
                    i.putExtra("FINAL_STATUS", finalStatus);
                    i.putExtra(login.EXTRA_ID, customer_id);
                    i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
                    startActivityForResult(i, REQUEST_CODE5);
                }
            });

        }else{
            btn1.setVisibility(View.INVISIBLE);
            btn2.setVisibility(View.INVISIBLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE5) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    public class Background extends AsyncTask<String, Void, ResultSet> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12372307";
        private static final String DB_NAME = "sql12372307";
        private static final String PASSWORD = "LYyljvuyn8";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);


            try {
                setResult(RESULT_CANCELED);
                finish();
            }
            finally {
                progressDialog.hide();
                closeConn();
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(finalAppointmentInfo.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            if (conn == null) {
                return null;
            }
            try {
                String query = "SELECT appointment_datetime FROM service_request WHERE id=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, finalServiceID);

                result = stmt.executeQuery();
                result.next();

                Timestamp t = result.getTimestamp(1);
                result.close();
                Date d = new Date(t.getTime());
                Calendar cal = Calendar.getInstance();

                long timeNow = cal.getTimeInMillis();
                cal.setTime(d);
                cal.add(Calendar.HOUR, -2);
                long appointmentDatetime = cal.getTimeInMillis();

                if (timeNow < appointmentDatetime) {
                    query = "DELETE FROM service_request WHERE id=?;";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, finalServiceID);
                    stmt.executeUpdate();

                }
                return result;
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return result;
        }


        private Connection connectDB(){
            try {
                Class.forName(LIBRARY);
                return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DB_NAME, USERNAME, PASSWORD);
            } catch (Exception e) {
                Log.e("Error on Connection", e.getMessage());
                return null;
            }
        }

        public void closeConn() {
            try {
                stmt.close();
            } catch (Exception e) {
                /* ignored */
            }
            try {
                conn.close();
            } catch (Exception e) { /* ignored */ }
        }
    }
}