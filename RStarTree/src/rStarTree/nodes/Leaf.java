package rStarTree.nodes;

import rStarTree.dataToObject.NodeDTO;
import rStarTree.spatial.Rectangle;
import rStarTree.spatial.Point;
import Utilities.Constants;

import java.util.ArrayList;

public class Leaf extends Node {
    public ArrayList<Point> loadedChildren;

    public Leaf(int dimension) {
        generateId();
        dimensions = dimension;
        loadedChildren = new ArrayList<Point>();
        pointersToChildren = new ArrayList<Long>();
        mbr = new Rectangle(dimension);
    }

    public Leaf(NodeDTO dto, long nodeId) {
        this.nodeId = nodeId;
        this.setParentId(dto.parentId);
        dimensions = Constants.DIMENSION;
        pointersToChildren = dto.children;
        loadedChildren = new ArrayList<Point>();
        mbr = new Rectangle(dto.mbr);
    }

    //είναι φύλλο;
    @Override
    public boolean isLeaf() {
        return true;
    }

    //είναι γεμάτο;
    @Override
    public boolean isNotFull() {
        return ((pointersToChildren.size() + loadedChildren.size()) < Constants.MAX_CHILDREN);
    }

    //εισαγωγή νέου φύλλου 
    @Override
    public <T> int insert(T neo) {
        if (this.isNotFull()) {
            loadedChildren.add((Point) neo);
            mbr.update((Point) neo);
            return 1;
        }
        else return -1;
    }

    //επιστροφή mbr
    @Override
    public Rectangle returnMBR() {
        return mbr;
    }

    //θέτουμε νέο mbr
    @Override
    public void setMbr(Rectangle mbr) {
        this.mbr = mbr;
    }

    //εξαγωγή σε μορφή για αποθήκευση
    @Override
    public NodeDTO toDTO() {
        return new NodeDTO(getParentId(), true, mbr.toDTO(), pointersToChildren);
    }

    //υπάρχουν μη αποθηκευμένα σημεία;
    public boolean hasUnsavedPoints(){
        return loadedChildren.size() > 0;
    }

    //γυρνάμε σε arraylist όλα τα παιδιά 
    public ArrayList<Point> getLoadedChildren(){
        return loadedChildren;
    }
}

