package com.geasser.marcheauxrabais;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;


public class EcranPrincipal extends Activity implements SensorEventListener {

    static TextView textView;
    static int nbPas;
    static int pasSupp=0;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private ControleurBdd control;

    private String NOMBRE_PAS = "nombre_pas";
    private String PAS_SUPPLEMENTAIRES = "pas_supplementaires";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_principal);
        control = ControleurBdd.getInstance(this);
        updateValuesFromBundle(savedInstanceState);

        textView = (TextView) findViewById(R.id.textView);
       // final ImageButton bCarte = (ImageButton) findViewById(R.id.btcarte);
        final ImageButton bChallenges = (ImageButton) findViewById(R.id.btchallenges);
        final ImageButton bProfil = (ImageButton) findViewById(R.id.btprofil);
        final ImageButton bRabais = (ImageButton) findViewById(R.id.btrabais);
        final Button bSettings = (Button) findViewById(R.id.btSettings);
        final ImageButton ibMaps = (ImageButton) findViewById(R.id.ibMaps);


        updateTextView();


//        bCarte.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent MapsIntent = new Intent(EcranPrincipal.this, MapsActivity.class);
//                EcranPrincipal.this.startActivity(MapsIntent);
//            }
//        });

        ibMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MapsIntent = new Intent(EcranPrincipal.this, MapsActivity.class);
                EcranPrincipal.this.startActivity(MapsIntent);
            }
        });

        bChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ChallengeIntent = new Intent(EcranPrincipal.this, ChallengeActivity.class);
                EcranPrincipal.this.startActivity(ChallengeIntent);
            }
        });

        bProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profilIntent = new Intent(EcranPrincipal.this, ProfilActivity.class);
                EcranPrincipal.this.startActivity(profilIntent);
            }
        });

        bRabais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RabaisIntent = new Intent(EcranPrincipal.this, RabaisActivity.class);
                EcranPrincipal.this.startActivity(RabaisIntent);
            }
        });

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SettingsIntent = new Intent(EcranPrincipal.this, SettingsActivity.class);
                EcranPrincipal.this.startActivity(SettingsIntent);
            }
        });
        
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }

    // onResume est une fonction appellée quand l'activité est au sommet de la pile d'activité donc ne fonctionne pas en arrière-plan.
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
    }
    // Called when the activity is no longer visible to the user, because another activity has been resumed and is covering this one.
    protected void onStop() {
        super.onStop();
    }
    public void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(this, mStepCounterSensor);

    }
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        // values[0]: Acceleration minus Gx on the x-axis , 1 --> y, 2--> z
        float[] values = event.values;
       // int value = -1;

        if (values.length > 0) {
          //  value = (int) values[0];
            pasSupp++;
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER)
            updateTextView();
            control.execute("UPDATE profil SET Stock='" + pasSupp + "' WHERE UserName='" + LoginActivity.pseudo + "';", ControleurBdd.BASE.INTERNE);

        //Enregistre le nombre de pas dans la bdd interne tous les 10pas
        //   if(nbTot%10==0)

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Obligatoire quand SensorEventListener est implémenté
    }

    public static void updateTextView (){

//        if (LoginActivity.NameAPI !=null)
//            textView.setText(LoginActivity.NameAPI + " Nombre de pas en stock : " + (nbPas+pasSupp));
//        else
//            textView.setText(LoginActivity.pseudo + " Nombre de pas en stock : " + (nbPas+pasSupp));
    }

   public static void UpdatePas (int pas){
        nbPas=pas;
        pasSupp=0;
        updateTextView();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(NOMBRE_PAS,nbPas);
        savedInstanceState.putInt(PAS_SUPPLEMENTAIRES,pasSupp);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            if (savedInstanceState.keySet().contains(NOMBRE_PAS)) {
                nbPas = savedInstanceState.getInt(NOMBRE_PAS);
            }

            if (savedInstanceState.keySet().contains(PAS_SUPPLEMENTAIRES)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                pasSupp = savedInstanceState.getInt(PAS_SUPPLEMENTAIRES);
            }
        }
        else{

            // Synchronisation de la table profil des BDD interne et externe
            control.syncProfil();
            ArrayList<HashMap<String, String>> tab = control.selection("SELECT Pas FROM profil WHERE UserName='"+LoginActivity.pseudo+"';",ControleurBdd.BASE.INTERNE);

            if(tab!=null)
                nbPas = Integer.parseInt(tab.get(0).get("Pas"));
            else
                nbPas = 0;
        }
    }



//    // Empeche le retour arrière
//    @Override
//    public void onBackPressed(){
//
//    }
}
