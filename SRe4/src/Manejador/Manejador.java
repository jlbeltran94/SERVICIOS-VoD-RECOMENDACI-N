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
import com.mongodb.MongoException;
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
        coll1.ensureIndex(doc, new BasicDBObject("unique", true));

        try {
            coll1.insert(doc);
        } catch (MongoException.DuplicateKey e) {
            
        }

    }

    public Vector obtenerCompetencias(int idUsuario, double umbralSimilitud) throws UnknownHostException {
        DBCollection coll1 = getConexion().getCollection("usuarios");
        Vector competencias = new Vector();
        DBObject usuario = new BasicDBObject("_id", idUsuario);
        DBCursor cursor = coll1.find(usuario);
        DBObject obj = cursor.next();

        if (obj.get("competencia1").equals(1)) {
            competencias.add("Competencia1");
        }

        if (obj.get("competencia2").equals(1)) {
            competencias.add("Competencia2");
        }

        if (obj.get("competencia3").equals(1)) {
            competencias.add("Competencia3");
        }

        if (obj.get("competencia4").equals(1)) {
            competencias.add("Competencia4");
        }

        if (obj.get("competencia5").equals(1)) {
            competencias.add("Competencia5");
        }

        if (obj.get("competencia6").equals(1)) {
            competencias.add("Competencia6");
        }

        competencias.add(obj.get("deficit1"));
        competencias.add(obj.get("deficit2"));

        /**
         * ****************************************
         */
        BasicDBList or = new BasicDBList();
        for (int i = 0; i < competencias.size(); i++) {
            DBObject clause = new BasicDBObject("id1", competencias.get(i));
            DBObject clause2 = new BasicDBObject("id2", competencias.get(i));
            or.add(clause);
            or.add(clause2);
        }
        DBObject query = new BasicDBObject("$or", or).append("similitud", new BasicDBObject("$gt", umbralSimilitud));
        DBCollection coll2 = getConexion().getCollection("similitudes");
        DBCursor cursor2 = coll2.find(query, new BasicDBObject("id2", true).append("id1", true).append("_id", false)); //de los docmentos encontrados, eliminamos _id de cada documento porque no lo necesitamos, y dejamos sólo id1 e id2

        int n = 0;
        while (cursor2.hasNext()) {
            DBObject obj1 = cursor2.next();
            if (competencias.contains(obj1.get("id1"))) {
            } else {
                competencias.add(obj1.get("id1"));
            }

            if (competencias.contains(obj1.get("id2"))) {
            } else {
                competencias.add(obj1.get("id2"));
            }

            n++;
        }

        System.out.println("Competencias: " + competencias);

        /******************************************/
        return competencias;
    }

    public Vector filtroContenido(Vector competenciass) throws UnknownHostException {
        int numlikes = 20; //numero minimo de likes del video
        DBCollection coll = obtenerColeccion("videos"); //descripciones guardadas en coll
        BasicDBList or = new BasicDBList();

        for (int i = 0; i < competenciass.size(); i++) {
            DBObject clause = new BasicDBObject("competencias", competenciass.get(i));
            or.add(clause);
        }

        DBObject query = new BasicDBObject("$or", or).append("like", new BasicDBObject("$gt", numlikes));;
        DBCursor cursor = coll.find(query);
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
            Vector temp = new Vector(); //vector para guardar competencias

            for (int cont = 0; cont < tokens.length; cont++) {
                temp.add(tokens[cont]);
            }
            p.setCompetencia(temp);
            p.setLike((int) obj.get("like"));
            p.setDislike((int) obj.get("dislike"));
            String keywords = (String) obj.get("keywords").toString();
            String[] tokens2;
            tokens2 = parsearTexto(keywords); //eliminar llaves, comillas y comas
            Vector temp2 = new Vector(); //vector para guardar keywords

            for (int cont2 = 0; cont2 < tokens2.length; cont2++) {
                temp2.add(tokens2[cont2]);
            }
            p.setKeywords(temp2);
            datos.add(p); //aquí se va incluyendo la info de los contenidos en el vector datos
            i++;
        }
        for (int j = 0; j < datos.size(); j++) {
            System.out.println("*****************************");
            System.out.println("Video " + (j+1));
            System.out.println(datos.get(j).getNombre());
            System.out.println("competencias" + datos.get(j).getCompetencias());

        }
        return datos;
    }

    public static String[] parsearTexto(String actores) {
        String actores4 = actores.replaceAll("\"", "");
        String actores5 = actores4.replaceAll(" ", "");
        String actores6 = actores5.replace("[", "");
        String actores7 = actores6.replace("]", "");
        String[] tokens;
        tokens = actores7.split(",");
        return tokens;
    }

    public void insertarDocumento(String id1, String id2, double sim) throws UnknownHostException {
        //System.out.println("entro al metodo");
        BasicDBObject doc = new BasicDBObject();
        doc.append("id1", id1);
        doc.append("id2", id2);
        doc.append("similitud", sim);

        BasicDBObject doc2 = new BasicDBObject();
        doc2.append("id1", 1);
        doc2.append("id2", 1);
        doc.append("similitud", sim);

        DBCollection coll1 = getConexion().getCollection("similitudes");
        coll1.ensureIndex(doc2, new BasicDBObject("unique", true));

        try {
            coll1.insert(doc);
        } catch (MongoException.DuplicateKey e) {
            
        }

    }

    /*public Vector obtenerTopSimilitudes(Vector competencias, double umbralSimilitud) throws UnknownHostException {
     System.out.println("top simil");
     BasicDBList or = new BasicDBList();
     for (int i = 0; i < competencias.size(); i++) {        
     DBObject clause = new BasicDBObject("id1", competencias.get(i));
     DBObject clause2 = new BasicDBObject("id2", competencias.get(i));
     or.add(clause);
     or.add(clause2);
     }        
     DBObject query = new BasicDBObject("$or",or).append("similitud", new BasicDBObject("$gt",umbralSimilitud));
     DBCollection coll1=getConexion().getCollection("similitudes");    
     DBCursor cursor=coll1.find(query,new BasicDBObject("id2", true).append("id1", true).append("_id", false)); //de los docmentos encontrados, eliminamos _id de cada documento porque no lo necesitamos, y dejamos sólo id1 e id2
     Vector topsimilitudes=new Vector();
     int i=0;
     while (cursor.hasNext()){
     DBObject obj=cursor.next();
     if(topsimilitudes.contains(obj.get("id1"))){
     }else{
     topsimilitudes.add(obj.get("id1"));
     }
            
     if(topsimilitudes.contains(obj.get("id2"))){
     }else{
     topsimilitudes.add(obj.get("id2"));
     }           
            
     i++;
     }  
     return topsimilitudes;
     }*/
    /**
     * private static void obtenerRecomendaciones(int umbralRating, double
     * umbralSimilitud) throws UnknownHostException { Manejador man=new
     * Manejador(); DBObject query=new BasicDBObject("rating",new
     * BasicDBObject("$gt",umbralRating)); //cadena de consulta, identificadores
     * por encima de umbralRating Vector
     * topratings=man.obtenerTopRating(umbralRating,query); //obtenerTopRating,
     * los identificadores de los contenidos Top se almacenan en el vector
     * toprating Vector recomendaciones=new Vector(); for (int i = 0; i <
     * topratings.size(); i++) { Vector rec=new Vector(); DBObject clause1 = new
     * BasicDBObject("id1", topratings.get(i)); DBObject clause2 = new
     * BasicDBObject("id2", topratings.get(i)); BasicDBList or = new
     * BasicDBList(); or.add(clause1); or.add(clause2); DBObject query1=new
     * BasicDBObject("$or",or).append("similitud", new
     * BasicDBObject("$gt",umbralSimilitud)); //cadena de consulta,
     * identificadores en id1 o id2, y similitud por encima de umbralSimilitud
     * rec=man.obtenerTopSimilitudes(topratings.get(i),umbralSimilitud,query1);
     * //obtenerTopSimilitudes recomendaciones.add(rec); }         
    }**
     */
}
