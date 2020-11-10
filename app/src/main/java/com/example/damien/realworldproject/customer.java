package com.example.damien.realworldproject;

import android.content.Intent;
import android.widget.Button;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class customer extends AppCompatActivity {
    TextView mTVMoney;
    TextView headerUsername;

    private int id;
    private float totalAmt;
    private String phone_no;
    private String password;
    private String username;
    private String service1;
    private String service2;
    private String service3;
    private Float price1;
    private Float price2;
    private Float price3;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar toolbar;

    public static int REQUEST_CODE = 49;
    public static int REQUEST_CODE2 = 40;
    public static int REQUEST_CODE10 = 97;

    //stafflocation variables
    private double latitude;
    private double longitude;

    public static final String PENDING = "pending assign staff";
    public static final String ONGOING = "pending service";
    public static final String SERVICING = "servicing";
    public static final String COMPLETED = "completed";

    public static final String EXTRA_STATUS = "com.example.damien.realworldproject.STATUS";
    public static final String EXTRA_LATITUDE = "com.example.damien.realworldproject.LATITUDE";
    public static final String EXTRA_LONGITUDE = "com.example.damien.realworldproject.LONGITUDE";

    private String status;

    private Button btn1;
    //end of stafflocation variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        id = getIntent().getIntExtra(login.EXTRA_ID, -1);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);
        phone_no = getIntent().getStringExtra(login.EXTRA_PHONE);
        password = getIntent().getStringExtra(login.EXTRA_PASSWORD);
        username = getIntent().getStringExtra(login.EXTRA_USERNAME);
        service1 = getIntent().getStringExtra(login.EXTRA_SERVICE1);
        service2 = getIntent().getStringExtra(login.EXTRA_SERVICE2);
        service3 = getIntent().getStringExtra(login.EXTRA_SERVICE3);
        price1 = getIntent().getFloatExtra(login.EXTRA_PRICE1, -1);
        price2 = getIntent().getFloatExtra(login.EXTRA_PRICE2, -1);
        price3 = getIntent().getFloatExtra(login.EXTRA_PRICE3, -1);

        mTVMoney = findViewById(R.id.tvMoney);
        mTVMoney.setText("RM " + totalAmt + "0");

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView nvDrawer = (NavigationView)findViewById(R.id.nv);
        //call setupDrawerContent
        setupDrawerContent(nvDrawer);

        View headerView = nvDrawer.getHeaderView(0);
        headerUsername = headerView.findViewById(R.id.tvAccountName);
        headerUsername.setText(username);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }
    public void selectDrawerItem(MenuItem menuItem) {
        Class aClass;
        switch(menuItem.getItemId()) {
            case R.id.profile:
                aClass = profile.class;
                break;
            case R.id.password:
                aClass = changePassword.class;
                break;
            case R.id.logout:
                aClass = login.class;
                break;
            default:
                aClass = profile.class;
        }
        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
        Intent i = new Intent(this,aClass);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_USERNAME, username);
        i.putExtra(login.EXTRA_PASSWORD, password);
        i.putExtra(login.EXTRA_PHONE, phone_no);
        if(aClass == profile.class){
            startActivityForResult(i,REQUEST_CODE);
        }
        else{
            startActivity(i);
        }

    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                totalAmt = data.getFloatExtra("TOTAL_AMOUNT", 0);
                mTVMoney.setText("RM " + totalAmt + "0");
            }
        }
        else if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK){
                username = data.getStringExtra("USERNAME_EDIT");
                phone_no = data.getStringExtra("PHONE_EDIT");
            }
        }
        else if(requestCode == REQUEST_CODE2){
            if(resultCode == RESULT_OK){
                totalAmt = data.getFloatExtra("TOTAL_AMOUNT", 0);
                mTVMoney.setText("RM " + totalAmt + "0");
            }
        }
        else if(requestCode == REQUEST_CODE10){
            if(resultCode == RESULT_OK){
                totalAmt = data.getFloatExtra("TOTAL_AMOUNT_AFTER_PAID", 0);
                mTVMoney.setText("RM " + totalAmt + "0");
                Toast.makeText(this, "Payment Successfully! Thank you for using Ebox Salon!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void btnReload_onClicked(View view) {
        Intent i = new Intent(customer.this, reload.class);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
        i.putExtra(login.EXTRA_PASSWORD, password);
        startActivityForResult(i, 1);
    }

    public void btnAppointment_onClicked(View view) {
        Intent i = new Intent(customer.this, appointment.class);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
        i.putExtra(login.EXTRA_SERVICE1, service1);
        i.putExtra(login.EXTRA_SERVICE2, service2);
        i.putExtra(login.EXTRA_SERVICE3, service3);
        i.putExtra(login.EXTRA_PRICE1, price1);
        i.putExtra(login.EXTRA_PRICE2, price2);
        i.putExtra(login.EXTRA_PRICE3, price3);
        startActivityForResult(i, REQUEST_CODE2);
    }

    public void btnCheckAppointment_onClicked(View view) {
        Intent i = new Intent(customer.this, checkAppointment.class);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
        startActivityForResult(i, REQUEST_CODE10);
    }
}