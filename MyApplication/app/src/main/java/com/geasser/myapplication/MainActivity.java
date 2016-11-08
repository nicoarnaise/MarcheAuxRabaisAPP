package com.geasser.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView lvSex;
    private ListView lvLangage;
    private Button button;
    private List<String> listSex= new ArrayList<String>();
    private List<String> listLangage= new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvSex = (ListView)findViewById(R.id.lvSex);
        lvLangage = (ListView)findViewById(R.id.lvLangage);
        button = (Button)findViewById(R.id.button);

        listSex.add("Masculin");
        listSex.add("Féminin");

        listLangage.add("C");
        listLangage.add("Java");
        listLangage.add("COBOL");
        listLangage.add("Perl");

        button.setOnClickListener(this);

        // android.R.layout.simple_list_item_single_choice est ce qui fait apparaitre les cercles à sélectionner.
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, listSex);

        ArrayAdapter<String> adapterLangage = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, listLangage);

        // Attention à l'ordre !
        lvSex.setAdapter(adapterSex);

        // Rend les cercles cliquables.
        lvSex.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvSex.setItemChecked(0,true);

        lvLangage.setAdapter(adapterLangage);
        lvLangage.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvLangage.setItemChecked(1,true);


    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Votre message a bien été envoyé", Toast.LENGTH_LONG).show();

        //On déclare qu'on ne peut plus sélectionner d'élément
        lvSex.setChoiceMode(ListView.CHOICE_MODE_NONE);
        //On affiche un layout qui ne permet pas de sélection
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listSex);
        lvSex.setAdapter(adapterSex);

        //On déclare qu'on ne peut plus sélectionner d'élément
       lvLangage.setChoiceMode(ListView.CHOICE_MODE_NONE);
        //On affiche un layout qui ne permet pas de sélection
        ArrayAdapter<String> adapterLangage = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listLangage);
        lvLangage.setAdapter(adapterLangage);

        //On désactive le bouton
        button.setEnabled(false);

    }
}
