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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.damien.realworldproject.appointment.Background.INSERT_DATA;

public class appointment extends AppCompatActivity {
    EditText date_time;
    EditText serviceType;

    EditText unitFloor;
    EditText buildingName;
    TextView TVLatLng;

    private int id;
    private float totalAmt;
    private double latitude, longitude;

    private TextInputLayout layoutDateTime;
    private TextInputLayout layoutServiceType;
    private TextInputLayout layoutUnitFloor;
    private TextInputLayout layoutBuildingName;
    private TextInputLayout layoutDescription;
    private TextInputLayout layoutLatLng;
    private TextView mTVTotalPayment;

    private int[] serviceIds;
    private String[] serviceString;
    private Float[] servicePrice;
    private boolean[] checkedServiceType;
    private String dateTimeOrder;
    private String finalService;
    private Float finalPrice;
    private List<Integer> list;

    public static final String EXTRA_LATITUDE = "com.example.damien.realworldproject.LATITUDE";
    public static final String EXTRA_LONGITUDE = "com.example.damien.realworldproject.LONGITUDE";
    public static final String EXTRA_SERVICE_ID = "com.example.damien.realworldproject.SERVICE_ID";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        mTVTotalPayment = findViewById(R.id.tvTotalPayment);
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
        list = new ArrayList<>();

        unitFloor = findViewById(R.id.eTUnitFloor);
        buildingName = findViewById(R.id.eTBuildingName);
        TVLatLng = findViewById(R.id.tvLatLng);
        TVLatLng.setText("Latitude  : " + latitude + "\n"
                + "Longitude: " + longitude);
        new Background(Background.FETCH_SERVICE).execute();
        date_time = findViewById(R.id.eTDateTime);
        date_time.setInputType(InputType.TYPE_NULL);
        date_time.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                date_time.clearFocus();
                if (!hasFocus){
                    return;
                }
                showDateTimeDialog(date_time);
            }
        });
        serviceType = findViewById(R.id.eTServiceType);
        serviceType.setInputType(InputType.TYPE_NULL);
        serviceType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                serviceType.clearFocus();
                if (!hasFocus){
                    return;
                }
                boolean[] temp = checkedServiceType.clone();
                List<Integer> tempList = new ArrayList<>(list);
                AlertDialog.Builder builder = new AlertDialog.Builder(appointment.this);
                builder.setTitle("Service Type");
                builder.setIcon(R.drawable.service_type_icon);

                builder.setMultiChoiceItems(serviceString, checkedServiceType, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            list.add(which);
                        }
                        else {
                            list.remove(new Integer(which));
                        }
                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collections.sort(list);
                        finalService = "";
                        finalPrice = 0f;
                        for (int i = 0; i < list.size(); i++) {
                            if (i != 0) {
                                finalService += ", ";
                            }
                            finalService += serviceString[list.get(i)];
                            finalPrice += servicePrice[list.get(i)];
                        }
                        serviceType.setText(finalService);
                        mTVTotalPayment.setText("RM " + finalPrice + "0");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedServiceType.length; i++) {
                            checkedServiceType[i] = temp[i];
                        }
                        list = new ArrayList<>(tempList);
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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
                            layoutDateTime.setError(null);
                        } else {
                            layoutDateTime.setError("Only 3 hours after current time can be select!");
                            date_time.setText("");
                        }
                    }
                };
                new TimePickerDialog(appointment.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };
        new DatePickerDialog(appointment.this, dateSetListener, calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH)).show();
    }

    public void btnConfirm_onClicked(View view) {
        if (!validateServiceType() | !validateDateTime() | !validateUnitFloor() | !validateBuildingName() | !validateLatLng()) {
            return;
        } else {
            openConfirmationDialog();
        }
    }

    private void openConfirmationDialog() {
        DecimalFormat format = new DecimalFormat("##.00");
        String formattedTotal = format.format(finalPrice);
        String msgDateTime = date_time.getText().toString();
        String address = unitFloor.getText().toString() + ", " + buildingName.getText().toString();
        String descriptionInput = layoutDescription.getEditText().getText().toString();

        if (descriptionInput.isEmpty()) {
            descriptionInput = "No description";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(appointment.this);
        builder.setTitle("Are you sure want to made Appointment ?");
        builder.setMessage("Total Payment    - RM " + formattedTotal + "\n"
                        + msgDateTime + "\n"
                        + "Address     : " + address + "\n"
                        + "Description: " + descriptionInput);

        String finalDescriptionInput = descriptionInput;
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Background bg = new Background(INSERT_DATA);
                bg.execute(String.valueOf(id), finalService, String.valueOf(latitude), String.valueOf(longitude), dateTimeOrder, address, String.valueOf(finalPrice), String.valueOf(finalDescriptionInput));
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

        public static final int FETCH_SERVICE = 30;
        public static final int INSERT_DATA = 31;

        private int method;
        private Connection conn;
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;

        public Background(int method) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.method = method;
        }
        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);

            try {
                switch (this.method) {
                    case FETCH_SERVICE:
                        result.last();
                        int totalRow = result.getRow();
                        result.first();
                        serviceIds = new int[totalRow];
                        serviceString = new String[totalRow];
                        servicePrice = new Float[totalRow];
                        checkedServiceType = new boolean[totalRow];
                        for (int i = 0; i < totalRow; i++) {
                            serviceIds[i] = result.getInt(1);
                            serviceString[i] = result.getString(2);
                            servicePrice[i] = result.getFloat(3);
                            checkedServiceType[i] = false;
                            result.next();
                        }
                        break;
                    case INSERT_DATA:
                        Intent i = new Intent();
                        i.putExtra("TOTAL_AMOUNT", totalAmt);
                        setResult(RESULT_OK, i);
                        finish();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(appointment.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            finally {
                progressDialog.hide();
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
                String query = "";
                switch (this.method) {
                    case FETCH_SERVICE:
                        query = "SELECT id, service, price FROM service_type";
                        stmt = conn.prepareStatement(query);
                        return stmt.executeQuery();
                    case INSERT_DATA:
                        query = "insert into service_request (customer_id, service_type, destination_latitude, destination_longitude, appointment_datetime, destination_address, payment_amount, status, request_datetime, service_description) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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