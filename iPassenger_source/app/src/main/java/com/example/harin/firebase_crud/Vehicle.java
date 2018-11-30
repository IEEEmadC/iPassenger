package com.example.harin.firebase_crud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Vehicle extends AppCompatActivity {

    private String mobile;
    private EditText et_registration_no;
    private EditText et_route_no;
    private Button btn_addVehicle;

    private String registration_no;
    private String route_no;

    private Intent intent;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle);

        intent=getIntent();
        mobile=intent.getStringExtra("mobile");

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("/users/"+mobile+"/vehicle");

        et_registration_no=(EditText)findViewById(R.id.et_registration_no);
        et_route_no=(EditText)findViewById(R.id.et_route_no);
        btn_addVehicle=(Button)findViewById(R.id.Button_addVehicle);

        btn_addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set values on variables
                setValues();
                if(!(registration_no.equals("")||route_no.equals(""))) {
                    //Register user's vehicle
                    registerVehicle();
                    Toast toast = Toast.makeText(getApplicationContext(), "Vehicle Registered", Toast.LENGTH_LONG);
                    toast.show();
                    //Go to login
                    Intent intent_login = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent_login);
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Please fill out all the fields", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    private void setValues() {
        //Set values on variables
        registration_no=et_registration_no.getText().toString();
        route_no=et_route_no.getText().toString();
    }

    protected void registerVehicle(){
        //Registering user's vehicle
        reference.child("registration_no").setValue(registration_no);
        reference.child("route_no").setValue(route_no);
    }
}
