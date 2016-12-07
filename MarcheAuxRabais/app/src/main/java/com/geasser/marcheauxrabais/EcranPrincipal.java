package com.geasser.marcheauxrabais;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.HashMap;


public class EcranPrincipal extends  AppCompatActivity implements SensorEventListener,GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    static TextView textView;
    static int nbPas;
    static int pasSupp=0;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private ControleurBdd control;
    private TextView pseudo;
    private String NOMBRE_PAS = "nombre_pas";
    private String PAS_SUPPLEMENTAIRES = "pas_supplementaires";
    public static NotificationManager notificationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecran_principal);
        control = ControleurBdd.getInstance(this);
        updateValuesFromBundle(savedInstanceState);

        initialisation();
        Toast.makeText(getApplicationContext(),"OnCreate",Toast.LENGTH_LONG).show();

    }

    // onResume est une fonction appellée quand l'activité est au sommet de la pile d'activité donc ne fonctionne pas en arrière-plan.
    protected void onResume() {
        super.onResume();
        // -pasSupp=0;
        miseAJour();
        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);
        Toast.makeText(getApplicationContext(),"OnResume",Toast.LENGTH_LONG).show();
    }

    // Called when the activity is no longer visible to the user, because another activity has been resumed and is covering this one.
    protected void onStop() {
       // Toast.makeText(getApplicationContext(),"OnStop",Toast.LENGTH_LONG).show();
        super.onStop();
    }

    public  void onDestroy(){
        Toast.makeText(getApplicationContext(),"OnDestroy",Toast.LENGTH_LONG).show();
        notificationManager.cancelAll();
        super.onDestroy();
        notificationManager.cancel(0);
        mSensorManager.unregisterListener(this, mStepCounterSensor);
    }

//    @Override
//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_HOME)
//        {
//            // do your stuff here
//            Toast.makeText(getApplicationContext(),"Bite",Toast.LENGTH_SHORT).show();
//            return true;
//        }
//        return super.onKeyLongPress(keyCode, event);
//    }

    @Override
    protected void onUserLeaveHint()
    {
        Toast.makeText(getApplicationContext(),"Bite",Toast.LENGTH_SHORT).show();
        super.onUserLeaveHint();
    }


    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        // values[0]: Acceleration minus Gx on the x-axis , 1 --> y, 2--> z
        float[] values = event.values;
       // int value = -1;

        if (values.length > 0) {
          //  value = (int) values[0];
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                pasSupp++;
                control.execute("UPDATE profil SET Stock='" + pasSupp + "' WHERE UserName='" + LoginActivity.pseudo + "';", ControleurBdd.BASE.INTERNE);
                miseAJour();
            }
        }


        //Enregistre le nombre de pas dans la bdd interne tous les 10pas
        //   if(nbTot%10==0)

    }



   public static void UpdatePas (int pas){
        nbPas=pas;
        pasSupp=0;
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

    private void miseAJour(){

        ArrayList<HashMap<String, String>> tab = control.selection("SELECT Pas FROM profil WHERE UserName='"+LoginActivity.pseudo+"';",ControleurBdd.BASE.INTERNE);
        if (LoginActivity.NameAPI!= null)
            pseudo.setText(LoginActivity.NameAPI );
        else
            pseudo.setText(LoginActivity.pseudo);

        pseudo.setText(pseudo.getText() + " : "+Integer.toString(nbPas+pasSupp) + " pas");


        Bitmap myBitmap1 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launchershoess);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("MarcheAuxRabais")
                .setContentText(pseudo.getText())
                .setLargeIcon(myBitmap1)
                .setSmallIcon( R.mipmap.ic_launchershoess)

                .setAutoCancel(true).build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);

    }

    public void initialisation(){

        textView = (TextView) findViewById(R.id.textView);
        final ImageButton bChallenges = (ImageButton) findViewById(R.id.btchallenges);
        final ImageButton bProfil = (ImageButton) findViewById(R.id.btprofil);
        final ImageButton bRabais = (ImageButton) findViewById(R.id.btrabais);
        final Button bSettings = (Button) findViewById(R.id.btSettings);
        final ImageButton ibMaps = (ImageButton) findViewById(R.id.ibMaps);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        pseudo = (TextView) findViewById(R.id.Pseudo);
        //  control.execute("DELETE FROM histachat", ControleurBdd.BASE.INTERNE);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, EcranPrincipal.class);




        ibMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                notificationManager.cancelAll();
                Intent MapsIntent = new Intent(EcranPrincipal.this, MapsActivity.class);
                EcranPrincipal.this.startActivity(MapsIntent);
                // On crée un utilitaire de configuration pour cette animation
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
                // On l'affecte au widget désiré, et on démarre l'animation
                ibMaps.startAnimation(animation);

            }
        });



        bChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On crée un utilitaire de configuration pour cette animation
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
                // On l'affecte au widget désiré, et on démarre l'animation
                bChallenges.startAnimation(animation);
                Intent ChallengeIntent = new Intent(EcranPrincipal.this, ChallengeActivity.class);
                EcranPrincipal.this.startActivity(ChallengeIntent);
            }
        });

        bProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On crée un utilitaire de configuration pour cette animation
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
                // On l'affecte au widget désiré, et on démarre l'animation
                bProfil.startAnimation(animation);
                Intent profilIntent = new Intent(EcranPrincipal.this, ProfilActivity.class);
                EcranPrincipal.this.startActivity(profilIntent);
            }
        });

        bRabais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On crée un utilitaire de configuration pour cette animation
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
                // On l'affecte au widget désiré, et on démarre l'animation
                bRabais.startAnimation(animation);
                Intent RabaisIntent = new Intent(EcranPrincipal.this, RabaisActivity.class);
                EcranPrincipal.this.startActivity(RabaisIntent);
            }
        });


        callbackManager = CallbackManager.Factory.create();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On crée un utilitaire de configuration pour cette animation
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
                // On l'affecte au widget désiré, et on démarre l'animation
                bSettings.startAnimation(animation);

                // Si connecté avec fb appuie sur le bouton log out qui est caché
                if (AccessToken.getCurrentAccessToken() != null) {
                    LoginManager.getInstance().logOut();
                    Intent registerIntent = new Intent(EcranPrincipal.this, LoginActivity.class);
                    EcranPrincipal.this.startActivity(registerIntent);
                    finish();
                } else {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    // [START_EXCLUDE]
                                    // updateUI(false);
                                    // [END_EXCLUDE]
                                }
                            });
                    Intent registerIntent = new Intent(EcranPrincipal.this, LoginActivity.class);
                    EcranPrincipal.this.startActivity(registerIntent);
                    finish();
                }

                finish();
            }
        });

        // Relatif au comptage des pas
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Obligatoire quand SensorEventListener est implémenté
    }


    @Override
    public void onBackPressed(){

    }

}
