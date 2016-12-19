package com.geasser.marcheauxrabais;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class ChallengeActivity extends AppCompatActivity {


    protected ListView mListView;
    protected String SAVE_ADAPTER = "adapter";
    protected ChallengeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mes Succès");
        setContentView(R.layout.activity_challenge);
        mListView = (ListView) findViewById(R.id.listview);
        updateValuesFromBundle(savedInstanceState);
        mListView.setAdapter(adapter);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(SAVE_ADAPTER,adapter);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if(!ControleurBdd.isOnline()) {
                Toast.makeText(this,"Vous devez être connecté à internet pour utiliser cette fonctionnalité",Toast.LENGTH_SHORT).show();
                finish();
            }else{


                ArrayList<HashMap<String,String>> tab = ControleurBdd.getInstance(this).selection("SELECT Nom, Description, Recompense, Image FROM succes", ControleurBdd.BASE.EXTERNE);

                adapter = new ChallengeAdapter(this, tab);
            }
        }
        else{
                adapter = (ChallengeAdapter)savedInstanceState.getSerializable(SAVE_ADAPTER);
            }


    }
}
