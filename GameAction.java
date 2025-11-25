package edu.uob;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class GameAction
{

    /// check for health prod/cons and have special flag

    public static GameAction newBasicAction(String basicCommand){
        GameAction action = new GameAction();
        action.isBasicCommand = true; // use get/set
        action.triggerWords.add(basicCommand);
        action.basicCommandIdentifier = basicCommand;
        switch(basicCommand){
            case "inv":
                action.triggerWords.add("inventory");
                action.actionNarrative = "You rummage in your bag and find you have ";
                break;
            case "get":
                action.actionNarrative = "You picked up ";
                action.anySubjectValid = true;
                break;
            case "drop":
                action.actionNarrative = "You dropped ";
                action.anySubjectValid = true;
                break;
            case "goto":
                action.actionNarrative = "You travel to ";
                action.anySubjectValid = true;

                break;
            case "look":
                action.actionNarrative = "You look around...";
                break;

            case "health":
                action.actionNarrative = "Your current health is ";
                break;
        }
    return action;
    }

    public String getActionNarrative(){ return actionNarrative; }


    public static Set<String> getStandardActionSet(){
        Set<String> basicCommands = new HashSet<>();
        basicCommands.add("inv");
        basicCommands.add("get");
        basicCommands.add("drop");
        basicCommands.add("goto");
        basicCommands.add("look");
        basicCommands.add("health");
        return basicCommands;
    }

    public static Set<GameAction> generateStandardActions(){
        Set<GameAction> actions = new HashSet<>();
        for(String basicCommand : GameAction.getStandardActionSet()){
            actions.add(GameAction.newBasicAction(basicCommand));
        }
            return actions;
    }


    private String basicCommandIdentifier;
    private Vector<String> triggerWords = new Vector<>();
    private Vector<String> subjects = new Vector<>();
    private Vector<String> consumables = new Vector<>();
    private Vector<String> producables = new Vector<>();
    private String actionNarrative;

    private boolean isBasicCommand = false;
    private boolean anySubjectValid = false;

    public  String basicCommandIdentifier(){
        if(isBasicCommand){
            return basicCommandIdentifier;
        }
        return null;
    }

    public Vector<String> getSubjects() {
        return subjects;
    }

    public Vector<String> getConsumables() {
        return consumables;
    }
    public Vector<String> getProducables() {
        return producables;
    }

    public boolean isBasicCommand() {
        return isBasicCommand;
    }
    public boolean anySubject() {
        return anySubjectValid;
    }

    public boolean isStandalone(){
        if(!isBasicCommand) return false;
        return ((!this.basicCommandIdentifier.equals("goto") && !this.anySubjectValid) && this.subjects.isEmpty());
    }

    public Vector<String> getTriggerWords(){
        return triggerWords;
    }


    public void setNarrative(String narrative) {
        actionNarrative = narrative;
    }

    public void setActionComponents(Vector<String> components, String componentType){

        switch(componentType){
            case "triggers":
                this.triggerWords.addAll(components);
                break;
            case "consumed":
                // use switch inside of loop
                this.consumables.addAll(components);
                break;
            case "produced":
                this.producables.addAll(components);
                  break;
            case "subjects":
             this.subjects.addAll(components);
        }
    }

}
