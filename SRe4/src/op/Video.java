package op;

import java.util.Vector;

public class Video {

    
    private String Id;
    private String nombre;
    private Vector competencias;
    private int like;
    private int dislike;
    private Vector keywords;

    

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public Vector getKeywords() {
        return keywords;
    }

    public void setKeywords(Vector keywords) {
        this.keywords = keywords;
    }

    public Vector getCompetencias() {
        return competencias;
    }

    public void setCompetencia(Vector competencias) {
        this.competencias = competencias;
    }

}
