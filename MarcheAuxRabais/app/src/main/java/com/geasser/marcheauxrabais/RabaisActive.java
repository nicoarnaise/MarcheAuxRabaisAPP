package com.geasser.marcheauxrabais;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class RabaisActive extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rabais_active);
        Intent intent = getIntent();
        int id = intent.getIntExtra("ID",-1);
        if(id == -1) {
            finish();
        }else{
            HashMap<String,String> rabais = ControleurBdd.getInstance(this).selection("SELECT Nom, Description, Image FROM rabais WHERE ID="+id, ControleurBdd.BASE.INTERNE).get(0);
            ((TextView)findViewById(R.id.titre)).setText(rabais.get("Nom"));
            ((TextView)findViewById(R.id.description)).setText(rabais.get("Description"));
            ((ImageView)findViewById(R.id.barcode)).setImageURI(Uri.parse("android.resource://"+rabais.get("Image")));
        }
    }
}
