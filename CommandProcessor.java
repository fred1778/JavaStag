package edu.uob;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandProcessor {

    private String command;
    private Game currentGame;
    private String player;
    private GameAction commandAction;
    private String commandSubject;
    private int validActionCount = 0;
    private boolean invalidUsername = false;
    private Vector<String> commandTokens = new Vector<>();
    private String responseMessage = "Invalid action";
    private Vector<String> validSubjects = new Vector<>();

    public CommandProcessor(String command, Game game) {
        this.command = command;
        this.currentGame = game;
    }

    private boolean countValidActions() {
         if(this.identifyCommandActions()) {
             if (validActionCount == 0) {
                 this.responseMessage = "Invalid command: No valid actions found";
                 return false;
             }
             if (validActionCount > 1) {
                 this.responseMessage = "Invalid command: multiple possible actions in command";
                 return false;
             }
             return this.extraneousSubjectCheck();
         }

         this.responseMessage ="Invalid command: presence of multiple built-in actions";
         return false;
    }

    public String processCommand() {
        if(this.cleanCommand()){
            this.tokeniseCommand();
            if(!this.countValidActions()) return this.responseMessage;
            try {
                ActionExecutor executor = new ActionExecutor(this.currentGame, this.commandAction, this.player, this.commandSubject);
                return executor.executeCommand();
            }catch(Exception error) {return error.getMessage();}
        }
        if(this.invalidUsername) return "Invalid username";
        return "This is not a valid command, I'm afraid.";
    }

    private void tokeniseCommand() {
        Iterator<String> tokens =  Arrays.stream(this.command.split(" ")).iterator();
        while(tokens.hasNext()){
            String token = tokens.next().trim();
            this.commandTokens.add(token);
        }
    }

    private boolean multiwordTriggerCheck(GameAction action) {
        for(String trigger : action.getTriggerWords()){
            if(trigger.contains(" ")){
               if(this.command.contains(trigger)){
                   this.commandAction = action;
                   for(String component : Arrays.asList(trigger.split(" "))){
                       this.commandTokens.remove(component);
                   }
                   return true;
               }
            }
        }
        return false;
    }

    private boolean commandHasAction(GameAction action) {
        if(this.multiwordTriggerCheck(action)) return true;
        for(String trigger : action.getTriggerWords()) {
            if (this.commandTokens.contains(trigger)){
              this.commandTokens.remove(trigger);
                return true;
            }
        }
        return false;
    }

    private boolean identifyCommandActions(){
        boolean basicActionFound = false;
        for(GameAction action : this.currentGame.getActions()){
            if(this.commandHasAction(action)){
                if(basicActionFound) return false;
                if(action.isBasicCommand()) basicActionFound = true;
                if(matchSubjectsToAction(action)) this.validActionCount++;
            }
        }
        return true;
    }


    private boolean matchSubjectsToAction(GameAction action){
        if(action.isStandalone()) { this.commandAction = action;
            return true;
        }
        if(action.anySubject()){
            // There is something else in the command that could be an entity
              if(this.verifyWildcardSubject(action) && this.subjectsAvailable(action)){ this.commandAction = action;
                  return true;
              }
        }
        int subjectsFound = 0;
        Vector<String> actionSubjects = new Vector<>();
        for(String subject : action.getSubjects()) {
            if (this.commandTokens.contains(subject)) {
                actionSubjects.add(subject);
                subjectsFound++;
            }
        }
        if ((subjectsFound > 0) && this.subjectsAvailable(action)) {
            this.commandAction = action;
            this.validSubjects.addAll(actionSubjects);
            return true;
        }
        return false;
    }

    // Next, are the subjects available?
    private boolean verifyWildcardSubject(GameAction action){
        for(String entity : this.commandTokens) {
            if (action.basicCommandIdentifier().equals("goto")) {
                if(this.currentGame.getLocationFromString(entity) != null){
                    this.commandSubject = entity;
                    this.commandTokens.remove(entity);
                    return true;
                }
            } else if (this.currentGame.entityIsAvailable(entity, this.player)) {
                this.commandSubject = entity;
                this.commandTokens.remove(entity);
                return true;
            }
        }
        return false;
    }


    private boolean extraneousSubjectCheck(){
        // Remaining tokens in command tokens can be anything else including other trigger words BUT not entities that are not subjects....

        for(String commandToken : this.commandTokens){
            if(this.currentGame.globalEntityManifest().containsKey(commandToken)){
               if(!this.validSubjects.contains(commandToken)) return false;
            }
            if(GameAction.getStandardActionSet().contains(commandToken)){
                return false;
            }
        }
        return true;
    }

    private boolean subjectsAvailable(GameAction action){
        for(String subject : action.getSubjects()){
            if(!this.currentGame.entityIsAvailable(subject, this.player)) return false;
        }
        return true;
    }

    boolean cleanCommand(){
        // checks if command is legitimate and cleans it
        if(this.command.equalsIgnoreCase("")) return false;
        if(this.determinePlayer()){
            this.command = this.command.toLowerCase();
            return !(this.command.isEmpty() || this.command.equals(" "));
        }
        return false;
    }

    boolean determinePlayer(){
        Iterator<String>  playerSplit =  Arrays.stream(this.command.split(":")).iterator();
        if(!playerSplit.hasNext()) return false;
        String playerName = playerSplit.next();
        if(!playerSplit.hasNext()) return false;
       if(!this.validateUsername(playerName)) return false;
       this.currentGame.processPlayer(playerName);
       this.player = playerName;
       this.command = playerSplit.next();
       return true;
    }

    boolean validateUsername(String name){
        if(name.isEmpty()) return false;
        Pattern namePattern = Pattern.compile("^[a-zA-Z '-]*$");
        Matcher nameMatcher = namePattern.matcher(name);
        boolean testOutcome = nameMatcher.find();
        this.invalidUsername = !testOutcome;
        return testOutcome;
    }
}
