
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class login extends AppCompatActivity {
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;

    public static final String EXTRA_ID = "com.example.damien.realworldproject.ID";
    public static final String EXTRA_USERNAME = "com.example.damien.realworldproject.USERNAME";
    public static final String EXTRA_WALLET_BALANCE = "com.example.damien.realworldproject.WALLET";
    public static final String EXTRA_PHONE = "com.example.damien.realworldproject.PHONE";
    public static final String EXTRA_PASSWORD = "com.example.damien.realworldproject.PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputUsername = findViewById(R.id.textInputUsername);
        textInputPassword = findViewById(R.id.textInputPassword);
    }

    private boolean validateUsername(){
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()){
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

    private boolean validatePassword(){
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()){
            textInputPassword.setError("This field cannot be empty!");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    public void btnSignIn_onClicked(View view) {
        if (!validateUsername() | !validatePassword()){
            return;
        } else {
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();
            String usernameInput = textInputUsername.getEditText().getText().toString().trim();

            Background bg = new Background();
            String hashed_password = MD5(passwordInput);
            bg.execute(usernameInput, hashed_password);
        }
    }

    public void btnSignUp_onClicked(View view) {
        startActivity(new Intent(login.this,register.class));
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
            Intent i = new Intent(login.this, customer.class);
            try {
                if (result.next()) {
                    i.putExtra(EXTRA_ID, result.getInt(1));
                    i.putExtra(EXTRA_USERNAME, result.getString(2));
                    i.putExtra(EXTRA_WALLET_BALANCE, result.getFloat(3));
                    i.putExtra(EXTRA_PHONE, result.getString(4));
                    i.putExtra(EXTRA_PASSWORD, result.getString(5));
                    startActivity(i);
                }
                else {
                    Toast.makeText(login.this, "Username and Password Invalid", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(login.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(login.this);
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
                String query = "SELECT id, username, wallet_balance, phone_no, password FROM account WHERE username LIKE ? AND password=? AND role='customer'";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, strings[0]);
                stmt.setString(2, strings[1]);
                result = stmt.executeQuery();
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
