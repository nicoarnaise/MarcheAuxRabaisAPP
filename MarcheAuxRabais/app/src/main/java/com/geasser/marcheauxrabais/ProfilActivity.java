package com.geasser.marcheauxrabais;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfilActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        ControleurBdd control = ControleurBdd.getInstance(this);
        control.syncHistorique();
        ArrayList<HashMap<String,String>> historique = control.selection("SELECT Date, Pas FROM historique WHERE Utilisateur="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE);
        if(historique != null){

        }else{

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
