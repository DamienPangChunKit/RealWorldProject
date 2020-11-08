package com.example.damien.realworldproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class paymentReview extends AppCompatActivity {
    TextView finalTVServiceID;
    TextView finalTVServiceType;
    TextView finalTVAddress;
    TextView finalTVDate;
    TextView finalTVTime;
    TextView finalTVTimeEnded;
    TextView finalTVDescription;
    TextView finalTVPrice;
    TextView finalTVStatus;
    RatingBar ratingBar;
    EditText finalReview;

    String finalServiceID;
    String finalServiceType;
    String finalAddress;
    String finalDate;
    String finalTime;
    String finalTimeEnded;
    String finalDescription;
    String finalPrice;
    String finalStatus;
    String review;
    String finalTimeEnded2;

    int rating;
    int customer_id;
    float totalAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_review);

        finalReview = findViewById(R.id.eTReview);
        finalTVServiceID = findViewById(R.id.tvFinalServiceID);
        finalTVServiceType = findViewById(R.id.tvFinalServiceType);
        finalTVAddress = findViewById(R.id.tvFinalAddress);
        finalTVDate = findViewById(R.id.tvFinalDate);
        finalTVTime = findViewById(R.id.tvFinalTime);
        finalTVTimeEnded = findViewById(R.id.tvFinalTimeEnded);
        finalTVDescription = findViewById(R.id.tvFinalDescription);
        finalTVPrice = findViewById(R.id.tvFinalPrice);
        finalTVStatus = findViewById(R.id.tvFinalStatus);

        Intent i = getIntent();
        customer_id = i.getIntExtra(login.EXTRA_ID, 0);
        totalAmt = i.getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);

        finalServiceID = i.getStringExtra("FINAL_SERVICE_ID");
        finalServiceType = i.getStringExtra("FINAL_SERVICE_TYPE");
        finalAddress = i.getStringExtra("FINAL_ADDRESS");
        finalDate = i.getStringExtra("FINAL_DATE");
        finalTime = i.getStringExtra("FINAL_TIME");
        finalDescription = i.getStringExtra("FINAL_DESCRIPTION");
        finalPrice = i.getStringExtra("FINAL_PRICE");
        finalStatus = i.getStringExtra("FINAL_STATUS");

        Date currentTime = Calendar.getInstance().getTime();
        finalTimeEnded = new SimpleDateFormat("HH:mm").format(currentTime);
        finalTimeEnded2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);

        finalTVServiceID.setText(finalServiceID + " ");
        finalTVServiceType.setText(finalServiceType + " ");
        finalTVAddress.setText(finalAddress + " ");
        finalTVDate.setText(finalDate + " ");
        finalTVTime.setText(finalTime + " ");
        finalTVTimeEnded.setText(finalTimeEnded + " ");
        finalTVDescription.setText(finalDescription + " ");
        finalTVPrice.setText(finalPrice + " ");
        finalTVStatus.setText(finalStatus + " ");

    }

    private boolean validateTotalAmount(){
        if (totalAmt < Float.parseFloat(finalPrice)){
            Toast.makeText(this, "Insufficient credit. Please reload money before making payment!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public void btnSubmitPaymenReview_onClicked(View view) {
        if (!validateTotalAmount()){
            return;
        } else {
            if (review == ""){
                review = "-";
            }
            ratingBar = (RatingBar)findViewById(R.id.rating_rating_bar);
            rating = (int) ratingBar.getRating();
            review = finalReview.getText().toString();
            totalAmt = totalAmt - Float.parseFloat(finalPrice);

            Background bg = new Background();
            bg.execute(String.valueOf(totalAmt), String.valueOf(finalServiceID), finalTimeEnded2, String.valueOf(rating), review, String.valueOf(customer_id));
        }
    }

    public class Background extends AsyncTask<String, Void, ResultSet> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12372307";
        private static final String DB_NAME = "sql12372307";
        private static final String PASSWORD = "LYyljvuyn8";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt, stmt2;
        private ProgressDialog progressDialog;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);

            try {
                Intent i = new Intent();
                i.putExtra("TOTAL_AMOUNT_AFTER_PAID", totalAmt);
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(paymentReview.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            finally {
                try { result.close(); } catch (Exception e) { /* ignored */ }
                closeConn();
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(paymentReview.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            if (conn == null) {
                return null;
            }
            try {
                String query = "UPDATE account SET wallet_balance = ? WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setFloat(1, Float.parseFloat(strings[0]));
                stmt.setInt(2, Integer.parseInt(strings[5]));

                String query2 = "UPDATE service_request SET endservice_datetime = ?, rating = ?, review = ?, status = ? WHERE id = ?";
                stmt2 = conn.prepareStatement(query2);
                stmt2.setString(1, strings[2]);
                stmt2.setInt(2, Integer.parseInt(strings[3]));
                stmt2.setString(3, strings[4]);
                stmt2.setString(4, "completed");
                stmt2.setInt(5, Integer.parseInt(strings[1]));

                stmt.executeUpdate();
                stmt2.executeUpdate();
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
                stmt2.close();
            } catch (Exception e) {
                /* ignored */
            }
            try {
                conn.close();
            } catch (Exception e) { /* ignored */ }
        }
    }
}
