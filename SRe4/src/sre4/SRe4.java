/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sre4;

import Manejador.Manejador;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import op.Video;
import org.json.simple.parser.JSONParser;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author jlbel
 */
public class SRe4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, ParseException{
        Vector<Video> datos = new Vector();
        Scanner in = new Scanner(System.in);
        System.out.println("ingrese un IdUsuario: ");
        int idU = in.nextInt(); // Reading from System.in
        
        Manejador man = new Manejador();                
        datos = man.filtroContenido(man.obtenerCompetencias(idU));
        
        for (int i = 0; i < datos.size(); i++) {
            man.insertarRecomendaciones(idU, datos.get(i).getId());
        }
        
    }

}
