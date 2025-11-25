package edu.uob;
import com.alexmerz.graphviz.objects.Edge;
import java.util.*;

// High level class
public class Game {

    private HashMap<String, GameLocation> locations = new HashMap<>();
    private Set<GameAction> actions = new HashSet<>();
    private HashMap<String, Player> players = new HashMap<>();

    private GameLocation gameStoreroom;

    public void processPlayer(String playerName) {
        if(this.players.containsKey(playerName)) {
            return;
        }
        Player player = new Player(playerName);
        //Put player at start location
        player.setCurrentLocation(this.getStartLocation());
        this.players.put(playerName, player);
    }



    private boolean entityOutOfBounds(String entity, String currentPlayer){
        Player player = this.players.get(currentPlayer);
        for(Player otherPlayer : this.players.values()) {
            if(otherPlayer != player) {
                if(otherPlayer.entityInInventory(entity)) return true;
            }
        }
         return false;
    }


    public HashMap<String, GameEntity> globalEntityManifest(){
        HashMap<String, GameEntity> globalEntities = new HashMap<>();
        for(Player player : this.players.values()) {
            globalEntities.putAll(this.playerEntityDirectory(player.getName()));
        }
        globalEntities.putAll(this.locations);
        return globalEntities;
    }

    public Player getPlayer(String playerName) {
        return this.players.get(playerName);
    }

    public HashMap<String, GameEntity> playerEntityDirectory(String playerName) {
        Player player = this.players.get(playerName);
        HashMap<String, GameEntity> playerEntities = new HashMap<>();
        for(GameLocation location : this.locations.values()) {
            playerEntities.putAll(location.getEntitiesAtLocation());
        }
        playerEntities.putAll(player.getInventory());
        return playerEntities;
    }

    public Set<GameAction> getActions() {
        return this.actions;
    }

    public Game() {
        this.actions.addAll(GameAction.generateStandardActions());
    }


    private void killPlayer(Player player) {
        for(GameEntity entity : player.getInventory().values()) {
            player.getCurrentLocation().addEntityToLocation(entity);

        }
        player.setCurrentLocation(this.getStartLocation());
    }


    private void moveEntityForAction(String entityName, Player thisPlayer, boolean isProducing){
        GameEntity targetEntity;
        if(thisPlayer.entityInInventory(entityName)){
            targetEntity = this.playerEntityDirectory(thisPlayer.getName()).get(entityName);
            thisPlayer.removeFromInventory(entityName);
            if(isProducing)thisPlayer.getCurrentLocation().addEntityToLocation(targetEntity);
           else this.gameStoreroom.addEntityToLocation(targetEntity);
        } else {
            for(GameLocation location : this.locations.values()) {
                if(location.getEntitiesAtLocation().get(entityName) != null) {
                    targetEntity = location.getEntitiesAtLocation().get(entityName);
                    location.removeEntityFromLocation(targetEntity);
                    if(isProducing)thisPlayer.getCurrentLocation().addEntityToLocation(targetEntity);
                    this.gameStoreroom.addEntityToLocation(targetEntity);
                    return;
                }
            }
        }
    }

    public void consumeEntity(String playerName, String entityName) {
        Player thisPlayer = this.getPlayer(playerName);
        if(this.locations.containsKey(entityName)) {
            this.removePathway(thisPlayer.getCurrentLocation(), this.locations.get(entityName));
            return;
        }
        if(entityName.equals("health")){ thisPlayer.changeHealth(-1);
            if (thisPlayer.playerIsDead()){
                this.killPlayer(thisPlayer);
            }
            return;
            }
        this.moveEntityForAction(entityName, thisPlayer, false);
    }

    public void produceEntity(String entityName, String playerName) {
        Player thisPlayer = this.getPlayer(playerName);
        if(entityName.equals("health")){ thisPlayer.changeHealth(1); return;}
        if(this.locations.containsKey(entityName)) {
            this.produceNewPathway(thisPlayer.getCurrentLocation(), this.locations.get(entityName));
            return;
        }
        this.moveEntityForAction(entityName, thisPlayer, true);
    }

    public GameLocation getLocationFromString(String locationName){return this.locations.get(locationName);}

    public String checkLocationForOtherPlayer(Player player) {
        StringBuilder playersAtLocation = new StringBuilder();
        GameLocation location = player.getCurrentLocation();
        for(Player otherPlayer : this.players.values()) {
            if(otherPlayer != player && otherPlayer.getCurrentLocation().equals(location)) {
                playersAtLocation.append(otherPlayer.getName());
                playersAtLocation.append(", ");
            }
        }
        return playersAtLocation.toString();
    }

    public boolean entityIsAvailable(String entity, String player) {
        Player commandPlayer = this.players.get(player);
        GameLocation location = commandPlayer.getCurrentLocation();
        if(this.entityOutOfBounds(entity, player)) return false;
        if(location.entityAvailableAtLocation(entity)) return true;
        return commandPlayer.entityInInventory(entity);
    }

    public void setGameStart(){
        this.startLocationKey = this.locations.keySet().iterator().next();
    }

    private String startLocationKey;

    public HashMap<String,GameLocation> getLocations() {
        return this.locations;
    }
    public GameLocation getStartLocation() {
        return locations.get(startLocationKey);
    }


    public void addGameAction(GameAction action) {
        this.actions.add(action);
    }

    private void removePathway(GameLocation startLocation, GameLocation destination) {
        startLocation.removePathway(destination);
    }

    private void produceNewPathway(GameLocation startLocation, GameLocation destination) {
        startLocation.addPathway(destination);
    }

    public void setPathways(Iterator<Edge> edges) {
        while (edges.hasNext()) {
            Edge thisEdge = edges.next();
            String sourceKey = thisEdge.getSource().getNode().getId().getId();
            String targetKey = thisEdge.getTarget().getNode().getId().getId();
            this.locations.get(sourceKey).addPathway(this.locations.get(targetKey));
        }
    }

    public void setLocations(LinkedHashMap<String, GameLocation> locations) {
        this.locations = locations;
        this.gameStoreroom = locations.get("storeroom");

    }

    public boolean executeDropCommands(Player player, GameObject artefact){
        if(!player.entityInInventory(artefact.getName())) return false;
        player.removeFromInventory(artefact.getName());
        player.getCurrentLocation().addEntityToLocation(artefact);
        return true;
    }

    public boolean executeGetCommand(Player player, GameObject artefact) {
       if(!player.getCurrentLocation().entityAvailableAtLocation(artefact.getName())) return false;

       player.addToInventory(artefact);
       player.getCurrentLocation().removeEntityFromLocation(artefact);
       return true;
        }
    }



