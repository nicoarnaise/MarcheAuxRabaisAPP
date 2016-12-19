package com.geasser.marcheauxrabais;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;


public class ListeEntreprises extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_entreprises);

        ArrayList<String> Names = trierEntreprisesNom();

        mListView = (ListView) findViewById(R.id.listView);

        //android.R.layout.simple_list_item_1 est une vue disponible de base dans le SDK android,
        //Contenant une TextView avec comme identifiant "@android:id/text1"

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Names);
        mListView.setAdapter(adapter);
    }

    // Afficher la liste des entreprises disponibles dans la Bdd en les triant par nom. Utilisation
    // du contrôleur pour sélectionner depuis la base interne.
    //TODO : réadapter pour les utilisations finales. Test de démarrage ok, mais il faut pouvoir afficher
    public ArrayList<String> trierEntreprisesNom() {

        ArrayList<String> Names = new ArrayList<String>();
        ControleurBdd control = ControleurBdd.getInstance(this);
        ArrayList<HashMap<String, String>> tab = control.selection("SELECT Nom FROM entreprises ORDER BY Nom ASC",ControleurBdd.BASE.EXTERNE);
        if(tab==null){
            // Si la Bdd externe n'est pas disponible (pas de connexion, ...), on utilise la bdd interne
            tab = control.selection("SELECT Nom FROM entreprises ORDER BY Nom ASC", ControleurBdd.BASE.INTERNE);
        }
        for(HashMap<String,String> map : tab){
            Names.add(map.get("Nom"));
        }

        return Names;
    }
}
