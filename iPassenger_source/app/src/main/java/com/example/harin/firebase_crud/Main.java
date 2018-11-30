package com.example.harin.firebase_crud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main extends AppCompatActivity {
    private Button btn_locate_busses;
    private TextView tv_diver_interface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btn_locate_busses=(Button)findViewById(R.id.btn_locate_busses);
        tv_diver_interface=(TextView)findViewById(R.id.tv_driver_interface);

        btn_locate_busses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Locate.class);
                startActivity(intent);
            }
        });

        tv_diver_interface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
            }
        });
    }
}
