package com.geasser.marcheauxrabais;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class EcranPrincipal extends Activity implements SensorEventListener {

    private TextView textView;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_principal);
        textView = (TextView) findViewById(R.id.textView);
        final Button bCarte = (Button) findViewById(R.id.btcarte);
        final Button bChallenges = (Button) findViewById(R.id.btchallenges);
        final Button bProfil = (Button) findViewById(R.id.btprofil);
        final Button bRabais = (Button) findViewById(R.id.btrabais);


        bCarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MapsIntent = new Intent(EcranPrincipal.this, MapsActivity.class);
                EcranPrincipal.this.startActivity(MapsIntent);
            }
        });

        bChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                // LoginActivity.this.startActivity(registerIntent);
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
                //Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                // LoginActivity.this.startActivity(registerIntent);
            }
        });

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // la partie en commentaire ci dessous envoie une requete à la BDD externe et affiche le résultat de la requete, non formaté.
        /*try {
            // On crée une AsyncTask car l'accès à un site internet ne peut se faire que de manière asynchrone sous Android
            AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM entreprises");
            Toast.makeText(this,"Connection ...",Toast.LENGTH_LONG).show();
            // task.get() permet de récupérer la réponse de la base de donnée.
            String rep = task.get();
            ArrayList<HashMap<String,String>> tab = BddExt.formate(rep);
            // après, on affiche simplement le texte retourné.
            textView.setText(tab.toString());
            Toast.makeText(this,"Terminé",Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
        /*
        ControleurBdd control = ControleurBdd.getInstance(this);
        control.open();
        control.synchronize();
        ArrayList<HashMap<String, String>> s = control.selection("SELECT * FROM entreprises");
        textView.setText(s.get(0).get("Nom"));*/
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
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            textView.setText("Step Counter Detected : " + value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Obligatoire quand SensorEventListener est implémeté
    }
}
