package com.bkz.menu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ExpandMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menu = findViewById(R.id.menu);
        menu.setOnClickListener(it -> menu.start());
    }

    @Override
    protected void onResume() {
        super.onResume();
        menu.showDelayed(2_000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        menu.reset();
    }
}