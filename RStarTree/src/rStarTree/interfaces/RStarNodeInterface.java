package rStarTree.interfaces;

import rStarTree.spatial.Rectangle;

public interface RStarNodeInterface {

    //είναι φύλλο;
    public boolean isLeaf();

    //είναι γεμάτο;
    public boolean isNotFull();

    //εισαγωγή παιδιού
    public <T> int insert(T newChild);

    //παίρνουμε το mbr
    public Rectangle returnMBR();

    //θέτουμε το mbr
    public void setMbr(Rectangle mbr);

    //δημιουργία id
    void generateId();
    
    //παίρνουμε το id του κόμβου
    long getNodeId();

    
    //θέτουμε το id του κόμβου
    void setNodeId(long nodeId);
}
