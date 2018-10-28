package com.example.harin.firebase_crud;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {
    private EditText et_name;
    private EditText et_nic;
    private EditText et_email;
    private EditText et_mobile;
    private EditText et_password;
    private Button btn_register;
    private CheckBox checkBox_termsAndConditions;

    private String name;
    private String nic;
    private String email;
    private String mobile;
    private String password;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        et_name=(EditText)findViewById(R.id.et_name);
        et_nic=(EditText)findViewById(R.id.et_nic);
        et_email=(EditText)findViewById(R.id.et_email);
        et_mobile=(EditText)findViewById(R.id.et_mobile);
        et_password=(EditText)findViewById(R.id.et_password);
        btn_register=(Button) findViewById(R.id.Button_register);
        checkBox_termsAndConditions=(CheckBox) findViewById(R.id.checkBox_termsAndConditions);

        /* Text fields are empty at onCreate */
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set values
                setValues();
                //Register user
                database=FirebaseDatabase.getInstance();
                reference=database.getReference("/users");

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(mobile)) {
                            if (checkBox_termsAndConditions.isChecked()) {
                                if (!(name.equals("") || nic.equals("") || email.equals("") || mobile.equals("") || password.equals(""))) {
                                    registerUser();
                                    Toast toast = Toast.makeText(getApplicationContext(), "Your account is created", Toast.LENGTH_LONG);
                                    toast.show();
                                    //Go to register vehicle
                                    Intent intent = new Intent(getApplicationContext(), Vehicle.class);
                                    intent.putExtra("mobile", mobile);
                                    startActivity(intent);
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Please fill out all the fields", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Please accept Terms and Conditions", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(), "User already exists.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void setValues() {
        //set values
        name=et_name.getText().toString();
        nic=et_nic.getText().toString();
        email=et_email.getText().toString();
        mobile=et_mobile.getText().toString();
        password=et_password.getText().toString();
    }

    private void registerUser(){
        //Registering user according to mobile number.
        database=FirebaseDatabase.getInstance();
        //users/mobile/
        reference = database.getReference("/users/"+mobile);
        reference.child("name").setValue(name);
        reference.child("nic").setValue(nic);
        reference.child("email").setValue(email);
        reference.child("password").setValue(password);
        //default lactation colombo
        reference.child("location").child("latitude").setValue(6.928699);
        reference.child("location").child("longitude").setValue(79.848599);
        reference.child("location").child("visibility").setValue(false);
    }
}
