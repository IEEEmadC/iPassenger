package com.example.harin.firebase_crud;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
    }

    protected void onResume(){
        super.onResume();
        Handler mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent intent=new Intent(getApplicationContext(),Main.class);
                startActivity(intent);
                finish();
            }
        };
        mHandler.sendEmptyMessageDelayed(0,1500);
    }
}
