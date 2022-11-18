package com.example.credit__book.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.credit__book.Adapter.VPAdapter;
import com.example.credit__book.R;
import com.google.android.material.tabs.TabLayout;

public class client_supplier extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_supp);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.setupWithViewPager(viewPager);

        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager());
        vpAdapter.addFragment(new client_fragment(),"client");
        vpAdapter.addFragment(new supplier_fragment(),"supplier");
        viewPager.setAdapter(vpAdapter);
    }
}