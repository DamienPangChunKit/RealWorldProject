package com.example.damien.realworldproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class appointment extends AppCompatActivity {
    EditText date_time;
    EditText serviceType;

    EditText unitFloor;
    EditText buildingName;
    TextView TVLatLng;

    private int id;
    private float totalAmt;
    private double latitude, longitude;
    private double totalPayment;
    private String service1;
    private String service2;
    private String service3;
    private Float price1;
    private Float price2;
    private Float price3;

    private TextInputLayout layoutDateTime;
    private TextInputLayout layoutServiceType;
    private TextInputLayout layoutUnitFloor;
    private TextInputLayout layoutBuildingName;
    private TextInputLayout layoutDescription;
    private TextInputLayout layoutLatLng;
    private TextView mTVTotalPayment;
    private String serviceOrder;
    private String dateTimeOrder;

    private boolean[] checkedServiceType;

    public static final String EXTRA_LATITUDE = "com.example.damien.realworldproject.LATITUDE";
    public static final String EXTRA_LONGITUDE = "com.example.damien.realworldproject.LONGITUDE";
    public static final String EXTRA_SERVICE_ID = "com.example.damien.realworldproject.SERVICE_ID";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        layoutDateTime = findViewById(R.id.textInputDateTime);
        layoutServiceType = findViewById(R.id.textInputServiceType);
        layoutUnitFloor = findViewById(R.id.textInputUnitFloor);
        layoutBuildingName = findViewById(R.id.textInputBuildingName);
        layoutDescription = findViewById(R.id.textInputDescription);
        layoutLatLng = findViewById(R.id.textInputLatLng);

        id = getIntent().getIntExtra(login.EXTRA_ID, -1);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);
        latitude = getIntent().getDoubleExtra(map.EXTRA_LATITUDE, 0);
        longitude = getIntent().getDoubleExtra(map.EXTRA_LONGITUDE, 0);
        service1 = getIntent().getStringExtra(login.EXTRA_SERVICE1);
        service2 = getIntent().getStringExtra(login.EXTRA_SERVICE2);
        service3 = getIntent().getStringExtra(login.EXTRA_SERVICE3);
        price1 = getIntent().getFloatExtra(login.EXTRA_PRICE1, -1);
        price2 = getIntent().getFloatExtra(login.EXTRA_PRICE2, -1);
        price3 = getIntent().getFloatExtra(login.EXTRA_PRICE3, -1);

        unitFloor = findViewById(R.id.eTUnitFloor);
        buildingName = findViewById(R.id.eTBuildingName);
        TVLatLng = findViewById(R.id.tvLatLng);
        TVLatLng.setText("Latitude  : " + latitude + "\n"
                + "Longitude: " + longitude);

        date_time = findViewById(R.id.eTDateTime);
        date_time.setInputType(InputType.TYPE_NULL);
        date_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimeDialog(date_time);
            }
        });
        serviceType = findViewById(R.id.eTServiceType);
        serviceType.setInputType(InputType.TYPE_NULL);
        serviceType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appointment.this);
                final String[] serviceTypeArr = new String[]{service1, service2, service3};
                checkedServiceType = new boolean[]{
                        false, // service type 1
                        false, // service type 2
                        false  // service type 3
                };

                if (serviceType.getText().toString().equals(service1)) {
                    checkedServiceType[0] = true;
                } else if (serviceType.getText().toString().equals(service2)) {
                    checkedServiceType[1] = true;
                } else if (serviceType.getText().toString().equals(service3)) {
                    checkedServiceType[2] = true;
                } else if (serviceType.getText().toString().equals(service1 + ", " + service2)) {
                    checkedServiceType[0] = true;
                    checkedServiceType[1] = true;
                } else if (serviceType.getText().toString().equals(service2 + ", " + service3)) {
                    checkedServiceType[1] = true;
                    checkedServiceType[2] = true;
                } else if (serviceType.getText().toString().equals(service1 + ", " + service3)) {
                    checkedServiceType[0] = true;
                    checkedServiceType[2] = true;
                } else if (serviceType.getText().toString().equals(service1 + ", " + service2 + ", " + service3)) {
                    checkedServiceType[0] = true;
                    checkedServiceType[1] = true;
                    checkedServiceType[2] = true;
                }

                builder.setTitle("Service Type");
                builder.setIcon(R.drawable.service_type_icon);
                builder.setMultiChoiceItems(serviceTypeArr, checkedServiceType, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    Float servicePrice123 = price1 + price2 + price3;
                    Float servicePrice12 = price1 + price2;
                    Float servicePrice23 = price2 + price3;
                    Float servicePrice13 = price1 + price3;
                    Float servicePrice1 = price1;
                    Float servicePrice2 = price2;
                    Float servicePrice3 = price3;

                    String displayService123 = service1 + ", " + service2 + ", " + service3;
                    String displayService12 = service1 + ", " + service2;
                    String displayService23 = service2 + ", " + service3;
                    String displayService13 = service1 + ", " + service3;
                    String displayService1 = service1;
                    String displayService2 = service2;
                    String displayService3 = service3;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkedServiceType[0] && checkedServiceType[1] && checkedServiceType[2]) {
                            serviceType.setText(displayService123);
                            mTVTotalPayment.setText("RM " + servicePrice123 + "0");
                            totalPayment = servicePrice123;
                            serviceOrder = displayService123;
                        } else if (checkedServiceType[0] && checkedServiceType[2]) {
                            serviceType.setText(displayService13);
                            mTVTotalPayment.setText("RM " + servicePrice13 + "0");
                            totalPayment = servicePrice13;
                            serviceOrder = displayService13;
                        } else if (checkedServiceType[1] && checkedServiceType[2]) {
                            serviceType.setText(displayService23);
                            mTVTotalPayment.setText("RM " + servicePrice23 + "0");
                            totalPayment = servicePrice23;
                            serviceOrder = displayService23;
                        } else if (checkedServiceType[0] && checkedServiceType[1]) {
                            serviceType.setText(displayService12);
                            mTVTotalPayment.setText("RM " + servicePrice12 + "0");
                            totalPayment = servicePrice12;
                            serviceOrder = displayService12;
                        } else if (checkedServiceType[2]) {
                            serviceType.setText(displayService3);
                            mTVTotalPayment.setText("RM " + servicePrice3 + "0");
                            totalPayment = servicePrice3;
                            serviceOrder = displayService3;
                        } else if (checkedServiceType[1]) {
                            serviceType.setText(displayService2);
                            mTVTotalPayment.setText("RM " + servicePrice2 + "0");
                            totalPayment = servicePrice2;
                            serviceOrder = displayService2;
                        } else if (checkedServiceType[0]) {
                            serviceType.setText(displayService1);
                            mTVTotalPayment.setText("RM " + servicePrice1 + "0");
                            totalPayment = servicePrice1;
                            serviceOrder = displayService1;
                        } else {
                            serviceType.setText("");
                            mTVTotalPayment.setText("RM 0.00");
                            totalPayment = 0.00;
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        mTVTotalPayment = findViewById(R.id.tvTotalPayment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                latitude = data.getDoubleExtra(map.EXTRA_LATITUDE, 0);
                longitude = data.getDoubleExtra(map.EXTRA_LONGITUDE, 0);

                TVLatLng.setText("Latitude  : " + latitude + "\n"
                        + "Longitude: " + longitude);

            }
        }
    }

    private boolean validateServiceType() {
        String serviceTypeInput = serviceType.getText().toString();

        if (serviceTypeInput.isEmpty()) {
            layoutServiceType.setError("This field cannot be empty!");
            return false;
        } else {
            layoutServiceType.setError(null);
            return true;
        }
    }

    private boolean validateDateTime() {
        String dateTimeInput = date_time.getText().toString();

        if (dateTimeInput.isEmpty()) {
            date_time.setError("This field cannot be empty!");
            layoutDateTime.setError("This field cannot be empty!");
            return false;
        } else {

            layoutDateTime.setError(null);
            return true;
        }
    }

    private boolean validateUnitFloor() {
        String unitFloorInput = unitFloor.getText().toString();

        if (unitFloorInput.isEmpty()) {
            layoutUnitFloor.setError("This field cannot be empty!");
            return false;
        } else {
            layoutUnitFloor.setError(null);
            return true;
        }
    }

    private boolean validateBuildingName() {
        String buidingNameInput = buildingName.getText().toString();

        if (buidingNameInput.isEmpty()) {
            layoutBuildingName.setError("This field cannot be empty!");
            return false;
        } else {
            layoutBuildingName.setError(null);
            return true;
        }
    }

    private boolean validateLatLng() {
        if (latitude == 0 | longitude == 0) {
            layoutLatLng.setError("Please choose location before making appointment!");
            return false;
        } else {
            layoutLatLng.setError(null);
            return true;
        }
    }

    private void showDateTimeDialog(final EditText date_time) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");

                        Calendar checkC = Calendar.getInstance();
                        if (calendar.getTimeInMillis() - 10800000 > checkC.getTimeInMillis()) {
                            String date = simpleDateFormat.format(calendar.getTime());
                            String time = simpleTimeFormat.format(calendar.getTime());
                            date_time.setText("Date: " + date + "   Time: " + time);
                            dateTimeOrder = date + " " + time;
                            date_time.setError(null);
                        } else {
                            date_time.setError("Only 3 hours after the current time can be selected!");
                            date_time.setText("");
                        }
                    }
                };
                new TimePickerDialog(appointment.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };
        new DatePickerDialog(appointment.this, dateSetListener, calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH)).show();
    }

    public void hotspotTracker_onClicked(View view) {
        // to be implement
    }

    public void btnConfirm_onClicked(View view) {
        if (!validateServiceType() | !validateDateTime() | !validateUnitFloor() | !validateBuildingName() | !validateLatLng()) {
            return;
        } else {
            openConfirmationDialog();
        }
    }

    // Havent add location msg or maybe no need add
    private void openConfirmationDialog() {
        String displayService123 = service1 + ", " + service2 + ", " + service3;
        String displayService12 = service1 + ", " + service2;
        String displayService23 = service2 + ", " + service3;
        String displayService13 = service1 + ", " + service3;

        DecimalFormat format = new DecimalFormat("##.00");
        String formattedTotal = format.format(totalPayment);
        String msgDateTime = date_time.getText().toString();
        String address = unitFloor.getText().toString() + ", " + buildingName.getText().toString();
        String descriptionInput = layoutDescription.getEditText().getText().toString();

        if (descriptionInput.isEmpty())
        {
            descriptionInput = "No description";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(appointment.this);
        builder.setTitle("Are you sure want to made Appointment ?");
        if (serviceType.getText().toString().equals(displayService123)) {
            builder.setMessage("Total Payment    - RM " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        } else if (serviceType.getText().toString().equals(displayService12)){
            builder.setMessage("Total Payment    - RM " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        } else if (serviceType.getText().toString().equals(displayService23)){
            builder.setMessage("Total Payment    - RM " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        } else if (serviceType.getText().toString().equals(displayService13)){
            builder.setMessage("Total Payment    - RM " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        } else {
            builder.setMessage("Total Payment    - RM   " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        }

        String finalDescriptionInput = descriptionInput;
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Background bg = new Background();
                bg.execute(String.valueOf(id), serviceOrder, String.valueOf(latitude), String.valueOf(longitude), dateTimeOrder, address, String.valueOf(totalPayment), String.valueOf(finalDescriptionInput));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.confirmation_icon);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void btnGetLocation_onClicked(View view) {
        Intent i = new Intent(appointment.this, map.class);
        startActivityForResult(i, 5);
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
                i.putExtra("TOTAL_AMOUNT", totalAmt);
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(appointment.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            finally {
                try { result.close(); } catch (Exception e) { /* ignored */ }
                closeConn();
            }
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(appointment.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

            if (conn == null) {
                return null;
            }
            try {
                String query = "insert into service_request (customer_id, service_type, destination_latitude, destination_longitude, appointment_datetime, destination_address, payment_amount, status, request_datetime, service_description) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(strings[0]));
                stmt.setString(2, strings[1]);
                stmt.setDouble(3, Double.parseDouble(strings[2]));
                stmt.setDouble(4, Double.parseDouble(strings[3]));
                stmt.setString(5, strings[4]);
                stmt.setString(6, strings[5]);
                stmt.setFloat(7, Float.parseFloat(strings[6]));
                stmt.setString(8, "pending assign staff");
                stmt.setTimestamp(9, timestamp);
                stmt.setString(10, strings[7]);

                stmt.executeUpdate();
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