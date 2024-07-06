package com.example.abuser_stop_abusing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText edit1 = (EditText)findViewById(R.id.edit1);
        EditText edit2 = (EditText)findViewById(R.id.edit2);
        EditText edit3 = (EditText)findViewById(R.id.edit3);
        Button btn=(Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("message/html");
                i.putExtra(Intent.EXTRA_EMAIL, new String("xyz@gmail.com"));
                i.putExtra(Intent.EXTRA_SUBJECT, "New report" );
                i.putExtra(Intent.EXTRA_TEXT, "Name: "+edit1.getText() + "\n Level: " + edit2.getText() + "\nReport description: " + edit3.getText());

                try{

                    startActivity(Intent.createChooser(i, "Please select Email"));
                }
                catch(android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(MainActivity3.this, "There are no email clients", Toast.LENGTH_SHORT).show();

                }

            }
        });


    }
}