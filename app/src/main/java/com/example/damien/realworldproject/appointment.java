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
    public static final String EXTRA_SERVICE_ID = "com.example.damien.realworldproject.LONGITUDE";

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
                final String[] serviceTypeArr = new String[]{"Service Type 1", "Service Type 2"};
                checkedServiceType = new boolean[]{
                        false, // service type 1
                        false // service type 2
                };
                if (serviceType.getText().toString().equals("Service Type 1")) {
                    checkedServiceType[0] = true;
                } else if (serviceType.getText().toString().equals("Service Type 2")) {
                    checkedServiceType[1] = true;
                } else if (serviceType.getText().toString().equals("Service Type 1 and 2")) {
                    checkedServiceType[0] = true;
                    checkedServiceType[1] = true;
                }
                builder.setTitle("Service Type");
                builder.setIcon(R.drawable.service_type_icon);
                builder.setMultiChoiceItems(serviceTypeArr, checkedServiceType, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkedServiceType[0] && checkedServiceType[1]) {
                            serviceType.setText("Service Type 1 and 2");
                            mTVTotalPayment.setText("RM 123.00");
                            totalPayment = 123.00;
                            serviceOrder = "service 1, service 2";
                        } else if (checkedServiceType[1]) {
                            serviceType.setText("Service Type 2");
                            mTVTotalPayment.setText("RM 73.00");
                            totalPayment = 73.00;
                            serviceOrder = "service 2";
                        } else if (checkedServiceType[0]) {
                            serviceType.setText("Service Type 1");
                            mTVTotalPayment.setText("RM 50.00");
                            totalPayment = 50.00;
                            serviceOrder = "service 1";
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
        if (serviceType.getText().

                toString().

                equals("Service Type 1 and 2"))

        {
            builder.setMessage("Service Type 1    - RM   50.00 " + "\n"
                    + "Service Type 2    - RM   73.00 " + "\n\n"
                    + "Total Payment    - RM " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        } else

        {
            builder.setMessage(serviceType.getText().toString() + "    - RM   " + formattedTotal + "\n\n"
                    + "Total Payment    - RM   " + formattedTotal + "\n"
                    + msgDateTime + "\n"
                    + "Address     : " + address + "\n"
                    + "Description: " + descriptionInput);
        }

        String finalDescriptionInput = descriptionInput;
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                totalAmt = (float) (totalAmt - totalPayment);

                Background bg = new Background();
                bg.execute(String.valueOf(id), serviceOrder, String.valueOf(latitude), String.valueOf(longitude), dateTimeOrder, String.valueOf(totalAmt), address, String.valueOf(totalPayment), String.valueOf(finalDescriptionInput));
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
                Intent i = new Intent();

                try {
                    i.putExtra("TOTAL_AMOUNT_AFTER_PAID", totalAmt);
                    setResult(RESULT_OK, i);
                    finish();
                }
                catch (Exception e) {
                    Log.e("ERROR BACKGROUND", e.getMessage());
                    Toast.makeText(appointment.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                finally {
                    //progressDialog.hide();
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
                // progressDialog.show();
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
                    stmt.setString(6, strings[6]);
                    stmt.setFloat(7, Float.parseFloat(strings[7]));
                    stmt.setString(8, "pending assign staff");
                    stmt.setTimestamp(9, timestamp);
                    stmt.setString(10, strings[8]);

                    String query2 = "UPDATE account SET wallet_balance = ? WHERE id = ?";
                    stmt2 = conn.prepareStatement(query2);
                    stmt2.setFloat(1, Float.parseFloat(strings[5]));
                    stmt2.setInt(2, Integer.parseInt(strings[0]));
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






