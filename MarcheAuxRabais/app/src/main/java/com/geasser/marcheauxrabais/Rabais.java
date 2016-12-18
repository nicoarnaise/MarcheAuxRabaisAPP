package com.geasser.marcheauxrabais;

import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Rabais implements Serializable{

    public int ID;
    public String image;
    public String titre;
    public int prix;
    public String description;
    public boolean disponible;
    public boolean active;          // true si activé, false sinon

    /*public Timer temps;
    public int timeLeft = 100000;
    */

    public Rabais(int ID, String image, String titre, int prix, String description, boolean disponible, boolean active) {
        this.ID = ID;
        this.image = image;
        this.titre = titre;
        this.prix = prix;
        this.description = description;
        this.disponible = disponible;
        this.active = active;
    }

    /**
     * Initialise une liste de rabais
     * @return une liste de "Rabais"
     */

    public static ArrayList<Rabais> getAListOfRabais() {
        ArrayList<Rabais> listRab = new ArrayList<>();

        listRab.add(new Rabais(0,"", "Titre1", 100,"Ceci est la \ndescription n°1",true,true));
        listRab.add(new Rabais(1,"", "Titre2", 200,"Ceci est la \ndescription n°2",true,true));
        listRab.add(new Rabais(2,"", "Titre3", 300,"Ceci est la \ndescription n°3",true,true));
        listRab.add(new Rabais(3,"", "Titre4", 400,"Ceci est la \ndescription n°4",true,true));
        listRab.add(new Rabais(4,"", "Titre5", 500,"Ceci est la \ndescription n°5",true,true));
        listRab.add(new Rabais(5,"", "Titre6", 600,"Ceci est la \ndescription n°6",true,true));
        listRab.add(new Rabais(6,"", "Titre7", 700,"Ceci est la \ndescription n°7",true,true));
        listRab.add(new Rabais(7,"", "Titre8", 800,"Ceci est la \ndescription n°8",true,true));
        listRab.add(new Rabais(8,"", "Titre9", 900,"Ceci est la \ndescription n°9",true,true));
        listRab.add(new Rabais(9,"", "Titre10", 1000,"Ceci est la \ndescription n°10",true,true));


        return listRab;
    }
/*
    public void startTimer(){
        temps = new Timer();
        temps.schedule(new TimerTask(){
            @Override
            public void run(){
                timeLeft--;
                if (timeLeft==0){
                    active = false;
                    Thread.currentThread().stop();
                }
            }
        }, 0, 1000);
    }

    public int getTimeLeft(){
        return timeLeft;
    }*/
}
