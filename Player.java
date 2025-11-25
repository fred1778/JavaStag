package edu.uob;
import java.util.HashMap;

public class Player {
    private String name;
    private GameLocation currentLocation;
    private HashMap<String, GameEntity> inventory = new HashMap<>();
    private int healthPoints = 3;
    private boolean isDead = false;

    public boolean playerIsDead() {
        return isDead;
    }

    public String getName() {return this.name;}

    public void healthReset(){
        this.inventory.clear();
        this.healthPoints = 3;
        this.isDead = false;
    }
    public int getHealth(){
        return this.healthPoints;
    }
    public void changeHealth(int change){
        this.healthPoints += change;
        if(healthPoints > 3) healthPoints = 3;
        if(this.healthPoints <= 0){
            isDead = true;
        }
    }
    public void moveToNewLocation(GameLocation newLocation) {
        this.currentLocation = newLocation;
    }

    public void addToInventory(GameObject artefact) {
        this.inventory.put(artefact.getName(), artefact);
    }

    public void removeFromInventory(String entityName) {
        this.inventory.remove(entityName);
    }

    public void setCurrentLocation(GameLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean entityInInventory(String entityName) {
        return this.inventory.get(entityName) != null;
    }

    public HashMap<String, GameEntity> getInventory() {
        return this.inventory;
    }

    public GameLocation getCurrentLocation() {
        return this.currentLocation;
    }

    public Player(String name) {
        this.name = name;
    }

}
