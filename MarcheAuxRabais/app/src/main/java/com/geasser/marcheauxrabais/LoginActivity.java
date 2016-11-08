package com.geasser.marcheauxrabais;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static android.R.attr.data;
import static com.geasser.marcheauxrabais.R.id.textView;
import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class LoginActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final String TAG = "SignInActivity";
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private TextView info;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);

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
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();


                if(testProfil(username,password)){
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
                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );

                if (testProfil(loginResult.getAccessToken().getUserId(),"null")){
                    Intent profil = new Intent(LoginActivity.this, EcranPrincipal.class);
                    LoginActivity.this.startActivity(profil);
                }else{
                    //info.setText(
                           // "error"
                   // );
                }

            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
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
            if (testProfil(result.getSignInAccount().getId().toString(),"null")){
                Intent profil = new Intent(LoginActivity.this, EcranPrincipal.class);
                LoginActivity.this.startActivity(profil);
            }else{
                //info.setText(
                // "error"
                // );
            }
            Toast.makeText(LoginActivity.this,result.getSignInAccount().getId().toString(),Toast.LENGTH_LONG).show();

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
            // ...
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
            info.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
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
            info.setText(R.string.signed_out);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    private boolean testProfil(String username, String password){
        AsyncTask<String, Void, String> task = new BddExt().execute("SELECT UserName, MotDePasse FROM profil");
        try {
            // task.get() permet de récupérer la réponse de la base de donnée.
            String rep = task.get();
            ArrayList<HashMap<String,String>> tab = BddExt.formate(rep);
            // après, on affiche simplement le texte retourné.
            int i =0;
            while (i<tab.size()){
                info.setText(tab.get(i).get("MotDePasse").toString() + " " + password);
                if(tab.get(i).get("UserName").toString().compareTo(username)==0){
                    if(tab.get(i).get("MotDePasse").toString().compareTo(password)==0){

                        return true;
                    }else {

                        return false;
                    }
                }
                i++;
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

}

