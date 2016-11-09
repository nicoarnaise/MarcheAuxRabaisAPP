package com.geasser.marcheauxrabais;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.os.FileObserver.CREATE;

/**
 * Created by Nicolas on 02/11/2016.
 */

public class BddInt extends SQLiteOpenHelper {

    private static BddInt instance = null;
    private Context context = null;

    public static String CREATE_TABLE_ENTREPRISES =
            "CREATE TABLE IF NOT EXISTS "+"entreprises"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Nom"+" TEXT NOT NULL, "+
                    "Adresse"+" TEXT NOT NULL, "+
                    "Logo"+" TEXT, "+
                    "Secteur"+" INTEGER NOT NULL DEFAULT 1);";
    public static String CREATE_TABLE_HISTACHAT =
            "CREATE TABLE IF NOT EXISTS "+"histachat"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Utilisateur"+" INT NOT NULL, "+
                    "DATE"+" TEXT NOT NULL, "+
                    "Rabais"+" INTEGER NOT NULL);";
    public static String CREATE_TABLE_HISTORIQUE =
            "CREATE TABLE IF NOT EXISTS "+"historique"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Utilisateur"+" INT NOT NULL, "+
                    "DATE"+" TEXT NOT NULL, "+
                    "Pas"+" INTEGER NOT NULL, "+
                    "Km"+" INTEGER NOT NULL, "+
                    "Kcal"+" INTEGER NOT NULL);";
    public static String CREATE_TABLE_PROFIL =
            "CREATE TABLE IF NOT EXISTS "+"profil"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "UserName"+" TEXT NOT NULL, "+
                    "MotDePasse"+" TEXT, "+
                    "Lvl"+" INTEGER NOT NULL DEFAULT 1, "+
                    "Exp"+" INTEGER NOT NULL DEFAULT 0, "+
                    "Pas"+" INTEGER NOT NULL DEFAULT 0, "+
                    "Stock"+" INTEGER NOT NULL DEFAULT 0);";
    public static String CREATE_TABLE_RABAIS =
            "CREATE TABLE IF NOT EXISTS "+"rabais"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Nom"+" TEXT NOT NULL, "+
                    "Description"+" TEXT NOT NULL, "+
                    "Cout"+" INTEGER NOT NULL, "+
                    "Valeur"+" REAL NOT NULL, "+
                    "Entreprise"+" INTEGER NOT NULL, "+
                    "Secteur"+" INTEGER NOT NULL DEFAULT 1, "+
                    "Image"+" TEXT NOT NULL, "+
                    "Lvl"+" INTEGER); ";
    public static String CREATE_TABLE_SECTEURS =
            "CREATE TABLE IF NOT EXISTS "+"secteurs"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Nom"+" TEXT NOT NULL, "+
                    "Image"+" TEXT);";
    public static String CREATE_TABLE_SUCCES =
            "CREATE TABLE IF NOT EXISTS "+"succes"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "Nom"+" TEXT NOT NULL, "+
                    "Description"+" TEXT NOT NULL, "+
                    "Recompense"+" INTEGER NOT NULL, "+
                    "Image"+" TEXT NOT NULL);";
    public static String CREATE_TABLE_SUCCESPROFIL =
            "CREATE TABLE IF NOT EXISTS "+"succesprofil"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "IDProfil"+" INTEGER NOT NULL, "+
                    "IDSucces"+" INTEGER NOT NULL, "+
                    "Date"+" TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP);";
    public static String CREATE_TABLE_RABAISPROFIL =
            "CREATE TABLE IF NOT EXISTS "+"rabaisprofil"+" ("+
                    "ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    "IDProfil"+" INTEGER NOT NULL, "+
                    "IDRabais"+" INTEGER NOT NULL, "+
                    "Disponible"+" INTEGER NOT NULL DEFAULT 0, "+
                    "Active"+" INTEGER NOT NULL DEFAULT 0);";


    private BddInt(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public static BddInt getInstance(Context context){
        if(instance == null){
            instance = new BddInt(context, "BddInterne", null, 1);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTREPRISES);
        db.execSQL(CREATE_TABLE_HISTACHAT);
        db.execSQL(CREATE_TABLE_HISTORIQUE);
        db.execSQL(CREATE_TABLE_PROFIL);
        db.execSQL(CREATE_TABLE_RABAIS);
        db.execSQL(CREATE_TABLE_SECTEURS);
        db.execSQL(CREATE_TABLE_SUCCES);
        db.execSQL(CREATE_TABLE_SUCCESPROFIL);
        db.execSQL(CREATE_TABLE_RABAISPROFIL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ControleurBdd.getInstance(context).synchronize();
    }


}
