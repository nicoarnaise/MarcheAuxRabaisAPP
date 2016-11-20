package com.geasser.marcheauxrabais;

import java.util.ArrayList;

public class Rabais {

    public int ID;
    public String image;
    public String titre;
    public int prix;
    public String description;
    public boolean disponible;

    public Rabais(int ID, String image, String titre, int prix, String description, boolean disponible) {
        this.ID = ID;
        this.image = image;
        this.titre = titre;
        this.prix = prix;
        this.description = description;
        this.disponible = disponible;
    }

    /**
     * Initialise une liste de rabais
     * @return une liste de "Rabais"
     */

    public static ArrayList<Rabais> getAListOfRabais() {
        ArrayList<Rabais> listRab = new ArrayList<>();

        listRab.add(new Rabais(0,"", "Titre1", 100,"Ceci est la \ndescription n°1",true));
        listRab.add(new Rabais(1,"", "Titre2", 200,"Ceci est la \ndescription n°2",true));
        listRab.add(new Rabais(2,"", "Titre3", 300,"Ceci est la \ndescription n°3",true));
        listRab.add(new Rabais(3,"", "Titre4", 400,"Ceci est la \ndescription n°4",true));
        listRab.add(new Rabais(4,"", "Titre5", 500,"Ceci est la \ndescription n°5",true));
        listRab.add(new Rabais(5,"", "Titre6", 600,"Ceci est la \ndescription n°6",true));
        listRab.add(new Rabais(6,"", "Titre7", 700,"Ceci est la \ndescription n°7",true));
        listRab.add(new Rabais(7,"", "Titre8", 800,"Ceci est la \ndescription n°8",true));
        listRab.add(new Rabais(8,"", "Titre9", 900,"Ceci est la \ndescription n°9",true));
        listRab.add(new Rabais(9,"", "Titre10", 1000,"Ceci est la \ndescription n°10",true));


        return listRab;
    }
}
