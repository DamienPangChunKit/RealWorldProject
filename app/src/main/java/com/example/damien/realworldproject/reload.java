package com.example.damien.realworldproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
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
    EditText mETCardNum;
    EditText mETAmount;
    TextView mTextView;

    private int id;
    private float totalAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload);

        mETCardNum = findViewById(R.id.eTCardNumber);
        mETAmount = findViewById(R.id.eTAmount);
        mTextView = findViewById(R.id.textView);

        id = getIntent().getIntExtra(login.EXTRA_ID, -1);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);

        mETCardNum.addTextChangedListener(new TextWatcher() {
            int count = 0;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputlength = mETCardNum.getText().toString().length();

                if (count <= inputlength && inputlength == 4 || inputlength == 9 || inputlength == 14){
                    mETCardNum.setText(mETCardNum.getText().toString() + " ");

                    int temp = mETCardNum.getText().length();
                    mETCardNum.setSelection(temp);

                } else if (count >= inputlength && (inputlength == 4 || inputlength == 9 || inputlength == 14)) {
                    mETCardNum.setText(mETCardNum.getText().toString().substring(0, mETCardNum.getText().toString().length() - 1));

                    int temp = mETCardNum.getText().length();
                    mETCardNum.setSelection(temp);
                }
                count = mETCardNum.getText().toString().length();
            }
        });
    }

    public void btnReload_onClicked(View view) {
        if (!validateCardNumber() | !validateAmount()){
            return;
        } else {
            openConfirmationReload();
        }
    }

    private void openConfirmationReload() {
        String cardNum = mETCardNum.getText().toString();
        String a = mETAmount.getText().toString();
        final int amount = Integer.parseInt(a);

        AlertDialog.Builder builder = new AlertDialog.Builder(reload.this);
        builder.setTitle("Are you sure want to reload \nRM " + amount + " ?");
        builder.setMessage("Card Number  : " + cardNum + "\n"
                + "Amount            : RM " + amount + ".00");

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

    private boolean validateCardNumber() {
        String cardNum = mETCardNum.getText().toString();

        if (cardNum.isEmpty()){
            mETCardNum.setError("This field cannot be empty!");
            return false;
        } else if (cardNum.length() != 19){
            mETCardNum.setError("Please enter only 16 digit number!");
            return false;
        } else {
            mETCardNum.setError(null);
            return true;
        }
    }

    private boolean validateAmount(){
        String amt = mETAmount.getText().toString();
        int amountTOP = Integer.parseInt(amt);

        if (amt.isEmpty()){
            mETAmount.setError("This field cannot be empty!");
            return false;
        } else if (amountTOP < 10) {
            mETAmount.setError("Please reload at least RM 10!");
            return false;
        } else if (amountTOP > 1000) {
            mETAmount.setError("Only can reload a maximum RM 1000!");
            return false;
        } else {
            mETAmount.setError(null);
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
