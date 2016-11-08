package com.geasser.marcheauxrabais;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ListeEntreprises extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_entreprises);
        /*AsyncTask<String, Void, String> task = new BddExt().execute("INSERT INTO entreprises (Nom,Adresse,Logo,Secteur) VALUES ('Le Petit Chef', '10 Rue des Spaghettis', NULL, 2)");
        String rep = "bleh";
        try {
            rep = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/
        ArrayList<String> Names = trierEntreprisesNom();
        //Names.add(rep);

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
    //TODO : la liste en hors connexion une fois les bdd synchronisées.
    public ArrayList<String> trierEntreprisesNom() {

        ArrayList<String> Names = new ArrayList<String>();
        ControleurBdd control = ControleurBdd.getInstance(this);
        ArrayList<HashMap<String, String>> tab = null;

        try {
            // On utilise la Bdd externe quand on le peut
            AsyncTask<String, Void, String> task = new BddExt().execute("SELECT Nom From entreprises ORDER BY Nom ASC");
            String rep = task.get();
            tab = BddExt.formate(rep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(tab==null){
            // Si la Bdd externe n'est pas disponible (pas de connexion, ...), on utilise la bdd interne
            tab = control.selection("SELECT Nom From entreprises ORDER BY Nom ASC");
            // Ne fonctionne pas pour l'instant car relancer l'appli <=> recréer la Bdd Interne. Sans
            // connexion, elle est vide.
        }
        for(HashMap<String,String> map : tab){
            Names.add(map.get("Nom"));
        }

        return Names;
    }
}
