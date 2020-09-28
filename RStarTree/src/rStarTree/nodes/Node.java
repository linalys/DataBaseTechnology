package rStarTree.nodes;

import rStarTree.interfaces.DtoConvertible;
import rStarTree.spatial.Rectangle;
import rStarTree.interfaces.RStarNodeInterface;
import Utilities.Utilities;

import java.util.ArrayList;

public abstract class Node implements DtoConvertible, RStarNodeInterface{
    protected long nodeId = -1;
    protected static int dimensions;
    protected Rectangle mbr;
    public ArrayList<Long> pointersToChildren;
    private Long parentId;

    //το id του γονιού
    public Long getParentId() {
        return parentId;
    }

    //παραγωγή id
    @Override
    public void generateId() {
        if (nodeId == -1) {
            nodeId = Utilities.getRandomId();
            if(nodeId < 0)
                nodeId = -1 * nodeId;
        }

    }
    //επιστρέφει δεικτες στα παιδιά
    public ArrayList<Long> returnChildPointers(){
        return this.pointersToChildren;
    }

    //ορίζει το id του γονέα
    public void setParentId(Long id) {
        this.parentId = id;
    }

    //πάρε το id του κόμβου
    @Override
    public long getNodeId() {
        generateId();
        return nodeId;
    }

    //θέσε id του κόμβου
    @Override
    public void setNodeId(long id){
        this.nodeId = id;
    }
}
