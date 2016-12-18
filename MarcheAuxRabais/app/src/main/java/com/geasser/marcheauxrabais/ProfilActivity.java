package com.geasser.marcheauxrabais;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProfilActivity extends AppCompatActivity {

    ControleurBdd control;
    Date premierJour;
    DateFormat formatbdd = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat formatgraph = new SimpleDateFormat("dd/MM");
    TextView pas;
    TextView niveau;
    TextView pseudo;
    Integer Pas;
    Integer Niveau;
    Integer Exp;
    MyBarData lineData;
    String PAS = "Pas";
    String NIVEAU = "Niveau";
    String EXP = "Exp";
    String PREMIERJOUR = "premierJour";
    String LINEDATA = "lineData";

    private ProgressBar mProgress;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        setTitle("Mon profil");

        control = ControleurBdd.getInstance(this);

        // Profil Activity parametrage

        pseudo = (TextView) findViewById(R.id.pseudo);
        pseudo.setText(LoginActivity.pseudo);
        pseudo.setTypeface(Typeface.DEFAULT_BOLD);
        pseudo.setTextColor(Color.BLUE);

        pas = (TextView) findViewById(R.id.pas);
        niveau = (TextView) findViewById(R.id.niveau);


        updateFromBundle(savedInstanceState);

        // Affichage du nombre de pas actuel de l'user
        pas.setText(Pas.toString());
        pas.setTypeface(Typeface.DEFAULT_BOLD);

        // Affhichage du niveau de l'user
        niveau.setText(""+Niveau);
        niveau.setTypeface(Typeface.DEFAULT_BOLD);

        // Affichage de la progression dans le niveau
        TextView exp = (TextView) findViewById(R.id.exp);
        exp.setText(Exp + "/500");
        exp.setTypeface(Typeface.DEFAULT_BOLD);

        // Creation de la progressBar
        Handler progressBarHandler = new Handler();

        final ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setMax(500);

        progressBarHandler .post(new Runnable() {

            public void run() {
                bar.setProgress(Exp);
            }
        });

        // Statistiques totales : pas, distance, calories dépensées
        TextView pastot = (TextView) findViewById(R.id.pastot);
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView calories = (TextView) findViewById(R.id.calories);

        Integer tot = 0;
        Integer Tot = tot + Pas;
        Double dist = Tot * 0.75;
        Double calorie = Tot * 0.5;
        pastot.setText(Tot.toString());
        pastot.setTypeface(Typeface.DEFAULT_BOLD);
        distance.setText((dist/1000) + " km");
        distance.setTypeface(Typeface.DEFAULT_BOLD);
        calories.setText((calorie/1000) + " kcal");
        calories.setTypeface(Typeface.DEFAULT_BOLD);

        // statistique graphe parametrage
        BarChart chart = (BarChart) findViewById(R.id.chart);
        if (lineData != null) {
            chart.setData(lineData);
        }
        chart.setNoDataText("La connexion à internet est requise pour afficher le graphe.");

        chart.getDescription().setEnabled(false);

        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setTextSize(15);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setSpaceBottom(0);

        chart.invalidate(); // refresh
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(PAS, Pas);
        savedInstanceState.putInt(NIVEAU, Niveau);
        savedInstanceState.putInt(EXP, Exp);
        savedInstanceState.putSerializable(PREMIERJOUR, premierJour);
        savedInstanceState.putSerializable(LINEDATA, lineData);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Pas = savedInstanceState.getInt(PAS);
            Niveau = savedInstanceState.getInt(NIVEAU);
            Exp = savedInstanceState.getInt(EXP);
            premierJour = (Date) savedInstanceState.get(PREMIERJOUR);
            lineData = (MyBarData) savedInstanceState.get(LINEDATA);

        } else {
            // pas
            ArrayList<HashMap<String, String>> PasActuel = control.selection("SELECT Pas,Stock FROM profil WHERE ID = " + LoginActivity.IDuser, ControleurBdd.BASE.INTERNE);
            Pas = Integer.parseInt(PasActuel.get(0).get("Pas")) + Integer.parseInt(PasActuel.get(0).get("Stock"));

            // Affhichage du niveau de l'user
            ArrayList<HashMap<String, String>> niv = control.selection("SELECT Lvl FROM profil WHERE ID = " + LoginActivity.IDuser, ControleurBdd.BASE.INTERNE);
            Niveau = Integer.parseInt(niv.get(0).get("Lvl"));

            // Affichage de la progression dans le niveau
            ArrayList<HashMap<String, String>> exp = control.selection("SELECT Exp FROM profil WHERE ID = " + LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE);
            Exp = Integer.parseInt(exp.get(0).get("Exp"));

            // statistique graphe parametrage
            ArrayList<HashMap<String, String>> historique = ControleurBdd.getInstance(this).selection("SELECT Date,Pas FROM historique WHERE Utilisateur=" + LoginActivity.IDuser + " ORDER BY DATE", ControleurBdd.BASE.EXTERNE);
            if (historique != null) {
                ArrayList<Pas> pasData = new ArrayList<>();
                Date jour = null;
                for (HashMap<String, String> ligne : historique) {
                    try {
                        if (jour != null) {
                            for (int i = 1; i < DateUtil.daysBetween(jour, formatbdd.parse(ligne.get("Date"))); i++) {
                                pasData.add(new Pas(0, pasData.size()));
                            }
                        }
                        jour = formatbdd.parse(ligne.get("Date"));
                        if (historique.get(0) == ligne) {
                            premierJour = jour;
                        }
                        pasData.add(new Pas(Integer.parseInt(ligne.get("Pas")), DateUtil.daysBetween(premierJour, jour)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                Pas[] dataObjects = {};
                dataObjects = pasData.toArray(dataObjects);

                List<BarEntry> entries = new ArrayList<BarEntry>();

                for (Pas data : dataObjects) {
                    // turn your data into Entry objects
                    entries.add(new BarEntry(data.getValueX(), data.getValueY()));
                }

                BarDataSet dataSet = new BarDataSet(entries, "Pas"); // add entries to dataset
                dataSet.setColor(Color.rgb(33, 180, 115));
                dataSet.setValueTextColor(Color.BLUE);
                //dataSet.setValueTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                dataSet.setValueTextSize(10);

                lineData = new MyBarData(dataSet);
            } else {
                lineData = null;
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Profil Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public class Pas {
        private int nbPas;
        private int date;

        public Pas(int nbPas, int date) {
            this.nbPas = nbPas;
            this.date = date;
        }

        public int getValueX() {
            return date;
        }

        public int getValueY() {
            return nbPas;
        }
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return formatgraph.format(DateUtil.addDays(premierJour, (int) value));
        }
    }

    public static class DateUtil {
        public static Date addDays(Date date, int days) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, days); //minus number would decrement the days
            return cal.getTime();
        }

        public static int daysBetween(Date start, Date end) {
            Calendar day1 = Calendar.getInstance();
            Calendar day2 = Calendar.getInstance();
            day1.setTime(start);
            day2.setTime(end);

            return day2.get(Calendar.DAY_OF_YEAR) - day1.get(Calendar.DAY_OF_YEAR);
        }
    }

    public class MyBarData extends BarData implements Serializable {
        MyBarData(BarDataSet dataSet){
            super(dataSet);
        }

    }
}
