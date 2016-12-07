package com.geasser.marcheauxrabais;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChallengeActivity extends AppCompatActivity {


    protected ListView mListView;
    protected ArrayList<Rabais> listR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mes Challenges");
        setContentView(R.layout.activity_challenge);

        mListView = (ListView) findViewById(R.id.listview);

        ArrayList<HashMap<String,String>> tab = ControleurBdd.getInstance(this).selection("SELECT Nom, Description, Recompense, Image FROM succes", ControleurBdd.BASE.EXTERNE);

       ChallengeAdapter adapter = new ChallengeAdapter(this, tab);

        mListView.setAdapter(adapter);
    }
}
