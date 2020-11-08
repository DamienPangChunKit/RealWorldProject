package com.example.damien.realworldproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class checkAppointment extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private checkAppointmentAdapter mAdapter;

    private int customer_id;
    private float totalAmt;

    public static int REQUEST_CODE5 = 1000;
    public static int REQUEST_CODE6 = 10000;
    public static int REQUEST_CODE10 = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_appointment);

        customer_id = getIntent().getIntExtra(login.EXTRA_ID, 0);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);

        Background bg = new Background(Background.FETCH_DATA);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewCheckAppointment);
        mAdapter = new checkAppointmentAdapter(bg);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(checkAppointment.this));

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE6) {
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
        private int method;

        public static final int FETCH_DATA = 1;

        public Background(int method) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.method = method;
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;
            Log.e("Error", conn.toString());
            if (conn == null) {
                return null;
            }
            try {
                String query;

                switch(method){
                    case FETCH_DATA:
                        query = "SELECT id, appointment_datetime, status, service_type, destination_address, service_description, payment_amount FROM service_request WHERE customer_id = ?";
                        stmt = conn.prepareStatement(query);
                        stmt.setString(1, String.valueOf(customer_id));
                        result = stmt.executeQuery();
                        return result;
                }
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return null;
        }

        private Connection connectDB() {
            try {
                Class.forName(LIBRARY);
                return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DB_NAME, USERNAME, PASSWORD);
            }
            catch (Exception e) {
                Log.e("Error on Connection", e.getMessage());
                return null;
            }
        }

        public void closeConn () {
            try { stmt.close(); } catch (Exception e) { /* ignored */ }
            try { conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    private class checkAppointmentAdapter extends RecyclerView.Adapter<checkAppointmentAdapter.checkAppointmentHolder>{
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg;
        private ResultSet result;


        public checkAppointmentAdapter(Background bg){
            this.bg = bg;
            updateResultSet();
            mInflater = LayoutInflater.from(checkAppointment.this);
        }

        class checkAppointmentHolder extends RecyclerView.ViewHolder{
            TextView TVserviceID;
            TextView TVdate;
            TextView TVstatus;
            TableLayout mTableLayout;

            final checkAppointmentAdapter mAdapter;

            public checkAppointmentHolder(@NonNull View itemView, checkAppointmentAdapter adapter){
                super(itemView);
                TVserviceID = (TextView) itemView.findViewById(R.id.tvServiceID);
                TVdate = (TextView) itemView.findViewById(R.id.tvDate);
                TVstatus = (TextView) itemView.findViewById(R.id.tvStatus);
                mTableLayout = (TableLayout) itemView.findViewById(R.id.layout_table);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public checkAppointmentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View mItemView = mInflater.inflate(R.layout.check_appointment_layout, viewGroup, false);
            return new checkAppointmentHolder(mItemView, this);
        }

        @Override
        public void onBindViewHolder(@NonNull checkAppointmentHolder checkAppointmentHolder, int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final String serviceID = result.getString(1);
                final String dateTime = result.getString(2);
                String[] separate = dateTime.split(" ");
                final String date = separate[0];
                final String time = separate[1];
                final String status = result.getString(3);
                final String serviceType = result.getString(4);
                final String address = result.getString(5);
                final String description = result.getString(6);
                final String price = result.getString(7);

                checkAppointmentHolder.TVserviceID.setText(" " + serviceID);
                checkAppointmentHolder.TVdate.setText(" " + date);
                checkAppointmentHolder.TVstatus.setText(" " + status);

                checkAppointmentHolder.mTableLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(checkAppointment.this, finalAppointmentInfo.class);
                        i.putExtra("finalServiceID", serviceID);
                        i.putExtra("finalServices", serviceType);
                        i.putExtra("finalAddress", address);
                        i.putExtra("finalDate", date);
                        i.putExtra("finalTime", time.substring(0, time.length() - 5));
                        i.putExtra("finalDescription", description);
                        i.putExtra("finalPrice", price);
                        i.putExtra("finalStatus", status);
                        i.putExtra(login.EXTRA_ID, customer_id);
                        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
                        startActivityForResult(i, REQUEST_CODE6);
                    }
                });
            }
            catch (SQLException e) {
                Log.d("ERROR BIND VIEW", e.getMessage());
            }
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        private int getResultCount() {
            try {
                result.last();
                int count = result.getRow();
                result.first();
                return count;
            } catch (SQLException e) {

            }
            return 0;
        }

        public void updateResultSet() {
            try {
                bg.closeConn();
                bg = new Background(Background.FETCH_DATA);
                this.result = this.bg.execute().get();
                itemCount = getResultCount();
            } catch (ExecutionException e) {
                Log.e("ERROR EXECUTION", e.getMessage());
            } catch (InterruptedException e) {
                Log.e("ERROR INTERRUPTED", e.getMessage());
            }
        }
    }
}
