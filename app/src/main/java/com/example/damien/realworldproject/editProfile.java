package com.example.damien.realworldproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class editProfile extends AppCompatActivity {
    private EditText mETusername;
    private EditText mETphone;
    private TextInputLayout mLayoutUsername;
    private TextInputLayout mLayoutPhone;

    private int id;
    private String username;
    private String phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mETusername = findViewById(R.id.ETusername);
        mETphone = findViewById(R.id.ETphone);
        mLayoutUsername = findViewById(R.id.layout_username);
        mLayoutPhone = findViewById(R.id.layout_phone_no);

        Intent i = getIntent();
        id = i.getIntExtra(login.EXTRA_ID, -1);
        username = i.getStringExtra(profile.USERNAME);
        phoneNo = i.getStringExtra(profile.PHONE);

        mETusername.setText(username);
        mETphone.setText(phoneNo);
    }

    private boolean validateUsername(){
        String usernameInput = mLayoutUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()){
            mLayoutUsername.setError("This field cannot be empty!");
            return false;
        } else if (usernameInput.length() > 15) {
            mLayoutUsername.setError("Username was too long!");
            return false;
        } else {
            mLayoutUsername.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phoneInput = mLayoutPhone.getEditText().getText().toString().trim();

        if (phoneInput.isEmpty()) {
            mLayoutPhone.setError("This field cannot be empty!");
            return false;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            mLayoutPhone.setError("Please input phone number format as 0123456789");
            return false;
        } else {
            mLayoutPhone.setError(null);
            return true;
        }
    }

    public void btnSave_onClick(View view) {
        if (!validateUsername() | !validatePhone()){
            return;
        } else {
            String usernameInput = mLayoutUsername.getEditText().getText().toString().trim();
            String phoneInput = mLayoutPhone.getEditText().getText().toString().trim();

            new Background().execute(usernameInput, phoneInput);
        }
    }

    public class Background extends AsyncTask<String, Void, String> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12372307";
        private static final String DB_NAME = "sql12372307";
        private static final String PASSWORD = "LYyljvuyn8";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;
        private String usernameEdit;
        private String phoneEdit;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            closeConn();

            try {
                if (result.isEmpty()) {
                    Intent i = new Intent();
                    i.putExtra("USERNAME_EDIT", usernameEdit);
                    i.putExtra("PHONE_EDIT", phoneEdit);
                    setResult(RESULT_OK, i);
                    finish();
                }
                else {
                    Toast.makeText(editProfile.this, result, Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(editProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editProfile.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            conn = connectDB();
            usernameEdit = strings[0];
            phoneEdit = strings[1];

            if (conn == null) {
                return null;
            }
            try {
                String query = "SELECT username, phone_no FROM account WHERE (username LIKE ? OR phone_no = ?) AND id <> ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, usernameEdit);
                stmt.setString(2, phoneEdit);
                stmt.setInt(3, id);
                ResultSet result1 = stmt.executeQuery();

                if (result1.next()) {
                    String uname = result1.getString(1);
                    String phNo = result1.getString(2);
                    if (result1.next() || uname.toLowerCase().equals(usernameEdit.toLowerCase()) && phNo.equals(phoneEdit))
                        return getString(R.string.name_and_phone_exists);
                    if (phNo.equals(phoneEdit))
                        return getString(R.string.phone_exists);
                    return getString(R.string.name_exists);
                } else {
                    stmt = conn.prepareStatement("UPDATE account SET username=?, phone_no=? WHERE id=?");
                    stmt.setString(1, usernameEdit);
                    stmt.setString(2, phoneEdit);
                    stmt.setInt(3, id);

                    stmt.executeUpdate();
                }
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return "";
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
}
