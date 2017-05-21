/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package op;

import java.util.Vector;

/**
 *
 * @author jlbel
 */
public class Competencia {

    private String Id;
    private String Title;
    private String LevelScheme;
    private int Level;
    private String CompetencyType;
    private Vector ActionVerb;
    private Vector Topic;
    private String IntendedUserRole;
    private String TypicalAgeRange;
    private String Difficulty;
    private int TypicalLearningTime;
    private String Languge;
    private Vector Keywords;

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public Vector getTopic() {
        return Topic;
    }

    public void setTopic(Vector Topic) {
        this.Topic = Topic;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getLevelScheme() {
        return LevelScheme;
    }

    public void setLevelScheme(String LevelScheme) {
        this.LevelScheme = LevelScheme;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int Level) {
        this.Level = Level;
    }

    public String getCompetencyType() {
        return CompetencyType;
    }

    public void setCompetencyType(String CompetencyType) {
        this.CompetencyType = CompetencyType;
    }

    public Vector getActionVerb() {
        return ActionVerb;
    }

    public void setActionVerb(Vector ActionVerb) {
        this.ActionVerb = ActionVerb;
    }  

    public String getIntendedUserRole() {
        return IntendedUserRole;
    }

    public void setIntendedUserRole(String IntendedUserRole) {
        this.IntendedUserRole = IntendedUserRole;
    }

    public String getTypicalAgeRange() {
        return TypicalAgeRange;
    }

    public void setTypicalAgeRange(String TypicalAgeRange) {
        this.TypicalAgeRange = TypicalAgeRange;
    }

    public String getDifficulty() {
        return Difficulty;
    }

    public void setDifficulty(String Difficulty) {
        this.Difficulty = Difficulty;
    }

    public int getTypicalLearningTime() {
        return TypicalLearningTime;
    }

    public void setTypicalLearningTime(int TypicalLearningTime) {
        this.TypicalLearningTime = TypicalLearningTime;
    }

    public String getLanguge() {
        return Languge;
    }

    public void setLanguge(String Languge) {
        this.Languge = Languge;
    }

    public Vector getKeywords() {
        return Keywords;
    }

    public void setKeywords(Vector Keywords) {
        this.Keywords = Keywords;
    }

}
