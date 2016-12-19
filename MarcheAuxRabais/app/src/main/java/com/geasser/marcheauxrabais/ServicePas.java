package com.geasser.marcheauxrabais;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by GeaSSer on 2016-12-15.
 */

public class ServicePas extends Service implements SensorEventListener {

    final static String MY_ACTION = "MY_ACTION";
    final static String MY_PONEY = "MY_PONEY";
    protected SensorManager mSensorManager;
    protected Sensor mStepCounterSensor;
    protected int i =0;

    private ControleurBdd control;
    static int nbPas = 0;
    static int pasSupp=0;

    protected int previousValue=0;
    protected int iThread=0;

    protected int nbRabaisInitial;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate(){
        // Relatif au comptage des pas
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor,SensorManager.SENSOR_DELAY_FASTEST);

        control = ControleurBdd.getInstance(this);

//        MyThreadVerification myThreadVerification= new MyThreadVerification();
//        myThreadVerification.start();

        if(ControleurBdd.isOnline()) {
            // Synchronisation de la table profil des BDD interne et externe
            control.syncProfil();
            // On remet pasSupp à 0 car la synchronisation a été faite.
            pasSupp = 0;
        }else{

            pasSupp = Integer.parseInt(control.selection("SELECT Stock FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0).get("Stock"));
            nbPas = Integer.parseInt(control.selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0).get("Pas"));
        }
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

    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int pasVerifBdd = Integer.parseInt(control.selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0).get("Pas"));

        if(pasVerifBdd != nbPas){
            previousValue = 0;
            pasSupp=0;
            nbPas = Integer.parseInt(control.selection("SELECT Pas FROM profil WHERE ID="+LoginActivity.IDuser, ControleurBdd.BASE.INTERNE).get(0).get("Pas"));
        }

        if (previousValue==0 && pasSupp==0) {
            previousValue = (int) event.values[0];
        }else if(previousValue==0 && pasSupp!=0){
            previousValue = (int) event.values[0]-pasSupp;
        }

        pasSupp=(int)event.values[0]-previousValue;

        control.execute("UPDATE profil SET Stock='" + pasSupp + "' WHERE UserName='" + LoginActivity.pseudo + "';", ControleurBdd.BASE.INTERNE);

        Intent intent2 = new Intent();
        intent2.setAction(MY_ACTION);
        intent2.putExtra("DATAPASSED", pasSupp+nbPas);
        sendBroadcast(intent2);

        if (iThread==10){
            MyThread myThread = new MyThread();
            myThread.start();
            iThread=0;
        }

        iThread++;



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class MyThread extends Thread{

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Intent intent3 = new Intent();
            intent3.setAction(MY_PONEY);
            intent3.putExtra("CHOCOLAT", nbRabaisInitial);
            sendBroadcast(intent3);
            stopSelf();
        }

    }
//
//    public class MyThreadVerification extends Thread{
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            ArrayList<HashMap<String, String>> tab = control.selection("SELECT COUNT(Utilisateur) FROM histachat WHERE Utilisateur="+LoginActivity.IDuser+";",ControleurBdd.BASE.EXTERNE);
//            nbRabaisInitial=Integer.parseInt(tab.get(0).get("COUNT(Utilisateur)"));
//            stopSelf();
//        }
//
//    }

}