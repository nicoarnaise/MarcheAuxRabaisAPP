package com.geasser.marcheauxrabais;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class LoginActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = "SignInActivity";
    private LoginButton loginButton;
    private CallbackManager callbackManager;
  //  private TextView info;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    public static String pseudo=null;
    public static int IDuser=0;
    static String NameFbk = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Connexion");

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);


        final EditText etUsername = (EditText) findViewById(R.id.etLoginUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPasswordlogin);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        final Button bRegisterHere = (Button) findViewById(R.id.bRegister);
        final TextView registerLink = (TextView) findViewById(R.id.tvRegisterHere);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
     //   info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        // Si l'utilisateur est déjà connecté avec Facebook alors l'envoie à l'ecran principal.
        if ( AccessToken.getCurrentAccessToken()!=null){
            pseudo = AccessToken.getCurrentAccessToken().getUserId();
            NameFbk = Profile.getCurrentProfile().getName();
            IDuser=Integer.parseInt(ControleurBdd.getInstance(this).selection("SELECT ID FROM profil WHERE UserName='"+pseudo+"'", ControleurBdd.BASE.INTERNE).get(0).get("ID"));
            Intent registerIntent = new Intent(LoginActivity.this, EcranPrincipal.class);
            LoginActivity.this.startActivity(registerIntent);
        }

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pseudo = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                if(testProfil(pseudo,password)){
                    NameFbk = null;
                    Intent registerIntent = new Intent(LoginActivity.this, EcranPrincipal.class);
                    LoginActivity.this.startActivity(registerIntent);
                }
                else{
                    Toast.makeText(LoginActivity.this,"Pseudo ou mot de passe erroné",Toast.LENGTH_LONG).show();
                }
            }
        });


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                info.setText(
//                        "User ID: "
//                                + loginResult.getAccessToken().getUserId()
//                                + "\n" +
//                                "Auth Token: "
//                                + loginResult.getAccessToken().getToken()
//                );



                NameFbk = Profile.getCurrentProfile().getName();



                // Si l'ID (username) est présent dans la BDD externe, alors go to écran principal
                if (testProfil(loginResult.getAccessToken().getUserId(),"null")){
                    pseudo = AccessToken.getCurrentAccessToken().getUserId();
                    Intent profil = new Intent(LoginActivity.this, EcranPrincipal.class);
                    LoginActivity.this.startActivity(profil);
                    // Sinon créer l'ID (username) dans la BDD externe et go to écran principal.
                }else{
                    AsyncTask<String, Void, String> task = new BddExt().execute
                            ("INSERT INTO profil (UserName,MotDePasse) VALUES ('"+(loginResult.getAccessToken().getUserId()+"','null');"));
                }
            }

            @Override
            public void onCancel() {
              //  info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
             //   info.setText("Login attempt failed.");
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            // Si l'ID (username) est présent dans la BDD externe, alors go to écran principal.
            if (testProfil(result.getSignInAccount().getId().toString(),"null")){
                pseudo=result.getSignInAccount().getId().toString();
                Intent profil = new Intent(LoginActivity.this, EcranPrincipal.class);
                LoginActivity.this.startActivity(profil);
                // Sinon créer l'ID (username) dans la BDD externe et go to écran principal.
            }else{
                AsyncTask<String, Void, String> task = new BddExt().execute
                        ("INSERT INTO profil (UserName,MotDePasse) VALUES ('"+(result.getSignInAccount().getId().toString()+"','null');"));
                IDuser=Integer.parseInt(ControleurBdd.getInstance(this).selection("SELECT ID WHERE UserName="+result.getSignInAccount().getId(), ControleurBdd.BASE.EXTERNE).get(0).get("ID"));
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
           // info.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            Intent profil = new Intent(LoginActivity.this, EcranPrincipal.class);
            LoginActivity.this.startActivity(profil);

        } else {
            //info.setText(R.string.signed_out);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    public boolean testProfil(String username, String password){

            ArrayList<HashMap<String,String>> tab = ControleurBdd.getInstance(this).selection("SELECT ID, UserName, MotDePasse FROM profil", ControleurBdd.BASE.EXTERNE);
            // On affiche simplement le texte retourné.
            int i =0;
            while (i<tab.size()) {
                //  info.setText(tab.get(i).get("MotDePasse").toString() + " " + password);
                if (tab.get(i).get("UserName").toString().compareTo(username) == 0) {
                    if (tab.get(i).get("MotDePasse").toString().compareTo(password) == 0) {
                        IDuser = Integer.parseInt(tab.get(i).get("ID"));
                        return true;
                    } else {
                        return false;
                    }
                }
                i++;
            }
        return false;
    }

}

