package com.example.damien.realworldproject;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class finalAppointmentInfo extends AppCompatActivity {

    TextView finalTVServiceID;
    TextView finalTVServiceType;
    TextView finalTVAddress;
    TextView finalTVDate;
    TextView finalTVTime;
    TextView finalTVDescription;
    TextView finalTVPrice;
    TextView finalTVStatus;

    String finalServiceID;
    String finalServiceType;
    String finalAddress;
    String finalDate;
    String finalTime;
    String finalDescription;
    String finalPrice;
    String finalStatus;

    private int customer_id;
    private float totalAmt;

    public static int REQUEST_CODE5 = 1000;
    public static int REQUEST_CODE6 = 10000;
    public static int REQUEST_CODE10 = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_appointment_info);

        customer_id = getIntent().getIntExtra(login.EXTRA_ID, 0);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);

        finalTVServiceID = findViewById(R.id.tvFinalServiceID);
        finalTVServiceType = findViewById(R.id.tvFinalServiceType);
        finalTVAddress = findViewById(R.id.tvFinalAddress);
        finalTVDate = findViewById(R.id.tvFinalDate);
        finalTVTime = findViewById(R.id.tvFinalTime);
        finalTVDescription = findViewById(R.id.tvFinalDescription);
        finalTVPrice = findViewById(R.id.tvFinalPrice);
        finalTVStatus = findViewById(R.id.tvFinalStatus);

        Intent i = getIntent();
        finalServiceID = i.getStringExtra("finalServiceID");
        finalServiceType = i.getStringExtra("finalServices");
        finalAddress = i.getStringExtra("finalAddress");
        finalDate = i.getStringExtra("finalDate");
        finalTime = i.getStringExtra("finalTime");
        finalDescription = i.getStringExtra("finalDescription");
        finalPrice = i.getStringExtra("finalPrice");
        finalStatus = i.getStringExtra("finalStatus");

        if (finalDescription == null) {
            finalDescription = "-";
        }

        finalTVServiceID.setText(finalServiceID + " ");
        finalTVServiceType.setText(finalServiceType + " ");
        finalTVAddress.setText(finalAddress + " ");
        finalTVDate.setText(finalDate + " ");
        finalTVTime.setText(finalTime + " ");
        finalTVDescription.setText(finalDescription + " ");
        finalTVPrice.setText(finalPrice + " ");
        finalTVStatus.setText(finalStatus + " ");
    }

    public void btnPaymentReview_onClicked(View view) {

        if (finalStatus.equals("completed")){
            Toast.makeText(this, "Service and Payment are done!", Toast.LENGTH_SHORT).show();
            return;
        } else if (finalStatus.equals("pending assign staff")){
            Toast.makeText(this, "Please wait for the owner to assign staff!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent i = new Intent(finalAppointmentInfo.this, paymentReview.class);
            i.putExtra("FINAL_SERVICE_ID", finalServiceID);
            i.putExtra("FINAL_SERVICE_TYPE", finalServiceType);
            i.putExtra("FINAL_ADDRESS", finalAddress);
            i.putExtra("FINAL_DATE", finalDate);
            i.putExtra("FINAL_TIME", finalTime);
            i.putExtra("FINAL_DESCRIPTION", finalDescription);
            i.putExtra("FINAL_PRICE", finalPrice);
            i.putExtra("FINAL_STATUS", finalStatus);
            i.putExtra(login.EXTRA_ID, customer_id);
            i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
            startActivityForResult(i, REQUEST_CODE5);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE5) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
}
