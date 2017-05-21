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
import op.Competencia;
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
    public static void main(String[] args) throws UnknownHostException, ParseException {
        Manejador man = new Manejador();
        Vector<Video> datos = new Vector();

        double umbralSimilitud = 0.4;
        /**
         * * ******************************
         */
        DBCollection coll = man.obtenerColeccion("competencias"); //descripciones guardadas en coll
        DBCursor cursor = coll.find();
        int i = 1;
        Vector<Competencia> datoscomp = new Vector();
        while (cursor.hasNext()) {
            DBObject obj = cursor.next();
            Competencia p = new Competencia();
            p.setId((String) obj.get("Id"));
            p.setTitle((String) obj.get("Title"));
            p.setLevelScheme((String) obj.get("LevelScheme"));
            p.setLevel((int) obj.get("Level"));
            p.setCompetencyType((String) obj.get("CompetencyType"));
            String ActionVerb = (String) obj.get("ActionVerb").toString();
            String[] tokens;
            tokens = man.parsearTexto(ActionVerb); //eliminar llaves, comillas y comas
            Vector temp = new Vector(); //vector para guardar actores

            for (int cont = 0; cont < tokens.length; cont++) {
                temp.add(tokens[cont]);
            }
            p.setActionVerb(temp);
            String Topic = (String) obj.get("Topic").toString();
            String[] tokens3;
            tokens3 = man.parsearTexto(Topic); //eliminar llaves, comillas y comas
            Vector temp3 = new Vector(); //vector para guardar actores

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
            Vector temp2 = new Vector(); //vector para guardar actores

            for (int cont2 = 0; cont2 < tokens2.length; cont2++) {
                temp2.add(tokens2[cont2]);
            }
            p.setKeywords(temp2);
            datoscomp.add(p); //aquí se va incluyendo la info de los contenidos en el vector datos
            i++;
        }

        /**
         * ******************************
         */
        Scanner in = new Scanner(System.in);
        calcularSimilitud(datoscomp);
        System.out.println("ingrese un IdUsuario: ");
        int idU = in.nextInt(); // Reading from System.in        

        datos = man.filtroContenido(man.obtenerCompetencias(idU, umbralSimilitud));

        for (int l = 0; l < datos.size(); l++) {
            man.insertarRecomendaciones(idU, datos.get(l).getId());
        }

    }

    public static void calcularSimilitud(Vector<Competencia> datos) throws UnknownHostException, ParseException {
        int cont;
        Manejador man1 = new Manejador();
        Vector similitudes = new Vector();
        DBCollection coll1 = man1.getConexion().getCollection("competencias");
        DBCursor cursor = coll1.find();
        double sim;
        for (int i = 0; i < datos.size(); i++) {
            System.out.println("*****************************");

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
                System.out.println("La similitud de " + datos.get(i).getId() + "|" + datos.get(j).getId() + " es " + sim);
                //man1.insertarDocumento(datos.get(i).getId(), datos.get(j).getId(), sim);   //registra las similitudes en la colección similitudes        
                BasicDBObject doc = new BasicDBObject();
                doc.append("id1", datos.get(i).getId());
                doc.append("id2", datos.get(j).getId());
                doc.append("similitud", sim);                
                similitudes.add(doc);                
            }
            System.out.println("*****************************");
        }
        DBCollection colle = man1.getConexion().getCollection("similitudes");
        colle.insert(similitudes);
    }

    

}
