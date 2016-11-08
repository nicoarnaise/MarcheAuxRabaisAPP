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

/**
 * Created by Nicolas on 07/11/2016.
 */

public class ControleurBdd {
    private BddExt externe = null;
    private BddInt interne = null;
    protected SQLiteDatabase mDb = null;
    private static ControleurBdd instance = null;

    private ControleurBdd(Context contexte){
        externe = new BddExt();
        interne = BddInt.getInstance(contexte);
    }

    public static ControleurBdd getInstance(Context contexte){
        if(instance == null){
            instance = new ControleurBdd(contexte);
        }
        return instance;
    }

    public SQLiteDatabase open(){
        mDb = interne.getWritableDatabase();
        return mDb;
    }

    public ArrayList<HashMap<String,String>> selection(String SQLReq){
        mDb = interne.getReadableDatabase();
        Cursor c = mDb.rawQuery(SQLReq, null);
        if(c.moveToFirst()){
            ArrayList<HashMap<String,String>> tab = new ArrayList<HashMap<String,String>>();
            while(!c.isAfterLast()) {
                HashMap<String, String> map = new HashMap<String, String>();
                String[] keys = c.getColumnNames();
                for (String key : keys) {
                    String value = c.getString(c.getColumnIndex(key));
                    map.put(key, value);
                }
                tab.add(map);
                c.moveToNext();
            }
            return tab;
        }else{
            return null;
        }
    }

    public void synchronize(){

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
            ArrayList<HashMap<String,String>> profilOffline = selection("SELECT * FROM profil ORDER BY ID");
            String ids = "";
            for (HashMap<String,String> ligne:profilOffline) {
                ids += " OR ID="+ligne.get("ID");
            }
            ids = ids.substring(4);
            AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM profil WHERE "+ids+" ORDER BY ID");
            ArrayList<HashMap<String,String>> profilOnline = BddExt.formate(task.get());
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
                    AsyncTask<String, Void, String> task1 = new BddExt().execute("UPDATE profil" +
                            " SET " + key + " = " + profilOnline.get(i).get(key) +
                            " WHERE ID = " + profilOnline.get(i).get("ID"));
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

    public void close(){
        mDb = null;
    }

    private void importation(String tableName){
        AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM "+tableName);
        mDb.delete(tableName,"ID > ?",new String[]{"0"});
        try {
            String rep = task.get();
            ArrayList<HashMap<String,String>> tab = BddExt.formate(rep);
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
}
