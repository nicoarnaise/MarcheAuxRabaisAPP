package com.geasser.marcheauxrabais;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class ChallengeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private  ArrayList<HashMap<String,String>> mDataSource;
    ArrayList<HashMap<String,String>> tab;
    public ChallengeAdapter(Context context,  ArrayList<HashMap<String,String>> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        String IDProfil = String.valueOf(LoginActivity.IDuser);
        tab = ControleurBdd.getInstance(mContext).selection("SELECT IDSucces FROM succesprofil WHERE IDProfil="+LoginActivity.IDuser, ControleurBdd.BASE.EXTERNE);
    }

    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.activity_challenge_adapter, parent, false);

// Get title element
        TextView titleTextView =
                (TextView) rowView.findViewById(R.id.firstLine);
// Get subtitle element
        TextView subtitleTextView =
                (TextView) rowView.findViewById(R.id.secondLine);
// Get thumbnail element
        ImageView thumbnailImageView =
                (ImageView) rowView.findViewById(R.id.icon);

        titleTextView.setText(mDataSource.get(position).get("Nom").toString());
        subtitleTextView.setText(mDataSource.get(position).get("Description").toString());
        thumbnailImageView.setImageURI(Uri.parse("android.resource://"+mDataSource.get(position).get("Image")));

        int i =0;
        while (i<tab.size()) {
            if (tab.get(i).get("IDSucces").compareTo(String.valueOf(position+1)) == 0) {
                rowView.setBackgroundColor(Color.parseColor("#4066FFCC"));
                i=-1;
                break;
            }
            i++;
        }

        if(i!=-1)
            rowView.setAlpha(0.3f);

        return rowView;
    }
}
