package com.example.johny.chatwithme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_activity);

        recieverUserID = getIntent().getExtras().get("visiUserID").toString();

        Toast.makeText(this, "User ID: " + recieverUserID, Toast.LENGTH_SHORT).show();
    }
}
