package edu.uob;

import java.util.HashMap;

public class ActionExecutor {

    private GameAction commandAction;
    private Game currentGame;
    private Player player;
    private String entity;
    private String playerString;

    public ActionExecutor(Game thisGame, GameAction action, String playerName, String subjectEntity){
        this.currentGame = thisGame;
        this.commandAction = action;
        this.player =  thisGame.getPlayer(playerName);
        this.playerString = playerName;
        this.entity = subjectEntity;
    }

    public String executeCommand(){ return this.executeAction();}

    private String entityListStringRenderer(HashMap<String,GameEntity> entities){
        StringBuilder entityList = new StringBuilder();
        for(GameEntity entity : entities.values()) {
            entityList.append(entity.getName());
            entityList.append(" - ");
            entityList.append(entity.getDescription());
            entityList.append(",\n ");
        }
        return entityList.toString();
    }

    private String executeLookCommand(GameLocation location){
        StringBuilder actionResult = new StringBuilder();
        actionResult.append("You are in a ");
        actionResult.append(location.getDescription());
        actionResult.append(" and you see \n ");
        actionResult.append(this.entityListStringRenderer(location.getEntitiesAtLocation()));
        actionResult.append(this.currentGame.checkLocationForOtherPlayer(player));
        for(GameLocation target : location.getPathways().values()) {
            actionResult.append(" and paths to \n");
            actionResult.append(target.getDescription());
            actionResult.append("\n ");
        }
        return actionResult.toString();
    }

    private String executeBasicSearchCommands() {
        StringBuilder actionResult = new StringBuilder();
        GameLocation location = this.player.getCurrentLocation();
        switch(this.commandAction.basicCommandIdentifier()){
            case "health":
                int health = this.player.getHealth();
                actionResult.append("Your health is ");
                actionResult.append(health);
                break;
            case "look":
                actionResult.append(executeLookCommand(location));
                break;
            case "inv":
                actionResult.append("You look in your bag and see...\n");
                if(player.getInventory().isEmpty()) actionResult.append("nothing :(");
                else actionResult.append(entityListStringRenderer(player.getInventory()));
                break;
        }
        return actionResult.toString();
    }


    private String executeInventoryAction(GameAction action, GameEntity entity) {
        if((entity.getEntityType() != EntityType.artefact)) return "Invalid action for this entity type";
       boolean success = false;
       switch(action.basicCommandIdentifier()) {
           case "get":
               success = this.currentGame.executeGetCommand(player, (GameObject) entity);
               break;
           case "drop":
              success =  this.currentGame.executeDropCommands(player, (GameObject) entity);
               break;
            }

        StringBuilder actionResult = new StringBuilder();
        if(success) {
            actionResult.append(action.getActionNarrative());
            actionResult.append(entity.getName());
            actionResult.append(" - ");
            actionResult.append(entity.getDescription());
            return actionResult.toString();
        }
        return "Invalid action for subjects.";
    }


    private String executeBasicChangeCommands(GameAction action, GameEntity targetEntity) {
        if (!action.basicCommandIdentifier().equals("goto")) return this.executeInventoryAction(action, targetEntity);
        if ((targetEntity.getEntityType() != EntityType.location)) return "Invalid action for this entity type";
        if (this.player.getCurrentLocation().checkIfPathwayTo((GameLocation) targetEntity)) {
            StringBuilder actionString = new StringBuilder();
            this.player.moveToNewLocation((GameLocation) targetEntity);
            actionString.append(action.getActionNarrative());
            actionString.append(targetEntity.getDescription());
            return actionString.toString();
        }
        return "Invalid action - no pathway to this location!";
    }



    private String handleBasicCommand(){
        StringBuilder basicActionOutput = new StringBuilder();
        if(this.commandAction.isStandalone()) {
            basicActionOutput.append(executeBasicSearchCommands());
        } else {
            GameEntity subject;
            if(this.commandAction.basicCommandIdentifier().equals("goto")){
                subject = this.currentGame.getLocations().get(this.entity);
            }
            else subject = this.currentGame.playerEntityDirectory(this.playerString).get(this.entity);
            basicActionOutput.append(executeBasicChangeCommands(this.commandAction, subject));
        }
        return basicActionOutput.toString();
    }

    private  String executeAction(){
        StringBuilder actionOutput = new StringBuilder();
        if(this.commandAction.isBasicCommand()){
            actionOutput.append(this.handleBasicCommand());
        } else {
             actionOutput.append(this.commandAction.getActionNarrative());
            for(String consumable : this.commandAction.getConsumables()) {
                System.out.println(consumable);
                this.currentGame.consumeEntity(this.playerString, consumable);
            }
            for(String producible : this.commandAction.getProducables()) {
                this.currentGame.produceEntity(producible, this.playerString);
            }
        }
        if(player.playerIsDead()) {
            player.healthReset();
            actionOutput.append("\nYou are dead - and are back at the start of the game and with nothing in your inventory");
        }
     return actionOutput.toString();
    }

}
