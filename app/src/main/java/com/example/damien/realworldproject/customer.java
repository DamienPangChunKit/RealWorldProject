package com.example.damien.realworldproject;

import android.content.Intent;
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

public class customer extends AppCompatActivity {
    TextView mTVMoney;

    private int id;
    private float totalAmt;
    private String phone_no;
    private String password;
    private String username;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        id = getIntent().getIntExtra(login.EXTRA_ID, -1);
        totalAmt = getIntent().getFloatExtra(login.EXTRA_WALLET_BALANCE, -1);
        phone_no = getIntent().getStringExtra(login.EXTRA_PHONE);
        password = getIntent().getStringExtra(login.EXTRA_PASSWORD);
        username = getIntent().getStringExtra(login.EXTRA_USERNAME);

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
            case R.id.history:
                aClass = history.class;
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
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    public void btnAppointment_onClicked(View view) {
        Intent i = new Intent(customer.this, appointment.class);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
        startActivity(i);
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
    }

    public void btnReload_onClicked(View view) {
        Intent i = new Intent(customer.this, reload.class);
        i.putExtra(login.EXTRA_ID, id);
        i.putExtra(login.EXTRA_WALLET_BALANCE, totalAmt);
        startActivityForResult(i, 1);
    }

}
