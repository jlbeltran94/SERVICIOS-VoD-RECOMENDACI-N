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
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import op.Competencia;
import op.Video;
import org.bson.Document;
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
    public static void main(String[] args) throws UnknownHostException, ParseException {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);//nivel de logger que se imprime en pantalla
        System.out.println("Loading.....");
        Manejador man = new Manejador();
        MongoDatabase DB = man.getConexion();
        double umbralSimilitud = 0.6; //similitud minima de la competencia
        double umbralSimilitudvideo = 0.4;//similitud minima de los videos
        DB.getCollection("recomendaciones").drop(); //eliminamos las recomendaciones ya existentes      

        calcularSimilitudVideos(videos(DB, man), DB);
        calcularSimilitud(competencias(DB, man), DB);
        MongoCollection<Document> coll2 = man.obtenerColeccion("usuarios"); //obtenemos todos los usuarios
        MongoCursor<Document> cursor2 = coll2.find().iterator();

        while (cursor2.hasNext()) {
            Document obj = cursor2.next();
            Vector<Video> datos = new Vector();
            Vector<Video> datos2 = new Vector();
            Vector<Competencia> competenciasusr = new Vector();
            Vector<Video> videoslike = new Vector();
            Vector<Video> videosdislike = new Vector();
            Vector videossimil = new Vector();
            int idU = Integer.parseInt(obj.get("_id").toString());
            String idUstring = obj.get("_id").toString();
            competenciasusr = man.obtenerCompetencias(idU);
            videoslike = man.obtenerlikes(idUstring);
            videosdislike = man.obtenerdislikes(idUstring);
            videossimil = man.obtenersimilvideos(videoslike, umbralSimilitudvideo);

            datos = man.filtroContenido(competenciasusr);
            datos2 = man.filtroContenido(man.obtenersimilcomp(competenciasusr, umbralSimilitud));

            for (int l = 0; l < datos.size(); l++) {
                if (videosdislike.contains(datos.get(l).getId())) {
                    System.out.println("esta");
                } else {
                    man.insertarRecomendaciones(idU, datos.get(l).getId(), "Directa", DB);
                }
            }
            for (int l = 0; l < datos2.size(); l++) {
                if (videosdislike.contains(datos2.get(l).getId())) {
                    System.out.println("esta");
                } else {
                    man.insertarRecomendaciones(idU, datos2.get(l).getId(), "Por Similitud Competencia", DB);
                }
            }
            for (int l = 0; l < videossimil.size(); l++) {
                if (videosdislike.contains(videossimil.get(l))) {
                    System.out.println("esta");
                } else {
                    man.insertarRecomendaciones(idU, videossimil.get(l).toString(), "Por Similitud video", DB);
                }
            }
        }
    }

    public static Vector videos(MongoDatabase DB, Manejador man) throws UnknownHostException, ParseException {
        MongoCollection<Document> collv = DB.getCollection("videos"); //descripciones guardadas en coll
        MongoCursor<Document> cursorv = collv.find().iterator();
        int j = 1;
        Vector<Video> datosvideo = new Vector();
        while (cursorv.hasNext()) {
            Document obj = cursorv.next();
            Video p = new Video();
            p.setId((String) obj.get("Id"));
            p.setNombre((String) obj.get("nombre"));
            p.setLike((int) obj.get("like"));
            p.setDislike((int) obj.get("dislike"));
            String competencias = (String) obj.get("competencias").toString();
            String[] tokens;
            tokens = man.parsearTexto(competencias); //eliminar llaves, comillas y comas
            Vector temp = new Vector(); //vector para guardar actionverb

            for (int cont = 0; cont < tokens.length; cont++) {
                temp.add(tokens[cont]);
            }
            p.setCompetencia(temp);
            String keywords = (String) obj.get("keywords").toString();
            String[] tokens3;
            tokens3 = man.parsearTexto(keywords); //eliminar llaves, comillas y comas
            Vector temp2 = new Vector(); //vector para guardar Topic

            for (int cont = 0; cont < tokens3.length; cont++) {
                temp2.add(tokens3[cont]);
            }
            p.setKeywords(temp2);

            datosvideo.add(p); //aquí se va incluyendo la info de los contenidos en el vector datos
            j++;
        }
        return datosvideo;
    }

    public static Vector competencias(MongoDatabase DB, Manejador man) throws UnknownHostException, ParseException {
        MongoCollection<Document> coll = DB.getCollection("competencias"); //descripciones guardadas en coll
        MongoCursor<Document> cursor = coll.find().iterator();
        int i = 1;
        Vector<Competencia> datoscomp = new Vector();
        while (cursor.hasNext()) {
            Document obj = cursor.next();
            Competencia p = new Competencia();
            p.setId((String) obj.get("Id"));
            p.setTitle((String) obj.get("Title"));
            p.setLevelScheme((String) obj.get("LevelScheme"));
            p.setLevel((int) obj.get("Level"));
            p.setCompetencyType((String) obj.get("CompetencyType"));
            String ActionVerb = (String) obj.get("ActionVerb").toString();
            String[] tokens;
            tokens = man.parsearTexto(ActionVerb); //eliminar llaves, comillas y comas
            Vector temp = new Vector(); //vector para guardar actionverb

            for (int cont = 0; cont < tokens.length; cont++) {
                temp.add(tokens[cont]);
            }
            p.setActionVerb(temp);
            String Topic = (String) obj.get("Topic").toString();
            String[] tokens3;
            tokens3 = man.parsearTexto(Topic); //eliminar llaves, comillas y comas
            Vector temp3 = new Vector(); //vector para guardar Topic

            for (int cont = 0; cont < tokens3.length; cont++) {
                temp3.add(tokens3[cont]);
            }
            p.setTopic(temp3);
            p.setIntendedUserRole((String) obj.get("IntendedUserRole"));
            p.setTypicalAgeRange((String) obj.get("TypicalAgeRange"));
            p.setDifficulty((String) obj.get("Difficulty"));
            p.setTypicalLearningTime((int) obj.get("TypicalLearningTime"));
            p.setLanguge((String) obj.get("Languge"));
            String Keywords = (String) obj.get("Keywords").toString();
            String[] tokens2;
            tokens2 = man.parsearTexto(Keywords); //eliminar llaves, comillas y comas
            Vector temp2 = new Vector(); //vector para guardar keywords

            for (int cont2 = 0; cont2 < tokens2.length; cont2++) {
                temp2.add(tokens2[cont2]);
            }
            p.setKeywords(temp2);
            datoscomp.add(p); //aquí se va incluyendo la info de los contenidos en el vector datos
            i++;
        }
        return datoscomp;
    }

    public static void calcularSimilitud(Vector<Competencia> datos, MongoDatabase DB) throws UnknownHostException, ParseException {
        int cont;
        MongoCollection<Document> colle = DB.getCollection("similitudes");
        colle.drop();
        Vector<Document> similitudes = new Vector();
        double sim;
        for (int i = 0; i < datos.size(); i++) {
            //System.out.println("*****************************");

            for (int j = i + 1; j < datos.size(); j++) {  //comparación entre una posición del vector y la siguiente, es decir, entre la información de un contenido y el siguiente              
                cont = 0;

                if (datos.get(i).getId().equals(datos.get(j).getId())) {
                    cont++;
                }

                if (datos.get(i).getTitle().equals(datos.get(j).getTitle())) {
                    cont++; //registra las similitudes entre la información de un contenido y el siguiente
                }

                if (datos.get(i).getLevelScheme().equals(datos.get(j).getLevelScheme())) {
                    cont++;
                }

                if (datos.get(i).getLevel() == datos.get(j).getLevel()) {
                    cont++;
                }

                if (datos.get(i).getCompetencyType().equals(datos.get(j).getCompetencyType())) {
                    cont++;
                }

                for (int k = 0; k < datos.get(i).getActionVerb().size(); k++) { //verificación cruzada entre competencias, por eso un for dentro de otro
                    for (int l = 0; l < datos.get(j).getActionVerb().size(); l++) {
                        if (datos.get(i).getActionVerb().get(k).equals(datos.get(j).getActionVerb().get(l))) {
                            cont++;
                        }
                    }
                }

                for (int k = 0; k < datos.get(i).getTopic().size(); k++) { //verificación cruzada entre Topic, por eso un for dentro de otro
                    for (int l = 0; l < datos.get(j).getTopic().size(); l++) {
                        if (datos.get(i).getTopic().get(k).equals(datos.get(j).getTopic().get(l))) {
                            cont++;
                        }
                    }
                }

                if (datos.get(i).getIntendedUserRole().equals(datos.get(j).getIntendedUserRole())) {
                    cont++;
                }

                if (datos.get(i).getTypicalAgeRange().equals(datos.get(j).getTypicalAgeRange())) {
                    cont++;
                }

                if (datos.get(i).getDifficulty().equals(datos.get(j).getDifficulty())) {
                    cont++;
                }

                if (datos.get(i).getTypicalLearningTime() == datos.get(j).getTypicalLearningTime()) {
                    cont++;
                }

                if (datos.get(i).getLanguge().equals(datos.get(j).getLanguge())) {
                    cont++;
                }

                for (int k = 0; k < datos.get(i).getKeywords().size(); k++) { //verificación cruzada entre keywords, por eso un for dentro de otro
                    for (int l = 0; l < datos.get(j).getKeywords().size(); l++) {
                        if (datos.get(i).getKeywords().get(k).equals(datos.get(j).getKeywords().get(l))) {
                            cont++;
                        }
                    }
                }

                sim = (2.0 * cont) / (19 + datos.get(i).getActionVerb().size() + datos.get(j).getActionVerb().size() + datos.get(i).getKeywords().size() + datos.get(j).getKeywords().size() + datos.get(j).getTopic().size() + datos.get(i).getTopic().size() + datos.get(j).getTopic().size());
                // System.out.println("La similitud de " + datos.get(i).getId() + "|" + datos.get(j).getId() + " es " + sim);
                //man1.insertarDocumento(datos.get(i).getId(), datos.get(j).getId(), sim);   //registra las similitudes en la colección similitudes        

                Document doc = new Document();
                doc.append("idC1", datos.get(i).getId());
                doc.append("idC2", datos.get(j).getId());
                doc.append("similitud", sim);

                similitudes.add(doc);

            }
            //System.out.println("*****************************");
        }

        try {
            colle.insertMany(similitudes);
        } catch (MongoException e) {
        }
        /**
         * **************************
         */
    }

    public static void calcularSimilitudVideos(Vector<Video> datos, MongoDatabase DB) throws UnknownHostException, ParseException {
        int cont;
        MongoCollection<Document> colle = DB.getCollection("similitudesVideos");
        colle.drop();
        Vector<Document> similitudes = new Vector();
        double sim;
        for (int i = 0; i < datos.size(); i++) {
            //System.out.println("*****************************");

            for (int j = i + 1; j < datos.size(); j++) {  //comparación entre una posición del vector y la siguiente, es decir, entre la información de un contenido y el siguiente              
                cont = 0;

                if (datos.get(i).getId().equals(datos.get(j).getId())) {
                    cont++;
                }

                if (datos.get(i).getNombre().equals(datos.get(j).getNombre())) {
                    cont++; //registra las similitudes entre la información de un contenido y el siguiente
                }

                if (datos.get(i).getLike() == datos.get(j).getLike()) {
                    cont++;
                }

                if (datos.get(i).getDislike() == datos.get(j).getDislike()) {
                    cont++;
                }

                for (int k = 0; k < datos.get(i).getCompetencias().size(); k++) { //verificación cruzada entre competencias, por eso un for dentro de otro
                    for (int l = 0; l < datos.get(j).getCompetencias().size(); l++) {
                        if (datos.get(i).getCompetencias().get(k).equals(datos.get(j).getCompetencias().get(l))) {
                            cont++;
                        }
                    }
                }

                for (int k = 0; k < datos.get(i).getKeywords().size(); k++) { //verificación cruzada entre Topic, por eso un for dentro de otro
                    for (int l = 0; l < datos.get(j).getKeywords().size(); l++) {
                        if (datos.get(i).getKeywords().get(k).equals(datos.get(j).getKeywords().get(l))) {
                            cont++;
                        }
                    }
                }

                sim = (2.0 * cont) / (8 + datos.get(i).getCompetencias().size() + datos.get(j).getCompetencias().size() + datos.get(i).getKeywords().size() + datos.get(j).getKeywords().size());
                // System.out.println("La similitud de " + datos.get(i).getId() + "|" + datos.get(j).getId() + " es " + sim);
                //man1.insertarDocumento(datos.get(i).getId(), datos.get(j).getId(), sim);   //registra las similitudes en la colección similitudes        

                Document doc = new Document();
                doc.append("idV1", datos.get(i).getId());
                doc.append("idV2", datos.get(j).getId());
                doc.append("similitud", sim);

                similitudes.add(doc);

            }
            //System.out.println("*****************************");
        }

        try {
            colle.insertMany(similitudes);
        } catch (MongoException e) {
        }
        /**
         * **************************
         */
    }

}
