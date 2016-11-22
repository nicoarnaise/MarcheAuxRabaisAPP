package com.geasser.marcheauxrabais;

import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class RabaisActivity extends AppCompatActivity implements RabaisAdapter.RabaisAdapterListener {

    protected HashMap<Integer,Rabais> listActive = new HashMap<Integer, Rabais>();
    protected ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mes Rabais");
        setContentView(R.layout.activity_rabais);

        ControleurBdd control = ControleurBdd.getInstance(this);
        //Récupération de la liste des personnes
        /*ArrayList<Rabais> listR = Rabais.getAListOfRabais();*/
        control.synchronize();
        ArrayList<Rabais> listR = toRabais(control.selection("SELECT ID, Image, Nom, Cout, Description FROM rabais", ControleurBdd.BASE.INTERNE));

        //Création et initialisation de l'Adapter pour les personnes
        RabaisAdapter adapter = new RabaisAdapter(this, listR);


        //Ecoute des évènements sur votre liste
        adapter.addListener(this);

        //Récupération du composant ListView
        list = (ListView)findViewById(R.id.ListView01);

        //Initialisation de la liste avec les données
        list.setAdapter(adapter);

        /*//Timer des temps impartis pour les rabais activés
        int i = 0;
        for (Rabais item : listR){
            if (item.active){
                listActive.put(i,item);
            }
            i++;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        for(Integer Tag : listActive.keySet()){
                            TextView txt = (TextView) list.findViewWithTag(Tag).findViewById(R.id.activation);
                            int timeLeft = listActive.get(Tag).getTimeLeft();
                            if (timeLeft==0){
                                expiration(listActive.get(Tag));
                                txt.setText("Activer le rabais");
                                txt.setAlpha(0.1f);
                            }
                            else {
                                txt.setText(""+timeLeft);
                            }
                        }
                    }
                });
            }
        },0,1000);*/
    }

    protected void expiration(Rabais item){
        ControleurBdd control = ControleurBdd.getInstance(this);
        int idjoin=Integer.parseInt(control.selection("SELECT ID WHERE IDProfil="+LoginActivity.IDuser+", IDRabais="+item.ID, ControleurBdd.BASE.EXTERNE).get(0).get("ID"));
        control.execute("UPDATE rabaisprofil SET Disponible=0 WHERE ID="+idjoin, ControleurBdd.BASE.INTERNE);
    }

    private ArrayList<Rabais> toRabais(ArrayList<HashMap<String, String>> selection) {
        ArrayList<Rabais> listR = new ArrayList<>();
        HashMap<String,String> profil = ControleurBdd.getInstance(this).selection("SELECT Pas, Stock FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0);
        int pas = Integer.parseInt(profil.get("Pas"));
        int stock = Integer.parseInt(profil.get("Stock"));
        boolean verifAchat, dispo, activable;
        int rep;
        for(HashMap<String,String> map : selection){
            try{
                rep = Integer.parseInt(ControleurBdd.getInstance(this).selection("SELECT Disponible FROM rabaisprofil WHERE IDprofil="+LoginActivity.IDuser+" AND IDRabais="+map.get("ID"), ControleurBdd.BASE.INTERNE).get(0).get("Disponible"));
                verifAchat=(rep==0);
            }catch(Exception e){
                verifAchat=true;
            }
            /*try{
                rep = Integer.parseInt(ControleurBdd.getInstance(this).selection("SELECT Disponible FROM rabaisprofil WHERE IDprofil="+LoginActivity.IDuser+" AND IDRabais="+map.get("ID"), ControleurBdd.BASE.INTERNE).get(0).get("Disponible"));
                activable=(rep!=2);
            }catch(Exception e){
                activable=false;
            }*/
            dispo = (Integer.parseInt(map.get("Cout"))<pas+stock) && verifAchat;
            listR.add(new Rabais(Integer.parseInt(map.get("ID")),map.get("Image"),map.get("Nom"),Integer.parseInt(map.get("Cout")),map.get("Description"),dispo, false));
        }
        return listR;
    }

    public void onClickNom(Rabais item, int position) {
        ListView list = (ListView)findViewById(R.id.ListView01);
        TextView desc = (TextView)list.findViewWithTag(position).findViewById(R.id.description);
        if(desc.getVisibility()==View.GONE && !item.description.equals("")){
            desc.setVisibility(View.VISIBLE);
        }else {
            desc.setVisibility(View.GONE);
        }
    }

    public void onClickAchat(Rabais item, int position) {
        ListView list = (ListView)findViewById(R.id.ListView01);
        ImageView selected = (ImageView)list.findViewWithTag(position).findViewById(R.id.imageAchat);
        Toast.makeText(this,"Achat en cours ...",Toast.LENGTH_SHORT).show();
        if(selected.getAlpha() == 1){
            ControleurBdd control = ControleurBdd.getInstance(this);
            control.syncProfil();
            HashMap<String,String> map = control.selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE).get(0);
            int pas = Integer.parseInt(map.get("Pas"));
            pas -= item.prix;
            control.execute("UPDATE profil SET Pas="+pas+" WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE);

            //mise à jour de la table histachat
            control.execute("INSERT INTO histachat (Utilisateur,Date,Rabais) VALUES ("+LoginActivity.IDuser+","+(new Date()).getTime()+","+item.ID+")", ControleurBdd.BASE.EXTERNE);

            // mise à jour de la table rabaisprofil
            try{
                int idjoin=Integer.parseInt(control.selection("SELECT ID WHERE IDProfil="+LoginActivity.IDuser+", IDRabais="+item.ID, ControleurBdd.BASE.EXTERNE).get(0).get("ID"));
                control.execute("UPDATE rabaisprofil SET Disponible=1 WHERE ID="+idjoin, ControleurBdd.BASE.EXTERNE);
            }catch(Exception e){
                control.execute("INSERT INTO rabaisprofil (IDProfil,IDRabais,Disponible) VALUES ("+LoginActivity.IDuser+","+item.ID+",1)", ControleurBdd.BASE.EXTERNE);
            }
            control.synchronize();
            selected.setAlpha(0.1f);
            ((TextView)list.findViewWithTag(position).findViewById(R.id.activation)).setAlpha(1f);
            Toast.makeText(this,item.titre+" acheté !",Toast.LENGTH_SHORT).show();
            EcranPrincipal.UpdatePas(pas);
        }else{
            Toast.makeText(this,"Ce rabais n\'est pas disponible !",Toast.LENGTH_SHORT).show();
        }
    }

/*    public void onClickActivate(Rabais item, int position){
        ListView list = (ListView)findViewById(R.id.ListView01);
        TextView selected = (TextView)list.findViewWithTag(position).findViewById(R.id.activation);
        Toast.makeText(this,"Activation en cours ...",Toast.LENGTH_SHORT).show();
        if(selected.getAlpha() == 1){
            item.active = true;
            ControleurBdd control = ControleurBdd.getInstance(this);
            item.startTimer();
            // mise à jour de la table rabaisprofil
            try{
                int idjoin=Integer.parseInt(control.selection("SELECT ID WHERE IDProfil="+LoginActivity.IDuser+", IDRabais="+item.ID, ControleurBdd.BASE.EXTERNE).get(0).get("ID"));
                control.execute("UPDATE rabaisprofil SET Disponible=2 WHERE ID="+idjoin, ControleurBdd.BASE.EXTERNE);
            }catch(Exception e){
                Toast.makeText(this, "Erreur d'activation",Toast.LENGTH_SHORT).show();
            }
            control.synchronize();
            selected.setAlpha(0.1f);
            Toast.makeText(this,item.titre+" acheté pour" + LoginActivity.IDuser,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Ce rabais n\'est pas disponible !",Toast.LENGTH_SHORT).show();
        }
    }*/
}