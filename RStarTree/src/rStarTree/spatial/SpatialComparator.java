package rStarTree.spatial;

import rStarTree.nodes.Node;
import Utilities.Constants;

import java.util.Comparator;

public class SpatialComparator implements Comparator {
    private int dimension;
    private int order;

    public SpatialComparator(int dimension, int coordToSort) {
        this.dimension = dimension;
        this.order = coordToSort;
    }

    
    /**
     * Σύγκριση δύο αντικειμένων - κάνουμε διάκριση αν είναι Point ή Nodes, λαμβάνουμε ειδική μέριμνα ανάλογα.
     * Ουσιαστικά ένας comparator που γυρνάει 1 για μεγαλύτερο ή -1 για μικρότερο
     * @param o1
     * @param o2
     * @return 
     */
    @Override
    public int compare(Object o1, Object o2) {
        Rectangle mbr1;
        Rectangle mbr2;
        if (o1 instanceof Point) {
            Point[] tmp = new Point[1];
            tmp[0] = (Point) o1;
            mbr1 = new Rectangle(Constants.DIMENSION, tmp);
            tmp[0] = (Point)o2;
            mbr2 = new Rectangle(Constants.DIMENSION, tmp);
        }
        else if (o1 instanceof Node) {
            mbr1 = ((Node)o1).returnMBR();
            mbr2 = ((Node)o2).returnMBR();
        } else {
            mbr1 = (Rectangle) o1;
            mbr2 = (Rectangle) o2;
        }

        int result = 0;
        if (mbr1.getPoints()[dimension][order] < mbr2.getPoints()[dimension][order])
            result = -1;

        if (mbr1.getPoints()[dimension][order] > mbr2.getPoints()[dimension][order])
            result = 1;

        return result;
    }
}

