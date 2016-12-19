package com.geasser.marcheauxrabais;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Inscription");
        setContentView(R.layout.activity_register);

        final EditText etUsername = (EditText) findViewById(R.id.etRegisterUserName);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final EditText etPasswordConfirm = (EditText) findViewById(R.id.etPasswordconfirm);
        final Button bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ControleurBdd.isOnline()) {
                    final String username = etUsername.getText().toString();
                    final String password = etPassword.getText().toString();
                    final String passwordConfirm = etPasswordConfirm.getText().toString();

                    if (tailleValideUsername(username) && tailleValidePassword(password) && confirmerPassword(password, passwordConfirm)) {
                        AsyncTask<String, Void, String> task = new BddExt().execute
                                ("INSERT INTO profil (UserName,MotDePasse) VALUES ('" + username + "','" + password + "');");
                        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                        RegisterActivity.this.startActivity(registerIntent);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Connectez-vous à internet pour créer votre compte",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public boolean confirmerPassword(String password, String passwordConfirm) {
        if (password.compareTo(passwordConfirm) == 0) return true;
        else {
            Toast.makeText(RegisterActivity.this, "Les mots de passes ne sont pas identiques", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public boolean tailleValideUsername (String wordtested){
        if (wordtested.length()>5)
            return true;
        else
            Toast.makeText(RegisterActivity.this, "Nom d'utilisateur trop court, minimum 6 caractères.", Toast.LENGTH_LONG).show();
            return false;
    }

    public boolean tailleValidePassword (String wordtested){
        if (wordtested.length()>5)
            return true;
        else
            Toast.makeText(RegisterActivity.this, "Mot de Passe trop court, minimum 6 caractères.", Toast.LENGTH_LONG).show();
        return false;
    }
}
