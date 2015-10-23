package com.mig.cpsudev.theblackgate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    Button mReadButton;
    Button mWriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWriteButton = (Button)findViewById(R.id.writeButton);
        mWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WriteActivity.class);
                startActivity(intent);
            }
        });
        mReadButton = (Button)findViewById(R.id.readButton);
        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ReadActivity.class);
                startActivity(intent);
            }
        });
    }
}