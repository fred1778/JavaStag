package edu.uob;


import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.util.HashMap;
import java.util.Iterator;

public abstract class GameEntity {
    static public HashMap<String, GameEntity> extractEntitiesFromGraph(Graph graph, EntityType type) {
        Iterator<Node> nodes = graph.getNodes(true).iterator();
        HashMap<String, GameEntity> entities = new HashMap<>();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            String name = node.getId().getId();
            String desc = node.getAttribute("description");
            GameEntity entity = null;
            switch (type) {
                case artefact:
                    entity = new GameObject(name,desc, EntityType.artefact);
                    break;
                case furniture:
                    entity = new GameObject(name,desc, EntityType.furniture);
                    break;
                case character:
                    entity = new GameObject(name,desc, EntityType.character);
                    break;
            }
            entities.put(name, entity);
        }
            return entities;
    }

    private String name;
    private String description;
    private EntityType type;

    public GameEntity(String name, String description, EntityType entityType)
    {
        this.type = entityType;
        this.name = name;
        this.description = description;
    }

    public EntityType getEntityType() {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
