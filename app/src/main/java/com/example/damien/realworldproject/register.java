package com.example.damien.realworldproject;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class register extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +      // at least 1 number
                    "(?=.*[a-zA-Z])" +   // any letter
                    "(.{8,})" +        // at least 6, at most 20 character
                    "$");

    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfirmPassword;
    private TextInputLayout textInputPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textInputUsername = findViewById(R.id.textInputUsername);
        textInputPassword = findViewById(R.id.textInputPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirm);
        textInputPhone = findViewById(R.id.textInputPhone);
    }

    private boolean validateUsername() {
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputUsername.setError("This field cannot be empty!");
            return false;
        } else if (usernameInput.length() > 15) {
            textInputUsername.setError("Username was too long!");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("This field cannot be empty!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPassword.setError("Password must contain at least 8 character, letter and number!");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword(){
        String confirmPassInput = textInputConfirmPassword.getEditText().getText().toString().trim();

        if (confirmPassInput.isEmpty()) {
            textInputConfirmPassword.setError("This field cannot be empty!");
            return false;
        } else if (!confirmPassInput.equals(textInputPassword.getEditText().getText().toString().trim())) {
            textInputConfirmPassword.setError("The Password does not match!");
            return false;
        } else {
            textInputConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phoneInput = textInputPhone.getEditText().getText().toString().trim();

        if (phoneInput.isEmpty()) {
            textInputPhone.setError("This field cannot be empty!");
            return false;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            textInputPhone.setError("Please input phone number format as 0123456789");
            return false;
        } else {
            textInputPhone.setError(null);
            return true;
        }
    }

    public void btnSignUp_onClicked(View view) {
        if (!validateUsername() | !validatePassword() | !validatePhone() | !validateConfirmPassword()){
            return;
        } else {
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();
            String usernameInput = textInputUsername.getEditText().getText().toString().trim();
            String phoneInput = textInputPhone.getEditText().getText().toString().trim();
            String hashed_password = MD5(passwordInput);

            Background bg = new Background();
            bg.execute(usernameInput, hashed_password, phoneInput);

        }
    }

    public static String MD5(String password) {
        byte[] bytes = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {}
        byte[] hashed_password = md.digest(bytes);
        StringBuilder sb = new StringBuilder();

        for (byte b: hashed_password) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
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
                startActivity(new Intent(register.this, login.class));
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(register.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(register.this);
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
                String query = "insert into account (username, password, phone_no, role, wallet_balance) values (?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, strings[0]);
                stmt.setString(2, strings[1]);
                stmt.setString(3, strings[2]);
                stmt.setString(4, "customer");
                stmt.setFloat(5, 0.0f);
                stmt.executeUpdate();
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return result;
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

