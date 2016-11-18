package com.geasser.marcheauxrabais;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_ENTREPRISES;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_RABAIS;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_SECTEURS;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_SUCCES;

/**
 *  Cette classe permet la gestion simplifiée de l'accès aux bases de données internes et externes.
 *  On peut envoyer une requete soit a la base interne, soit a la base externe aussi simplement et
 *  on recoit une reponse harmonisee.
 */
public class ControleurBdd {
    private BddExt externe = null;
    private BddInt interne = null;
    protected SQLiteDatabase mDb = null;
    private static ControleurBdd instance = null;
    private Context contexte;

    /**
     * Cet enum permet de savoir a quelle base on veut s'adresser.
     */
    public enum BASE{
        INTERNE,
        EXTERNE
    }

    /**
     * Constructeur prive pour respecter le pattern du Singleton
     * @param contexte
     */
    private ControleurBdd(Context contexte){
        externe = new BddExt();
        interne = BddInt.getInstance(contexte);
        this.contexte = contexte;
    }

    /**
     * Permet de recuperer l'instance du controleur de bases de donnees
     * @param contexte
     * @return
     */
    public static ControleurBdd getInstance(Context contexte){
        if(instance == null){
            instance = new ControleurBdd(contexte);
        }
        return instance;
    }

    /**
     * permet l'ouverture de la base de donnees interne
     * @return la base ouverte
     */
    public SQLiteDatabase open(){
        mDb = interne.getWritableDatabase();
        return mDb;
    }

    /**
     * permet l'envoie d'une requete SELECT a la base de donnes en parametre
     * @param SQLReq String de la requete a executer
     * @return une liste contenant les lignes de la selection, contenant chacune les colonnes et
     *  leurs valeurs sous forme de HashMap
     */
    public ArrayList<HashMap<String,String>> selection(String SQLReq, BASE b){
        if(b == BASE.INTERNE) {
            if(mDb == null){
                open();
            }
            mDb = interne.getReadableDatabase();
            Cursor c = mDb.rawQuery(SQLReq, null);
            if (c.moveToFirst()) {
                ArrayList<HashMap<String, String>> tab = new ArrayList<HashMap<String, String>>();
                while (!c.isAfterLast()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    String[] keys = c.getColumnNames();
                    for (String key : keys) {
                        String value = c.getString(c.getColumnIndex(key));
                        map.put(key, value);
                    }
                    tab.add(map);
                    c.moveToNext();
                }
                c.close();
                return tab;
            } else {
                c.close();
                return null;
            }
        }else{
            try {
                AsyncTask<String, Void, String> task = new BddExt().execute(SQLReq);
                return BddExt.formate(task.get(),contexte);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Permet la synchronisation entre la base Externe et la base Interne
     */
    public void synchronize(){

        if(mDb == null){
            open();
        }

        // Importation des tables en ligne
        // --------------------------------
        importation("entreprises");
        importation("rabais");
        importation("secteurs");
        importation("succes");

        // Mise à jour du profil
        // ----------------------
        // Récupération des données offline
        // Récupération des données online
        // Pas(OnLine) += Stock(OffLine)
        // Pas(OffLine) = Pas(OnLine)
        // Exp(OnLine) += Stock(OffLine)
        // Stock(OffLine) = 0
        // recalcul lvl

        // TODO : TESTS !
        try {
            ArrayList<HashMap<String,String>> profilOffline = selection("SELECT * FROM profil ORDER BY ID", BASE.INTERNE);
            String ids = "";
            for (HashMap<String,String> ligne:profilOffline) {
                ids += " OR ID="+ligne.get("ID");
            }
            ids = ids.substring(4);
            AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM profil WHERE "+ids+" ORDER BY ID");
            ArrayList<HashMap<String,String>> profilOnline = BddExt.formate(task.get(),contexte);
            for(int i = 0; i<profilOnline.size();i++){
                Integer pas = Integer.parseInt(profilOnline.get(i).get("Pas"))+Integer.parseInt(profilOffline.get(i).get("Stock"));
                profilOnline.get(i).remove("Pas");
                profilOffline.get(i).remove("Pas");
                profilOnline.get(i).put("Pas",pas.toString());
                profilOffline.get(i).put("Pas",pas.toString());
                pas = Integer.parseInt(profilOnline.get(i).get("Exp"))+Integer.parseInt(profilOffline.get(i).get("Stock"));
                profilOnline.get(i).remove("Exp");
                profilOnline.get(i).put("Exp",pas.toString());
                profilOffline.get(i).remove("Stock");
                profilOffline.get(i).put("Stock","0");

                // update base externe
                for(String key : profilOnline.get(i).keySet()) {
                    execute("UPDATE profil" +
                            " SET " + key + " = '" + profilOnline.get(i).get(key) +
                            "' WHERE ID = " + profilOnline.get(i).get("ID"),BASE.EXTERNE);
                }
                // update base interne
                for(String key : profilOffline.get(i).keySet()) {
                    execute("UPDATE profil" +
                            " SET " + key + " = '" + profilOffline.get(i).get(key) +
                            "' WHERE ID = " + profilOffline.get(i).get("ID"),BASE.INTERNE);
                }

                //TODO recalculer le niveau
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //TODO mise à jour des autres tables (historique et liens)
    }

    /**
     * permet la fermeture de l'acces a la base interne
     */
    public void close(){
        mDb = null;
    }

    /**
     * Permet l'importation de la table tableName depuis la base Externe vers la base Interne
     * @param tableName
     */
    private void importation(String tableName){
        AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM "+tableName);
        if(mDb == null){
            open();
        }
        mDb.delete(tableName,"ID > ?",new String[]{"0"});
        try {
            String rep = task.get();
            ArrayList<HashMap<String,String>> tab = BddExt.formate(rep,contexte);
            for(HashMap<String,String> map : tab){
                ContentValues value = new ContentValues();
                for(String key : map.keySet()){
                    value.put(key,map.get(key));
                }
                mDb.insertWithOnConflict(tableName,null,value,SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet l'execution de la requete par la base de donnee voulue.
     * @param requete
     */
    public void execute(String requete, BASE b){
        if(b==BASE.INTERNE) {
            if (mDb == null) {
                open();
            }
            mDb.execSQL(requete);
        }else{
            AsyncTask<String, Void, String> task = new BddExt().execute(requete);
        }
    }

    /**
     * Cette fonction permet d'importer un profil en ligne sur le smartphone.
     * @param ID l'id de la ligne du profil dans la base de données en lignes.
     */
    public void ajouteProfil(int ID){
        ArrayList<HashMap<String,String>> profil = selection("SELECT * FROM profil WHERE ID="+ID,BASE.EXTERNE);
        String colomns = "INSERT INTO profil (";
        String values = ") VALUES (";
        for(String key : profil.get(0).keySet()){
            colomns = colomns+key+",";
            values = values+profil.get(0).get(key)+",";
        }
        String request = colomns.substring(0,colomns.length()-2)+ values.substring(0,values.length()-2)+");";
        execute(request,BASE.INTERNE);
    }
}
