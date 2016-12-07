package com.geasser.marcheauxrabais;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ChallengeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private  ArrayList<HashMap<String,String>> mDataSource;

    public ChallengeAdapter(Context context,  ArrayList<HashMap<String,String>> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        //    "SELECT Nom, Description, Recompense, Image FROM succes", ControleurBdd.BASE.EXTERNE);

        titleTextView.setText(mDataSource.get(position).get("Nom").toString());
        subtitleTextView.setText(mDataSource.get(position).get("Description").toString());
        thumbnailImageView.setImageURI(Uri.parse("android.resource://"+mDataSource.get(position).get("Image")));
        return rowView;
    }
}
