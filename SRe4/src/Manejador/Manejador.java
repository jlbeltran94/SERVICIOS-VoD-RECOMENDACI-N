/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Manejador;

import com.mongodb.BasicDBList;
import op.Video;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 *
 * @author jlbel
 */
public class Manejador {

    public DB getConexion() throws UnknownHostException {
        MongoClient MongoClient = new MongoClient("localhost", 27017);
        DB db = MongoClient.getDB("video");
        return db;
    }

    public DBCollection obtenerColeccion(String collection) throws UnknownHostException {
        DBCollection coll = getConexion().getCollection(collection);
        return coll;
    }

    public int numerodocumentos(String collection) throws UnknownHostException {
        DBCollection coll = getConexion().getCollection(collection);
        int num = (int) coll.count();
        return num;
    }

    public void borrarDocumentos(String collection) throws UnknownHostException {

        DBCollection coll = getConexion().getCollection(collection);
        coll.drop();
    }

    public void insertarRecomendaciones(int idU, int idV) throws UnknownHostException {
        //System.out.println("entro al metodo");
        BasicDBObject doc = new BasicDBObject();
        doc.append("idUsuario", idU);
        doc.append("idVideo", idV);
        DBCollection coll1 = getConexion().getCollection("recomendaciones");
        coll1.insert(doc);

    }

    public Vector obtenerCompetencias(int idUsuario) throws UnknownHostException {
        DBCollection coll1 = getConexion().getCollection("usuarios");
        Vector competencias = new Vector();
        DBObject usuario = new BasicDBObject("_id", idUsuario);
        DBCursor cursor = coll1.find(usuario);
        DBObject obj = cursor.next();

        competencias.add(obj.get("competencia1"));
        competencias.add(obj.get("competencia2"));
        competencias.add(obj.get("competencia3"));
        competencias.add(obj.get("competencia4"));
        competencias.add(obj.get("competencia5"));
        competencias.add(obj.get("competencia6"));
        competencias.add(obj.get("deficit1"));
        competencias.add(obj.get("deficit2"));

        System.out.println("Competencias: " + competencias);

        return competencias;
    }

    public Vector filtroContenido(Vector competenciass) throws UnknownHostException {        
        DBCollection coll = obtenerColeccion("videos"); //descripciones guardadas en coll
        BasicDBList or = new BasicDBList();
        if (competenciass.get(0).equals(1)) {
            DBObject clause1 = new BasicDBObject("competencias", "Competencia1");
            or.add(clause1);
            System.out.println("comp1");
        }
        if (competenciass.get(1).equals(1)) {
            DBObject clause2 = new BasicDBObject("competencias", "Competencia2");
            or.add(clause2);
            System.out.println("comp2");
        }
        if (competenciass.get(2).equals(1)) {
            DBObject clause3 = new BasicDBObject("competencias", "Competencia3");
            or.add(clause3);
            System.out.println("comp3");
        }
        if (competenciass.get(3).equals(1)) {
            DBObject clause4 = new BasicDBObject("competencias", "Competencia4");
            or.add(clause4);
            System.out.println("comp4");
        }
        if (competenciass.get(4).equals(1)) {
            DBObject clause5 = new BasicDBObject("competencias", "Competencia5");
            or.add(clause5);
            System.out.println("comp5");
        }
        if (competenciass.get(5).equals(1)) {
            DBObject clause6 = new BasicDBObject("competencias", "Competencia6");
            or.add(clause6);
            System.out.println("comp6");
        }

        DBObject clause7 = new BasicDBObject("competencias", competenciass.get(6));
        DBObject clause8 = new BasicDBObject("competencias", competenciass.get(7));
        or.add(clause7);
        or.add(clause8);

        DBObject query1 = new BasicDBObject("$or", or);
        DBCursor cursor = coll.find(query1);
        int i = 1;
        Vector<Video> datos = new Vector();
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            Video p = new Video();
            p.setId((int) obj.get("_id"));
            p.setNombre((String) obj.get("nombre"));
            String competencias = (String) obj.get("competencias").toString();
            String[] tokens;
            tokens = parsearTexto(competencias); //eliminar llaves, comillas y comas
            Vector temp = new Vector(); //vector para guardar actores

            for (int cont = 0; cont < tokens.length; cont++) {
                temp.add(tokens[cont]);
            }
            p.setCompetencia(temp);
            p.setLike((int) obj.get("like"));
            p.setDislike((int) obj.get("dislike"));
            String keywords = (String) obj.get("keywords").toString();
            String[] tokens2;
            tokens2 = parsearTexto(keywords); //eliminar llaves, comillas y comas
            Vector temp2 = new Vector(); //vector para guardar actores

            for (int cont2 = 0; cont2 < tokens2.length; cont2++) {
                temp2.add(tokens2[cont2]);
            }
            p.setKeywords(temp2);
            datos.add(p); //aquí se va incluyendo la info de los contenidos en el vector datos
            i++;
        }
        for (int j = 0; j < datos.size(); j++) {
            System.out.println("*****************************");
            System.out.println("Video" + j);
            System.out.println(datos.get(j).getNombre());
            System.out.println("competencias" + datos.get(j).getCompetencias());

        }
        return datos;
    }

    private static String[] parsearTexto(String actores) {
        String actores4 = actores.replaceAll("\"", "");
        String actores5 = actores4.replaceAll(" ", "");
        String actores6 = actores5.replace("[", "");
        String actores7 = actores6.replace("]", "");
        String[] tokens;
        tokens = actores7.split(",");
        return tokens;
    }

}
