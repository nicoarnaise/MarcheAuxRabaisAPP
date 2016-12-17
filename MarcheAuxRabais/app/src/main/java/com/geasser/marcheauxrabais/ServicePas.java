package com.geasser.marcheauxrabais;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by GeaSSer on 2016-12-15.
 */

public class ServicePas extends Service implements SensorEventListener {

    final static String MY_ACTION = "MY_ACTION";
    protected SensorManager mSensorManager;
    protected Sensor mStepCounterSensor;
    protected int i =0;

    private ControleurBdd control;
    static int nbPas = 0;
    static int pasSupp=0;

    protected int previousValue=0;



    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate(){
        //MyThread myThread = new MyThread();
        // myThread.start();

        // Relatif au comptage des pas
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);


        control = ControleurBdd.getInstance(this);

        // Synchronisation de la table profil des BDD interne et externe
        control.syncProfil();
        // On remet pasSupp à 0 car la synchronisation a été faite.
        pasSupp=0;
        ArrayList<HashMap<String, String>> tab = control.selection("SELECT Pas FROM profil WHERE UserName='"+LoginActivity.pseudo+"';",ControleurBdd.BASE.INTERNE);

        if(tab!=null)
            nbPas = Integer.parseInt(tab.get(0).get("Pas"));

        Intent intent1 = new Intent();
        intent1.setAction(MY_ACTION);
        intent1.putExtra("DATAPASSED", pasSupp+nbPas);
        sendBroadcast(intent1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub


      return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (previousValue==0)
            previousValue  = (int) event.values[0];

        pasSupp=(int)event.values[0]-previousValue;
        Log.i("Yollo",""+pasSupp);

        control.execute("UPDATE profil SET Stock='" + pasSupp + "' WHERE UserName='" + LoginActivity.pseudo + "';", ControleurBdd.BASE.INTERNE);

        Intent intent2 = new Intent();
        intent2.setAction(MY_ACTION);
        intent2.putExtra("DATAPASSED", pasSupp+nbPas);
        sendBroadcast(intent2);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class MyThread extends Thread{

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for(int i=0; i<10; i++){
                try {
                    Thread.sleep(5000);
                    Intent intent = new Intent();
                    intent.setAction(MY_ACTION);

                    intent.putExtra("DATAPASSED", i);
                    i++;

                    sendBroadcast(intent);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            stopSelf();
        }

    }

}