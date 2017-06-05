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
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import org.bson.Document;

/**
 *
 * @author jlbel
 */
public class Manejador {
    
    public MongoDatabase getConexion() throws UnknownHostException {//conexion BD

        /*conexion a mlab ********************/
        MongoClientURI uri = new MongoClientURI("mongodb://jlbeltranc:3002028690jb@ds155811.mlab.com:55811/vod_enfasis4");
        MongoClient MongoClient = new MongoClient(uri);
        MongoDatabase db = MongoClient.getDatabase(uri.getDatabase());
        // conexion local
        //MongoClient mongoClient = new MongoClient("localhost", 27017);
        //MongoDatabase db = mongoClient.getDatabase("video");
        return db;
    }

    public MongoCollection<Document> obtenerColeccion(String collection) throws UnknownHostException {
        MongoCollection<Document> coll = getConexion().getCollection(collection);//obtener la coleccion que se quiere
        return coll;
    }

    public int numerodocumentos(String collection) throws UnknownHostException {
        MongoCollection<Document> coll = getConexion().getCollection(collection);
        int num = (int) coll.count();//numero de documentos en la coleccion
        return num;
    }

    public void borrarDocumentos(String collection) throws UnknownHostException {//eliminar coleccion

        MongoCollection<Document> coll = getConexion().getCollection(collection);
        coll.drop();
    }

    public void insertarRecomendaciones(int idU, String idV, String tipo, MongoDatabase BD) throws UnknownHostException {
        //inserta recomendaciones en la coleccion recomendaciones
        Document doc = new Document();
        doc.append("idUsuario", idU);
        doc.append("idVideo", idV);
        doc.append("tipo", tipo);
        MongoCollection<Document> coll1 = BD.getCollection("recomendaciones");

        //se crea indice unico para la recomendacion y que no se repita
        BasicDBObject doc2 = new BasicDBObject();
        doc2.append("idUsuario", idU);
        doc2.append("idVideo", 1);
        IndexOptions indexOptions = new IndexOptions().unique(true);
        coll1.createIndex(doc2, indexOptions);

        try {
            coll1.insertOne(doc); //intenta insertar la recomendacion
        } catch (MongoWriteException e) {
            //si ya existe la recomendacion no se hace nada
        }

    }

    public Vector filtroContenido(Vector competenciass) throws UnknownHostException {
        Vector<Video> datos = new Vector(); //vector que almacenara los datos
        if (competenciass.isEmpty()) {
            return datos; //si no se tiene competencias para analizar se retorna el vector vacio
        } else {
            int numdislikes = 30; //numero maximo de dislikes del video
            MongoCollection<Document> coll = obtenerColeccion("videos"); //descripciones guardadas en coll
            BasicDBList or = new BasicDBList(); //lista de las competencias 

            for (int i = 0; i < competenciass.size(); i++) {
                DBObject clause = new BasicDBObject("competencias", competenciass.get(i)); //se obtiene las competencias
                or.add(clause);//se añaden a la lista
            }

            Document query = new Document("$or", or).append("dislike", new BasicDBObject("$lt", numdislikes));//se arma la sentencia de busqueda            
            MongoCursor<Document> cursor = coll.find(query).iterator(); //resultados de la base de datos
            int i = 1;

            while (cursor.hasNext()) {//se asignan los datos obtenidos a la lista de videos
                Document obj = cursor.next();
                Video p = new Video();
                p.setId((String) obj.get("Id"));
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
                System.out.println("Recomendacion " + (j + 1));
                System.out.println("_id: " + datos.get(j).getId());
                System.out.println("Nombre: " + datos.get(j).getNombre());
                System.out.println("competencias: " + datos.get(j).getCompetencias());

            }
            return datos;
        }
    }

    public static String[] parsearTexto(String texto) {
        String texto4 = texto.replaceAll("\"", "");
        String texto5 = texto4.replaceAll(" ", "");
        String texto6 = texto5.replace("[", "");
        String texto7 = texto6.replace("]", "");
        String[] tokens;
        tokens = texto7.split(",");
        return tokens;
    }

