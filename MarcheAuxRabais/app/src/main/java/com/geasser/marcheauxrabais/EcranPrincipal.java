package com.geasser.marcheauxrabais;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
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
import java.util.Date;
import java.util.HashMap;

import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.PARTIAL_WAKE_LOCK;


public class EcranPrincipal extends  AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    static TextView textView;
    static int pas = 0;
    protected int ThreadPAs = 0;
    private ControleurBdd control;
    private TextView pseudo;
    public static NotificationManager notificationManager;


    protected Intent intent;
    MyReceiver myReceiver;
    MyReceiver2 myReceiver2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ecran_principal);
        control = ControleurBdd.getInstance(this);
        updateValuesFromBundle(savedInstanceState);

        initialisation();

        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServicePas.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        myReceiver2 = new MyReceiver2();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(ServicePas.MY_PONEY);
        registerReceiver(myReceiver2, intentFilter2);

        //Start our own service
        intent = new Intent(EcranPrincipal.this,com.geasser.marcheauxrabais.ServicePas.class);
        startService(intent);


    }

    // onResume est une fonction appellée quand l'activité est au sommet de la pile d'activité donc ne fonctionne pas en arrière-plan.
    protected void onResume() {
        super.onResume();
    }

    // Called when the activity is no longer visible to the user, because another activity has been resumed and is covering this one.
    protected void onStop() {
       // Toast.makeText(getApplicationContext(),"OnStop",Toast.LENGTH_LONG).show();
      //  unregisterReceiver(myReceiver);
        super.onStop();
}

    public  void onDestroy(){
        notificationManager.cancelAll();
        super.onDestroy();
        stopService(intent);
        notificationManager.cancel(0);
   //     mSensorManager.unregisterListener(this, mStepCounterSensor);
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putInt(NOMBRE_PAS,nbPas);
//        savedInstanceState.putInt(PAS_SUPPLEMENTAIRES,pasSupp);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

//            if (savedInstanceState.keySet().contains(NOMBRE_PAS)) {
//                nbPas = savedInstanceState.getInt(NOMBRE_PAS);
//            }
//
//            if (savedInstanceState.keySet().contains(PAS_SUPPLEMENTAIRES)) {
//                // Since LOCATION_KEY was found in the Bundle, we can be sure that
//                // mCurrentLocationis not null.
//                pasSupp = savedInstanceState.getInt(PAS_SUPPLEMENTAIRES);
//            }
//        }
//        else{
//            // Synchronisation de la table profil des BDD interne et externe
//            control.syncProfil();
//            ArrayList<HashMap<String, String>> tab = control.selection("SELECT Pas FROM profil WHERE UserName='"+LoginActivity.pseudo+"';",ControleurBdd.BASE.INTERNE);
//
//            if(tab!=null)
//                nbPas = Integer.parseInt(tab.get(0).get("Pas"));
//            else
//                nbPas = 0;
        }
    }

    public void miseAJour(int pas){

        if (LoginActivity.NameAPI!= null)
            pseudo.setText(LoginActivity.NameAPI );
        else
            pseudo.setText(LoginActivity.pseudo);

        pseudo.setText(pseudo.getText() + " : "+Integer.toString(pas) + " pas");

        Bitmap myBitmap1 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launchershoess);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("MarcheAuxRabais")
                .setContentText(pseudo.getText())
                .setLargeIcon(myBitmap1)
                .setSmallIcon( R.mipmap.ic_launchershoess)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
        new Intent(this, EcranPrincipal.class);




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
    }

    public void NotificationsChallenge (int i){

        if (i==1){
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("MarcheAuxRabais")
                    .setContentText(pseudo.getText())
                    .setSmallIcon( R.mipmap.caddie)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true).build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(i, notification);
        }

        else if (i==2){
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("MarcheAuxRabais")
                    .setContentText(pseudo.getText())
                    .setSmallIcon( R.mipmap.balance)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true).build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(i, notification);
        }



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed(){

    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                int datapassed = arg1.getIntExtra("DATAPASSED", 0);
                miseAJour(datapassed);
        }

    }

    public class MyReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                int hello = arg1.getIntExtra("CHOCOLAT",0);

                NotificationsChallenge(hello);
                Toast.makeText(EcranPrincipal.this,
                        "Triggered by Service!\n"
                                + "Data passed: " + String.valueOf(hello),
                        Toast.LENGTH_SHORT).show();




        }

    }

}
