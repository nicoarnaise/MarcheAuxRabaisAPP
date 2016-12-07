package com.geasser.marcheauxrabais;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 17/11/2016.
 */

public class RabaisAdapter extends BaseAdapter implements Serializable{

    // Une liste de personnes
    private List<Rabais> mListR;

    //Le contexte dans lequel est présent notre adapter
    private Context mContext;

    //Un mécanisme pour gérer l'affichage graphique depuis un layout XML
    private LayoutInflater mInflater;

    public RabaisAdapter(Context context, List<Rabais> aListR) {
        mContext = context;
        mListR = aListR;
        mInflater = LayoutInflater.from(mContext);
    }

    public int getCount() {
        return mListR.size();
    }

    public Object getItem(int position) {
        return mListR.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layoutItem;
        //(1) : Réutilisation des layouts
        if (convertView == null) {
            //Initialisation de notre item à partir du  layout XML "rabais_layout.xml"
            layoutItem = (LinearLayout) mInflater.inflate(R.layout.rabais_layout, parent, false);
        } else {
            layoutItem = (LinearLayout) convertView;
        }
        layoutItem.setMinimumWidth(parent.getWidth());

        //(2) : Récupération des Views a modifier de notre layout
        TextView tv_Titre = (TextView)layoutItem.findViewById(R.id.titreRabais);
        TextView tv_Activation = (TextView)layoutItem.findViewById(R.id.activation);
        TextView tv_Prix = (TextView)layoutItem.findViewById(R.id.textePrix);
        TextView tv_Description = (TextView)layoutItem.findViewById(R.id.description);
        ImageView iv_Image = (ImageView)layoutItem.findViewById(R.id.imageRabais);
        ImageView iv_Caddie = (ImageView)layoutItem.findViewById(R.id.imageAchat);

        //(3) : Renseignement des valeurs
        tv_Titre.setText(mListR.get(position).titre);
        tv_Activation.setText("Activer le rabais");
        tv_Activation.setAlpha(mListR.get(position).active?1f:0.1f);
        tv_Prix.setText(""+mListR.get(position).prix);
        tv_Description.setText(mListR.get(position).description);
        iv_Image.setImageResource(R.mipmap.ic_launcher);
        iv_Caddie.setAlpha(mListR.get(position).disponible?1f:0.1f);


        //On mémorise la position de la "Personne" dans le composant textview
        layoutItem.setTag(position);
        tv_Titre.setTag(position);
        tv_Activation.setTag(position);
        tv_Description.setTag(position);
        iv_Image.setTag(position);
        iv_Caddie.setTag(position);

        //On ajoute un listener
        tv_Titre.setOnClickListener(new MyOnClickListener());
        tv_Activation.setOnClickListener(new ActivateClickListener());
        tv_Description.setOnClickListener(new MyOnClickListener());
        iv_Image.setOnClickListener(new MyOnClickListener());
        iv_Caddie.setOnClickListener(new AchatClickListener());

        //On retourne l'item créé.
        return layoutItem;
    }

    //Contient la liste des listeners
    private ArrayList<RabaisAdapterListener> mListListener = new ArrayList<>();
    /**
     * Pour ajouter un listener sur notre adapter
     */
    public void addListener(RabaisAdapterListener aListener) {
        mListListener.add(aListener);
    }

    private void sendListener(Rabais item, int position) {
        for(int i = mListListener.size()-1; i >= 0; i--) {
            mListListener.get(i).onClickNom(item, position);
        }
    }

    private void sendAchat(Rabais item, int position) {
        for(int i = mListListener.size()-1; i >= 0; i--) {
            mListListener.get(i).onClickAchat(item, position);
        }
    }

    private void sendActivate(Rabais item, int position){
        for(int i = mListListener.size()-1; i>=0; i--){
            mListListener.get(i).onClickActivate(item, position);
        }
    }

    /**
     * Interface pour écouter les évènements sur les éléments du Rabais
     */
    public interface RabaisAdapterListener {
        public void onClickNom(Rabais item, int position);
        public void onClickAchat(Rabais item, int position);
        public void onClickActivate(Rabais item, int position);
    }

    /**
     * Définition du OnClickListener
     */
    public class MyOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //Lorsque l'on clique sur le nom, on récupère la position du Rabais
            Integer position = (Integer)v.getTag();

            //On prévient les listeners qu'il y a eu un clic.
            sendListener(mListR.get(position), position);
        }
    }

    public class AchatClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //Lorsque l'on clique sur le nom, on récupère la position du Rabais
            Integer position = (Integer)v.getTag();

            //On prévient les listeners qu'il y a eu un clic.
            sendAchat(mListR.get(position), position);
        }
    }

    public class ActivateClickListener implements  View.OnClickListener{
        @Override
        public void onClick(View v) {
            //Lorsque l'on clique sur le nom, on récupère la position du Rabais
            Integer position = (Integer)v.getTag();

            //On prévient les listeners qu'il y a eu un clic.
            sendActivate(mListR.get(position), position);
        }
    }
}
