package com.geasser.marcheauxrabais;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.TotalCaptureResult;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_ENTREPRISES;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_HISTACHAT;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_HISTORIQUE;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_RABAIS;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_RABAISPROFIL;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_SECTEURS;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_SUCCES;
import static com.geasser.marcheauxrabais.BddInt.CREATE_TABLE_SUCCESPROFIL;

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

    public static boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
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
        syncProfil();

        //TODO mise à jour des autres tables (historique et liens)
        syncHistAchat();
        syncRabaisProfil();

    }

    public void syncHistAchat(){
        // mise à jour histachat

        ArrayList<HashMap<String,String>> liste = selection("SELECT * FROM histachat WHERE Utilisateur="+LoginActivity.IDuser,BASE.EXTERNE);
        if(liste != null){
            execute("DELETE FROM histachat WHERE Utilisateur="+LoginActivity.IDuser, BASE.INTERNE);
            for(HashMap<String,String> map : liste){
                ContentValues value = new ContentValues();
                for(String key : map.keySet()){
                    value.put(key,map.get(key));
                }
                mDb.insertWithOnConflict("histachat",null,value,SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    public void syncEntreprise(){
        importation("entreprises");
        importation("secteurs");
    }

    public void syncRabais(){
        importation("rabais");
    }

    public void syncSucces(){
        importation("succes");
    }

    public void syncProfil() {

        ArrayList<HashMap<String, String>> profilOffline = selection("SELECT * FROM profil WHERE ID=" + LoginActivity.IDuser, BASE.INTERNE);
        String id = "ID=" + LoginActivity.IDuser;
        try {
            AsyncTask<String, Void, String> task = new BddExt().execute("SELECT * FROM profil WHERE " + id);
            ArrayList<HashMap<String, String>> profilOnline = BddExt.formate(task.get(), contexte);
            if (profilOffline != null) {
                Integer pas = Integer.parseInt(profilOnline.get(0).get("Pas")) + Integer.parseInt(profilOffline.get(0).get("Stock"));
                profilOnline.get(0).remove("Pas");
                profilOffline.get(0).remove("Pas");
                profilOnline.get(0).put("Pas", pas.toString());
                profilOffline.get(0).put("Pas", pas.toString());
                int stock = Integer.parseInt(profilOffline.get(0).get("Stock"));
                pas = Integer.parseInt(profilOnline.get(0).get("Exp")) + stock;
                profilOnline.get(0).remove("Exp");
                profilOnline.get(0).put("Exp", pas.toString());
                profilOffline.get(0).remove("Stock");
                profilOffline.get(0).put("Stock", "0");

                // update base externe
                for (String key : profilOnline.get(0).keySet()) {
                    execute("UPDATE profil" +
                            " SET " + key + " = '" + profilOnline.get(0).get(key) +
                            "' WHERE ID = " + profilOnline.get(0).get("ID"), BASE.EXTERNE);
                }
                // update base interne
                for (String key : profilOffline.get(0).keySet()) {
                    execute("UPDATE profil" +
                            " SET " + key + " = '" + profilOffline.get(0).get(key) +
                            "' WHERE ID = " + profilOffline.get(0).get("ID"), BASE.INTERNE);
                }
                // update de l'historique
                long ts = new Date().getTime() / 1000; // on divise le ts par 1000 car le unixtime est un timestemp en secondes au lieu d'un timestamp en milisecondes.
                if (Integer.parseInt(selection("SELECT COUNT(Pas) c FROM historique WHERE Utilisateur=" + LoginActivity.IDuser + " AND Date=DATE_FORMAT(FROM_UNIXTIME(" + ts + "), \'%Y-%m-%d\')", BASE.EXTERNE).get(0).get("c")) > 0) {
                    HashMap<String, String> ligne = selection("SELECT ID, Pas FROM historique WHERE Utilisateur=" + LoginActivity.IDuser + " AND Date=DATE_FORMAT(FROM_UNIXTIME(" + ts + "), \'%Y-%m-%d\')", BASE.EXTERNE).get(0);
                    int pasHistorique = Integer.parseInt(ligne.get("Pas"));
                    pasHistorique += stock;
                    Log.i("TEST",""+pasHistorique);
                    execute("UPDATE historique SET Pas=" + pasHistorique + " WHERE ID="+ligne.get("ID"), BASE.EXTERNE);
                } else {
                    execute("INSERT INTO historique (Utilisateur,Pas,Date) VALUES (" + LoginActivity.IDuser + "," + stock + ",FROM_UNIXTIME(" + ts + "))", BASE.EXTERNE);
                }
                //TODO recalculer le niveau
            } else {
                execute("INSERT INTO profil (ID,UserName,MotDePasse,Lvl,Exp,Pas) VALUES (" + profilOnline.get(0).get("ID") + ",'" + profilOnline.get(0).get("UserName") + "','" + profilOnline.get(0).get("MotDePasse") + "'," + profilOnline.get(0).get("Lvl") + "," + profilOnline.get(0).get("Exp") + "," + profilOnline.get(0).get("Pas") + ")", BASE.INTERNE);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void syncRabaisProfil() {
        //mise à jour rabaisprofil

        // recuperation des dates de derniere mise a jour de chaque ligne dans rabaisprofil pour chaque base de donnees.
        ArrayList<HashMap<String,String>> interneRP = selection("SELECT rp.ID, rp.Disponible, ha.Date FROM rabaisprofil rp, histachat ha WHERE rp.IDProfil = ha.Utilisateur AND rp.IDRabais = ha.Rabais AND rp.IDProfil="+LoginActivity.IDuser+" GROUP BY rp.ID ORDER BY ha.Date DESC",BASE.INTERNE);
        ArrayList<HashMap<String,String>> externeRP = selection("SELECT rp.ID, rp.Disponible, ha.Date FROM rabaisprofil rp, histachat ha WHERE rp.IDProfil = ha.Utilisateur AND rp.IDRabais = ha.Rabais AND rp.IDProfil="+LoginActivity.IDuser+" GROUP BY rp.ID ORDER BY ha.Date DESC",BASE.EXTERNE);
        if(interneRP!=null){
            if(externeRP!=null){
                // on supprime toutes les lignes inchangees.
                ArrayList<HashMap<String,String>> temp = new ArrayList<>(interneRP);
                interneRP.removeAll(externeRP);
                externeRP.removeAll(temp);
                // on verifie ici que les tables different et que la mise a jour est necessaire.
                if(!interneRP.isEmpty() && !externeRP.isEmpty()) {
                    // ce qui reste dans chacune sont les lignes qui different.
                    // on cree une map avec comme cle l'id de la ligne et comme valeur la map de la ligne pour reduire la complexite du code.
                    HashMap<Integer, HashMap<String, String>> idMapInterne = mapWithID(interneRP);
                    // puis on itere sur chaque ligne de la base externe pour verifier les changements.
                    // il y a forcement plus de ligne dans l'externe que dans l'interne puisque l'on cree la ligne lors de l'achat dans l'externe.
                    for (HashMap<String, String> ligne : externeRP) {
                        // on verifie si la ligne existe dans la base interne
                        if (!idMapInterne.containsKey(Integer.parseInt(ligne.get("ID")))) {
                            // on l'ajoute si elle n'existe pas
                            syncRabaisProfilVers(Integer.parseInt(ligne.get("ID")), BASE.EXTERNE, BASE.INTERNE);
                        } else {
                            // sinon on recupere la ligne corespondante
                            HashMap<String, String> ligneInterne = idMapInterne.get(Integer.parseInt(ligne.get("ID")));
                            // puis on compare les dates :
                            //long dateExt = Long.parseLong(ligne.get("Date"));
                            //long dateInt = Long.parseLong(ligneInterne.get("Date"));
                            //if (dateExt <= dateInt) {
                                // on est ici possiblement sur l'appareil d'achat
                                if (ligneInterne.get("Disponible").equals("1") && ligne.get("Disponible").equals("0")) {
                                    // on a achete le rabais sur cet appareil mais la base externe n'est pas encore a jour.
                                    execute("UPDATE rabaisprofil SET Disponible=2 WHERE ID=" + ligne.get("ID"), BASE.EXTERNE);
                                } else if (ligneInterne.get("Disponible").equals("3")) {
                                    // on a deja active le rabais, donc on le rend a nouveau dsponible pour tous les appareils
                                    execute("UPDATE rabaisprofil SET Disponible=0 WHERE ID=" + ligne.get("ID"), BASE.EXTERNE);
                                    execute("UPDATE rabaisprofil SET Disponible=0 WHERE ID=" + ligne.get("ID"), BASE.INTERNE);
                                } else if (ligneInterne.get("Disponible").equals("0") && ligne.get("Disponible").equals("2")) {
                                    // on est sur un autre appareil que celui de l'achat donc on rend le rabais indisponible
                                    execute("UPDATE rabaisprofil SET Disponible=2 WHERE ID=" + ligne.get("ID"), BASE.INTERNE);
                                } else if (ligneInterne.get("Disponible").equals("2") && ligne.get("Disponible").equals("0")) {
                                    // le rabais est de nouveau disponible pour tous les appareils donc on le rend disponible en interne
                                    execute("UPDATE rabaisprofil SET Disponible=2 WHERE ID=" + ligne.get("ID"), BASE.INTERNE);
                                }
                            /*} else {
                                // la ligne sur l'appareil est plus ancienne, donc on ne peut pas etre sur l'appareil d'achat
                                // donc on recupere la valeur de la base externe comme valeur de reference
                                syncRabaisProfilVers(Integer.parseInt(ligne.get("ID")), BASE.EXTERNE, BASE.INTERNE);
                            }*/
                        }
                    }
                }
            }
        }else{
            if(externeRP!=null) {
                // on ajoute toutes les lignes de la base externe a la base interne.
                for (HashMap<String, String> map : externeRP) {
                    syncRabaisProfilVers(Integer.parseInt(map.get("ID")), BASE.EXTERNE, BASE.INTERNE);
                }
            }
        }
    }

    private void syncRabaisProfilVers(int idRP, BASE orig, BASE dest){
        ArrayList<HashMap<String,String>> liste = selection("SELECT * FROM rabaisprofil WHERE ID="+idRP,orig);
        // il n'y a qu'une ligne avec cet id donc on ne prend que la map de la premiere ligne
        HashMap<String,String> map = liste.get(0);
        execute("INSERT OR REPLACE INTO rabaisprofil (ID, IDProfil,IDRabais,Disponible) VALUES ("+map.get("ID")+","+map.get("IDProfil")+","+map.get("IDRabais")+","+map.get("Disponible")+")", dest);
    }

    private HashMap<Integer,HashMap<String,String>> mapWithID(ArrayList<HashMap<String,String>> tableau){
        HashMap<Integer,HashMap<String,String>> tableauID = new HashMap<>();
        for(HashMap<String,String> map : tableau){
            tableauID.put(Integer.parseInt(map.get("ID")),map);
        }
        return tableauID;
    }

    public void syncHistorique(){
        // TODO à faire
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
        if(mDb == null){
            open();
        }
        ArrayList<HashMap<String,String>> tabExt = selection("SELECT * FROM "+tableName+" ORDER BY ID",BASE.EXTERNE);
        ArrayList<HashMap<String,String>> tabInt = selection("SELECT * FROM "+tableName+" ORDER BY ID",BASE.INTERNE);
        if(tabExt!=null &&(tabInt==null || !tabExt.equals(tabInt))){
            mDb.delete(tableName,"ID > ?",new String[]{"0"});
            for(HashMap<String,String> map : tabExt){
                ContentValues value = new ContentValues();
                for(String key : map.keySet()){
                    value.put(key,map.get(key));
                }
                mDb.insertWithOnConflict(tableName,null,value,SQLiteDatabase.CONFLICT_REPLACE);
            }
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
