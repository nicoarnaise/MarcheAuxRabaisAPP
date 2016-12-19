package com.geasser.marcheauxrabais;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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


public class EcranPrincipal extends  AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    static TextView textView;
    static int pas = 0;
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


        initialisation();

        //Register BroadcastReceiver to receive event from our service
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

    protected void onResume() {

        // Vérification des challenges validés par l'utilisateur
        if(ControleurBdd.isOnline()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Context mContext = getApplicationContext();
                    int nbTotalPas = Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT SUM(Pas) FROM historique WHERE Utilisateur=" + LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE).get(0).get("SUM(Pas)"));
                    int nbTotalKm = (int) (nbTotalPas * 0.75 / 1000);
                    int nbTotalCalorie = (int) (nbTotalPas * 0.5);
                    int nbTotalRabais = Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM histachat WHERE Utilisateur=" + LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)"));

                    if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=1", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                        ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",1)", ControleurBdd.BASE.EXTERNE);

                    if (nbTotalKm >= 1) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=2", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",2)", ControleurBdd.BASE.EXTERNE);
                    }

                    if (nbTotalKm >= 10) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=3", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",3)", ControleurBdd.BASE.EXTERNE);
                    }

                    if (nbTotalKm >= 100) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=4", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",4)", ControleurBdd.BASE.EXTERNE);
                    }

                    if (nbTotalRabais >= 1) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=5", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",5)", ControleurBdd.BASE.EXTERNE);
                    }

                    if (nbTotalRabais >= 50) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=6", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",6)", ControleurBdd.BASE.EXTERNE);
                    }

                    if (nbTotalCalorie >= 100) {
                        if (Integer.parseInt(ControleurBdd.getInstance(mContext).selection("SELECT COUNT(*) FROM succesprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDSucces=7", ControleurBdd.BASE.EXTERNE).get(0).get("COUNT(*)")) == 0)
                            ControleurBdd.getInstance(mContext).execute("INSERT INTO succesprofil (IDProfil,IDSucces) VALUES (" + LoginActivity.IDuser + ",7)", ControleurBdd.BASE.EXTERNE);
                    }
                }
            }).start();
        }
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }

    public  void onDestroy(){
        if(ControleurBdd.isOnline()) {
            new Thread(new Runnable() {
                public void run() {
                    ControleurBdd.getInstance(getApplicationContext()).syncProfil();
                }
            }).start();
        }
        // Stopper toutes les notifications
        notificationManager.cancelAll();
        // Stopper le service
        stopService(intent);
        unregisterReceiver(myReceiver);
        unregisterReceiver(myReceiver2);
        super.onDestroy();
    }


    // Actualise la valeur du nombre de pas et de la notification
    public void miseAJour(int pas){

        // Détermine si l'utilisateur est connecté avec Facebook ou Google
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

        final ImageButton bChallenges = (ImageButton) findViewById(R.id.btchallenges);
        final ImageButton bProfil = (ImageButton) findViewById(R.id.btprofil);
        final ImageButton bRabais = (ImageButton) findViewById(R.id.btrabais);
        final Button bSettings = (Button) findViewById(R.id.btSettings);
        final ImageButton ibMaps = (ImageButton) findViewById(R.id.ibMaps);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        pseudo = (TextView) findViewById(R.id.Pseudo);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        new Intent(this, EcranPrincipal.class);


        ibMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
                int datapassed = arg1.getIntExtra("DATAPASSED", 0);
                miseAJour(datapassed);
        }

    }

    public class MyReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
                int hello = arg1.getIntExtra("CHOCOLAT",0);

//                NotificationsChallenge(hello);
//                Toast.makeText(EcranPrincipal.this,
//                        "Triggered by Service!\n"
//                                + "Data passed: " + String.valueOf(hello),
//                        Toast.LENGTH_SHORT).show();

        }

    }

}
