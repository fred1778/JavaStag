package edu.uob;

import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Id;
import com.alexmerz.graphviz.objects.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class GameLocation extends GameEntity {

    static public LinkedHashMap<String, GameLocation> buildEntityList(EntityType type, Graph subgraph){
        // Add storeroom handling
        LinkedHashMap<String, GameLocation> entities = new LinkedHashMap<>();
            Iterator<Graph> locations = subgraph.getSubgraphs().iterator();
            while(locations.hasNext()){
                Graph location = locations.next();
                Iterator<Node> nodes = location.getNodes(true).iterator();
                Iterator<Graph> subgraphs = location.getSubgraphs().iterator();
                Node first = nodes.next();
                String name = first.getId().getId();
                String desc = first.getAttribute("description");
                GameLocation newLocation = new GameLocation(name, desc, subgraphs);
                entities.put(name, newLocation);
        }

        return entities;
    }

    private HashMap<String, GameLocation> locationPathways = new HashMap<>();
    private HashMap<String, GameEntity> locationEntities = new HashMap<>();


    public boolean entityAvailableAtLocation(String entity){
        return this.getEntitiesAtLocation().containsKey(entity);
    }
    public void addEntityToLocation(GameEntity entity){
        this.locationEntities.put(entity.getName(), entity);
    }

    public HashMap<String, GameEntity> getEntitiesAtLocation(){
        return locationEntities;
    }

    public void addPathway(GameLocation targetLocation){
        this.locationPathways.put(targetLocation.getName(), targetLocation);
    }
    public void removePathway(GameLocation targetLocation){
        this.locationPathways.remove(targetLocation.getName(), targetLocation);
    }

    public void removeEntityFromLocation(GameEntity entity){
        this.locationEntities.remove(entity.getName());
    }

    public HashMap<String, GameLocation> getPathways(){
        return this.locationPathways;
    }

    public boolean checkIfPathwayTo(GameLocation targetLocation){
        return (this.locationPathways.containsKey(targetLocation.getName()));
    }


    public GameLocation(String name, String desc, Iterator<Graph> subgraphs) {
        super(name, desc, EntityType.location);
        this.buildLocationContents(subgraphs);
    }



    private void buildLocationContents(Iterator<Graph> subgraphs) {
        Id artefactID = new Id();
        artefactID.setId("artefacts");
        Id furnitureID = new Id();
        furnitureID.setId("furniture");
        Id characterID = new Id();
        characterID.setId("characters");
        while(subgraphs.hasNext()){
            Graph subgraph = subgraphs.next();
            if(subgraph.getId().isEqual(artefactID)){
                this.locationEntities.putAll(GameEntity.extractEntitiesFromGraph(subgraph, EntityType.artefact));
            } else if (subgraph.getId().isEqual(furnitureID)){
                this.locationEntities.putAll(GameEntity.extractEntitiesFromGraph(subgraph, EntityType.furniture));
            } else if (subgraph.getId().isEqual(characterID)){
                this.locationEntities.putAll(GameEntity.extractEntitiesFromGraph(subgraph, EntityType.character));
            }
        }
    }


}
