package com.geasser.marcheauxrabais;

import android.os.AsyncTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicolas & Vincent on 27/10/2016.
 *
 * Cette classe permet la connection et l'envoie d'une requette à la base de données externe de manière asynchrone.
 *
 * <U>use :</U>
 * 1/ AsyncTask<String, Void, String> task = new BddExt().execute(String requette); // pour envoyer la requette à la base
 * 2/ task.get() // pour récupérer le String de la réponse de la base.
 */

public class BddExt extends AsyncTask<String, Void, String> {

    /**
     * Cette fonction est exécutée par la fonction new BddExt().execute(String requette)
     * Elle se connecte à la base et récupère la réponse à la requette demandée.
     * @param req
     * @return String se la réponse à la requette
     */
    @Override
    protected String doInBackground(String... req) {
        InputStream is = null;
        OutputStream os = null;
        try {
            // on se connecte à la page PHP qui fait le lien avec la base de donnée externe
            final HttpURLConnection conn = (HttpURLConnection) new URL("http://n.arnaise.free.fr/mar/request.php").openConnection();
            // on indique qu'on va utiliser la méthode POST (méthode de passage de variables Web)
            conn.setRequestMethod("POST");
            // on met en forme l'envoie de la requette pour qu'elle soit comprise par la page PHP
            Map<String,String> params = new LinkedHashMap<>();
            params.put("query",req[0]);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            //on prévoit d'envoyer des données à la page et d'en recevoir
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // on envoie ici la requette mise en forme à la page PHP
            os = conn.getOutputStream();
            os.write(postDataBytes);// = conn.connect (methode GET) mais avec la methode POST
            // on récupère la réponse de la page qui correspond à la mise en forme JQuerry de la réponse de la base de donnée externe
            is = conn.getInputStream();
            // On la retourne pour pouvoir s'en servir dans l'application.
            return readIt(is);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // On s'assure que les flux d'entrées et sorties sont bien fermés après l'exécution,
            // quelque soit le comportement de l'application
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Cette fonction permet la mise en forme de la réponse, transformant le contenu du flux d'entrée en String
     * @param is le flux d'entrée
     * @return .toString du contenu dans le flux de sortie
     * @throws IOException
     */
    private static String readIt(InputStream is) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            response.append(line).append('\n');
        }
        return response.toString();
    }
}
