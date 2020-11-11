package com.example.damien.realworldproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class reload extends AppCompatActivity {
    private TextInputLayout layoutAmount;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;

    private int id;
    private float totalAmt;
    private String password;
    TextView TVtitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload);
        TVtitle = findViewById(R.id.tvReloadTitle);
        layoutAmount = findViewById(R.id.textInputAmount);
        layoutPassword = findViewById(R.id.textInputPass);
        layoutConfirmPassword = findViewById(R.id.textInputConfirmPass);

        password = getIntent().getStringExtra(login.EXTRA_PASSWORD);
        id = getIntent().getIntExtra(login.EXTRA_ID, -1);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);
        TVtitle.setText(password);
    }

    public void btnReload_onClicked(View view) {
        if (!validateAmount() | !validatePassword() | !validateConfirmPassword()){
            return;
        } else {
            openConfirmationReload();
        }
    }

    private void openConfirmationReload() {
        String a = layoutAmount.getEditText().getText().toString();
        final int amount = Integer.parseInt(a);

        AlertDialog.Builder builder = new AlertDialog.Builder(reload.this);
        builder.setTitle("Are you sure want to reload \nRM " + amount + " ?");
        builder.setMessage("Amount : RM " + amount + ".00");

        builder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                totalAmt = totalAmt + amount;

                Background bg = new Background();
                bg.execute(String.valueOf(totalAmt), String.valueOf(id));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setIcon(R.drawable.reload_icon);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateAmount(){
        String amt = layoutAmount.getEditText().getText().toString();

        if (amt.isEmpty()){
            layoutAmount.setError("This field cannot be empty!");
            return false;
        } else {
            int amountTOP = Integer.parseInt(amt);
            if (amountTOP < 10) {
                layoutAmount.setError("Please reload at least RM 10!");
                return false;
            } else if (amountTOP > 1000) {
                layoutAmount.setError("Only can reload a maximum RM 1000!");
                return false;
            } else {
                layoutAmount.setError(null);
                return true;
            }
        }
    }

    private boolean validatePassword(){
        String passwordInput = layoutPassword.getEditText().getText().toString();

        if (passwordInput.isEmpty()) {
            layoutPassword.setError("This field cannot be empty!");
            return false;
        } else if (!passwordInput.equals(password)) {
            layoutPassword.setError("Password does not match to the old one!");
            return false;
        } else {
            layoutPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String passwordInput = layoutPassword.getEditText().getText().toString();
        String confirmPasswordInput = layoutConfirmPassword.getEditText().getText().toString();

        if (confirmPasswordInput.isEmpty()) {
            layoutConfirmPassword.setError("This field cannot be empty!");
            return false;
        } else if (!confirmPasswordInput.equals(passwordInput)) {
            layoutConfirmPassword.setError("Password does not match!");
            return false;
        } else {
            layoutConfirmPassword.setError(null);
            return true;
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
            Intent i = new Intent();

            try {
                i.putExtra("TOTAL_AMOUNT", totalAmt);
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(reload.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(reload.this);
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
                String query = "UPDATE account SET wallet_balance = ? WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setFloat(1, Float.parseFloat(strings[0]));
                stmt.setInt(2, Integer.parseInt(strings[1]));
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
