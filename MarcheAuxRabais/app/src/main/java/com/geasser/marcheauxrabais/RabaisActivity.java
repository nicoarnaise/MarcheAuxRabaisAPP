package com.geasser.marcheauxrabais;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class RabaisActivity extends AppCompatActivity implements RabaisAdapter.RabaisAdapterListener {

    protected ListView list;
    protected Rabais item;
    protected TextView selected;
    protected int pas;
    protected ArrayList<Rabais> listR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mes Rabais");
        setContentView(R.layout.activity_rabais);
            if (savedInstanceState == null) {
                ControleurBdd control = ControleurBdd.getInstance(getApplicationContext());
                if(ControleurBdd.isOnline()) {
                    control.syncEntreprise();
                    control.syncRabais();
                    ControleurBdd.getInstance(this).syncRabaisProfil();
                }
                listR = toRabais(ControleurBdd.getInstance(this).selection("SELECT r.ID, s.Image, r.Nom, r.Cout, r.Description, e.Nom entreprise FROM rabais r, secteurs s, entreprises e WHERE s.ID=r.Secteur AND e.ID=r.Entreprise", ControleurBdd.BASE.INTERNE));
                //Création et initialisation de l'Adapter pour les rabais
            } else {
                listR = (ArrayList<Rabais>) savedInstanceState.get("LISTR");
            }
            RabaisAdapter adapter = new RabaisAdapter(this, listR);

            //Ecoute des évènements sur votre liste
            adapter.addListener(this);

            //Récupération du composant ListView
            list = (ListView) findViewById(R.id.ListView01);

            //Initialisation de la liste avec les données
            list.setAdapter(adapter);
    }

    @Override
    public void onResume(){
        if(ControleurBdd.isOnline()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ControleurBdd control = ControleurBdd.getInstance(getApplicationContext());
                    control.syncEntreprise();
                    control.syncRabais();
                    ControleurBdd.getInstance(getApplicationContext()).syncRabaisProfil();
                }
            }).start();
        }
        super.onResume();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("LISTR",listR);
        super.onSaveInstanceState(savedInstanceState);
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
                activable=(rep==1);
            }catch(Exception e){
                verifAchat=true;
                activable=false;
            }
            dispo = (Integer.parseInt(map.get("Cout"))<pas+stock) && verifAchat;
            listR.add(new Rabais(Integer.parseInt(map.get("ID")),map.get("Image"),map.get("Nom"),Integer.parseInt(map.get("Cout")),map.get("Description"),dispo, activable,map.get("entreprise")));
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

    public void onClickAchat(Rabais sel, final int position) {
        if(ControleurBdd.isOnline()) {
            item = sel;
            final ImageView selected = (ImageView)list.findViewWithTag(position).findViewById(R.id.imageAchat);
            Toast.makeText(this,"Achat en cours ...",Toast.LENGTH_SHORT).show();
            list.setEnabled(false);
            if(selected.getAlpha() == 1 && listR.get(position).disponible){
                new Thread(new Runnable() {
                public void run() {
                    ControleurBdd control = ControleurBdd.getInstance(getApplicationContext());
                    control.syncProfil();
                    HashMap<String,String> map = control.selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE).get(0);
                    int pas = Integer.parseInt(map.get("Pas"));
                    pas -= item.prix;
                    control.execute("UPDATE profil SET Pas="+pas+" WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE);

                    //---- maj affichage ecran principal ----
                    Intent intent1 = new Intent();
                    intent1.setAction("MY_ACTION");
                    intent1.putExtra("DATAPASSED", pas);
                    sendBroadcast(intent1);
                    //--------------------------------------

                    //mise à jour de la table histachat
                    long date = new Date().getTime();
                    control.execute("INSERT INTO histachat (Utilisateur,Date,Rabais) VALUES ("+LoginActivity.IDuser+","+date+","+item.ID+")", ControleurBdd.BASE.EXTERNE);
                    control.execute("INSERT INTO histachat (Utilisateur,Date,Rabais) VALUES ("+LoginActivity.IDuser+","+date+","+item.ID+")", ControleurBdd.BASE.INTERNE);

                    // mise à jour de la table rabaisprofil

                    ArrayList<HashMap<String,String>> ret = control.selection("SELECT ID FROM rabaisprofil WHERE IDProfil="+LoginActivity.IDuser+" AND IDRabais="+item.ID, ControleurBdd.BASE.EXTERNE);
                    if(ret!=null){
                        int idjoin = Integer.parseInt(ret.get(0).get("ID"));
                        control.execute("UPDATE rabaisprofil SET Disponible=2 WHERE ID="+idjoin, ControleurBdd.BASE.EXTERNE);
                    }else{
                        control.execute("INSERT INTO rabaisprofil (IDProfil,IDRabais,Disponible) VALUES ("+LoginActivity.IDuser+","+item.ID+",2)", ControleurBdd.BASE.EXTERNE);
                    }
                    ret = control.selection("SELECT ID FROM rabaisprofil WHERE IDProfil="+LoginActivity.IDuser+" AND IDRabais="+item.ID, ControleurBdd.BASE.INTERNE);
                    if(ret!=null){
                        int idjoin = Integer.parseInt(ret.get(0).get("ID"));
                        control.execute("UPDATE rabaisprofil SET Disponible=1 WHERE ID="+idjoin, ControleurBdd.BASE.INTERNE);
                    }else{
                        control.execute("INSERT INTO rabaisprofil (IDProfil,IDRabais,Disponible) VALUES ("+LoginActivity.IDuser+","+item.ID+",1)", ControleurBdd.BASE.INTERNE);
                    }
                    control.syncProfil();
                    control.syncRabaisProfil();
                        }
                }).start();
                listR.get(position).disponible = false;
                selected.setAlpha(0.1f);
                listR.get(position).active = true;
                list.findViewWithTag(position).findViewById(R.id.activation).setAlpha(1f);
                list.findViewWithTag(position).findViewById(R.id.activation).setBackgroundResource(R.drawable.myprogressbar);
                Toast.makeText(this,item.titre+" acheté !",Toast.LENGTH_SHORT).show();
                list.setEnabled(true);
            }else {
                list.setEnabled(true);
                Toast.makeText(this, "Ce rabais n\'est pas disponible !", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"Vous devez être connecté à internet pour utiliser cette fonctionnalité",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onClickActivate(Rabais sel, final int position){
        item = sel;
        selected = (TextView)list.findViewWithTag(position).findViewById(R.id.activation);
        Toast.makeText(this,"Activation en cours ...",Toast.LENGTH_SHORT).show();
        if(selected.getAlpha() == 1 && listR.get(position).active){
            listR.get(position).active = false;
            new Thread(new Runnable() {
                public void run() {
                    ControleurBdd control = ControleurBdd.getInstance(getApplicationContext());
                    try {
                        int idjoin = Integer.parseInt(control.selection("SELECT ID FROM rabaisprofil WHERE IDProfil=" + LoginActivity.IDuser + " AND IDRabais=" + item.ID, ControleurBdd.BASE.INTERNE).get(0).get("ID"));
                        control.execute("UPDATE rabaisprofil SET Disponible=3 WHERE ID=" + idjoin, ControleurBdd.BASE.INTERNE);
                        Intent intent = new Intent(getApplicationContext(),RabaisActive.class);
                        intent.putExtra("ID",item.ID);
                        startActivity(intent);
                        if(ControleurBdd.isOnline()){
                            control.syncRabaisProfil();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Erreur d'activation", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
            boolean test = Integer.parseInt(ControleurBdd.getInstance(this).selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0).get("Pas")) >= item.prix;
            if(test){
                listR.get(position).disponible=true;
                list.findViewWithTag(position).findViewById(R.id.imageAchat).setAlpha(1f);
            }
            selected.setAlpha(0.1f);
            selected.setBackground(null);
        }else{
            Toast.makeText(this,"Ce rabais n\'est pas disponible !",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onStop() {
        EcranPrincipal.notificationManager.cancelAll();
        super.onStop();
    }
}

