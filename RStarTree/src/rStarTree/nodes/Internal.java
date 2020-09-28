package rStarTree.nodes;

import rStarTree.dataToObject.NodeDTO;
import rStarTree.spatial.Rectangle;
import Utilities.Constants;

import java.util.ArrayList;

public class Internal extends Node {


    public Internal(int dimension) {
        generateId();
        dimensions = dimension;
        pointersToChildren = new ArrayList<Long>(Constants.MAX_CHILDREN);
        mbr = new Rectangle(dimension);
    }

    public Internal(NodeDTO dto, long nodeId) {
        this.nodeId = nodeId;
        this.setParentId(dto.parentId);
        this.pointersToChildren = dto.children;
        this.mbr = new Rectangle(dto.mbr);
    }

    // είναι φύλλο;
    @Override
    public boolean isLeaf() {
        return false;
    }

    //είναι γεμάτο;
    @Override
    public boolean isNotFull() {
        return pointersToChildren.size() < Constants.MAX_CHILDREN;
    }

    /**
     * Εισαγωγή νέου παιδιού εσωτερικού κόμβου
     * @param <T>
     * @param newChild
     * @return 
     */
    @Override
    public <T> int insert(T newChild) {
        if (this.isNotFull() && newChild instanceof Node) {
            ((Node) newChild).setParentId(this.nodeId);
            pointersToChildren.add(((Node) newChild).getNodeId());
            mbr.update(((Node) newChild).returnMBR());
            return 1;
        }
        else return -1;
    }

    //επιστροφή του mbr
    @Override
    public Rectangle returnMBR() {
        return mbr;
    }

    //θέτουμε το νέο mbr
    @Override
    public void setMbr(Rectangle mbr) {
        this.mbr = mbr;
    }
    /**
     * Μετατροπή σε μορφή για αποθήκευση
     * @return 
     */

    @Override
    public NodeDTO toDTO() {
        return new NodeDTO(getParentId(), false, mbr.toDTO(), pointersToChildren);
    }
}