    public Vector obtenersimilcomp(Vector competencias, double umbralSimilitud) throws UnknownHostException {
        Vector competenciassimil = new Vector();
        if (competencias.isEmpty()) {
            return competenciassimil;
        } else {
            /**
             * ****************************************
             */

            BasicDBList or = new BasicDBList();
            for (int i = 0; i < competencias.size(); i++) {
                DBObject clause = new BasicDBObject("idC1", competencias.get(i));
                DBObject clause2 = new BasicDBObject("idC2", competencias.get(i));
                or.add(clause);
                or.add(clause2);
            }
            Document query = new Document("$or", or).append("similitud", new BasicDBObject("$gt", umbralSimilitud));
            MongoCollection<Document> coll2 = getConexion().getCollection("similitudes");
            MongoCursor<Document> cursor2 = coll2.find(query).iterator(); //de los docmentos encontrados, eliminamos _id de cada documento porque no lo necesitamos, y dejamos sólo id1 e id2

            int n = 0;
            while (cursor2.hasNext()) {
                Document obj1 = cursor2.next();
                if (competencias.contains(obj1.get("idC1"))) {
                } else {
                    competenciassimil.add(obj1.get("idC1").toString());
                }

                if (competencias.contains(obj1.get("idC2"))) {
                } else {
                    competenciassimil.add(obj1.get("idC2").toString());
                }

                n++;
            }
            System.out.println("*********************************");
            System.out.println("Competencias Similares: " + competenciassimil);

            /**
             * ***************************************
             */
            return competenciassimil;
        }
    }

    public Vector obtenerCompetencias(int idUsuario) throws UnknownHostException {
        MongoCollection<Document> coll1 = getConexion().getCollection("usuarios");
        Vector competencias = new Vector();
        Document usuario = new Document("_id", idUsuario);
        MongoCursor<Document> cursor = coll1.find(usuario).iterator();
        Document obj = cursor.next();
        /**
         *
         */

        List comps = (List) obj.get("competencias");
        for (int i = 0; i < comps.size(); i++) {
            Document competencia = (Document) comps.get(i);

            if (Float.parseFloat(competencia.get("value").toString()) < 3.5) {

                competencias.add(competencia.get("Id"));
            }
        }

        System.out.println("*********************************");
        System.out.println("Usuario" + idUsuario);
        System.out.println("Competencias Usuario: " + competencias);

        return competencias;
    }

    public Vector obtenerlikes(String idUsuario) throws UnknownHostException {
        MongoCollection<Document> coll1 = getConexion().getCollection("likes");
        Vector videos = new Vector();
        Document usuario = new Document("idUsuario", idUsuario);
        MongoCursor<Document> cursor = coll1.find(usuario).iterator();

        while (cursor.hasNext()) {//se asignan los datos obtenidos a la lista de videos
            Document obj = cursor.next();
            videos.add(obj.get("idVideo"));
        }

        /**
         *
         */
        System.out.println("*********************************");
        System.out.println("Usuario" + idUsuario);
        System.out.println("Videos que le gustan al Usuario: " + videos);

        return videos;
    }

    public Vector obtenerdislikes(String idUsuario) throws UnknownHostException {
        MongoCollection<Document> coll1 = getConexion().getCollection("dislikes");
        Vector videos = new Vector();
        Document usuario = new Document("idUsuario", idUsuario);
        MongoCursor<Document> cursor = coll1.find(usuario).iterator();

        while (cursor.hasNext()) {//se asignan los datos obtenidos a la lista de videos
            Document obj = cursor.next();
            videos.add(obj.get("idVideo"));
        }

        /**
         *
         */
        System.out.println("*********************************");
        System.out.println("Usuario" + idUsuario);
        System.out.println("Videos que NO le gustan al Usuario: " + videos);

        return videos;
    }

    public Vector obtenersimilvideos(Vector videos, double umbralSimilitud) throws UnknownHostException {
        Vector videossimil = new Vector();
        if (videos.isEmpty()) {
            return videossimil;
        } else {
            /**
             * ****************************************
             */

            BasicDBList or = new BasicDBList();
            for (int i = 0; i < videos.size(); i++) {
                DBObject clause = new BasicDBObject("idV1", videos.get(i));
                DBObject clause2 = new BasicDBObject("idV2", videos.get(i));
                or.add(clause);
                or.add(clause2);
            }
            Document query = new Document("$or", or).append("similitud", new BasicDBObject("$gt", umbralSimilitud));
            MongoCollection<Document> coll2 = getConexion().getCollection("similitudesVideos");
            MongoCursor<Document> cursor2 = coll2.find(query).iterator(); //de los docmentos encontrados, eliminamos _id de cada documento porque no lo necesitamos, y dejamos sólo id1 e id2

            int n = 0;
            while (cursor2.hasNext()) {
                Document obj1 = cursor2.next();
                if (videos.contains(obj1.get("idV1"))) {
                } else {
                    videossimil.add(obj1.get("idV1").toString());
                }

                if (videos.contains(obj1.get("idV2"))) {
                } else {
                    videossimil.add(obj1.get("idV2").toString());
                }

                n++;
            }
            System.out.println("*********************************");
            System.out.println("Videos similares a los que le gustan: " + videossimil);

            /**
             * ***************************************
             */
            return videossimil;
        }
    }

}
