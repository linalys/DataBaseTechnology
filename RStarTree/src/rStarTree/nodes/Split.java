package rStarTree.nodes;

import rStarTree.StorageManager;
import rStarTree.dataToObject.PointDTO;
import rStarTree.spatial.Rectangle;
import rStarTree.spatial.SpatialComparator;
import rStarTree.spatial.Point;
import Utilities.Constants;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.sort;

public class Split {
    private int dimension;
    public int bestSortOrder;
    private StorageManager disk;

    public Split(int dimension, StorageManager storageManager) {
        this.dimension = dimension;
        this.disk = storageManager;
        this.bestSortOrder = -1;
    }

    /**
     * Επιλογή φύλλου κατάλληλου, επιστρέφει το φύλλο σε μορφή αντικειμένου
     * @param startNode
     * @param newMbr
     * @return 
     */
    public Leaf chooseLeaf(Node startNode, Rectangle newMbr) {
        if(startNode.isLeaf()) {
            return (Leaf)startNode;
        }

        else {
            ArrayList<Long> childPointers = startNode.pointersToChildren;
            assert childPointers.size() > 0;
            ArrayList<Node> children = new ArrayList<Node>(childPointers.size());
            //φορτωση όλων των παιδιών
            for (long childId : childPointers) {
                try {
                    children.add(disk.loadNode(childId));
                } catch (FileNotFoundException e) {
                    System.err.println("Exception while loading node from disk. message = "+e.getMessage());
                }
            }

            //είναι τα παιδιά φύλλα;
            if (children.get(0).isLeaf()) {
                ArrayList<Double> minOverlap = new ArrayList<Double>();
                ArrayList<Node> cands = new ArrayList<Node>();

                for (Node child : children) {
                    Rectangle union = child.returnMBR().union(newMbr);
                    double deltaOverlap = 0;

                    for (Node otherChild : children) {
                        if (otherChild == child) {
                            continue;
                        }

                        deltaOverlap += union.overlap(otherChild.returnMBR()) -
                                child.returnMBR().overlap(otherChild.returnMBR());

                    }

                    if (minOverlap.size() == 0) {
                        cands.add(child);
                        minOverlap.add(deltaOverlap);
                    } else {
                        if (minOverlap.get(0) > deltaOverlap) {
                            minOverlap.removeAll(minOverlap);
                            cands.removeAll(cands);
                            minOverlap.add(deltaOverlap);
                            cands.add(child);
                        }
                        else if (minOverlap.get(0) == deltaOverlap) {
                            minOverlap.add(deltaOverlap);
                            cands.add(child);
                        }
                    }
                }

                if(cands.size() == 1)
                    return chooseLeaf(cands.get(0), newMbr);
                else{
                    ArrayList<Double> minAreas = new ArrayList<Double>();
                    ArrayList<Node> cands2 = new ArrayList<Node>();

                    double deltaV;
                    for (Node candNode : cands) {
                        deltaV = candNode.returnMBR().incrementCalculation(newMbr);
                        if(minAreas.size() == 0 || minAreas.get(0) > deltaV) {
                            minAreas.removeAll(minAreas);
                            cands2.removeAll(cands2);
                            minAreas.add(deltaV);
                            cands2.add(candNode);
                        }
                        else if (minAreas.get(0) == deltaV) {
                            minAreas.add(deltaV);
                            cands2.add(candNode);
                        }
                    }

                    if(cands2.size() == 1)
                        return chooseLeaf(cands2.get(0), newMbr);
                    else {
                        double minArea = Double.MAX_VALUE;
                        Node candidate = null;
                        for (Node candNode : cands2) {
                            double vol = candNode.returnMBR().volume();
                            if( vol < minArea ){
                                minArea = vol;
                                candidate = candNode;
                            }
                        }
                        return chooseLeaf(candidate, newMbr);
                    }
                }
            } else {
                ArrayList<Double> minAreas = new ArrayList<Double>();
                ArrayList<Node> cands = new ArrayList<Node>();

                double deltaV;
                for (Node candNode : children) {
                    deltaV = candNode.returnMBR().incrementCalculation(newMbr);
                    if(minAreas.size() == 0 || minAreas.get(0) > deltaV) {
                        minAreas.removeAll(minAreas);
                        cands.removeAll(cands);
                        minAreas.add(deltaV);
                        cands.add(candNode);
                    }
                    else if (minAreas.get(0) == deltaV) {
                        minAreas.add(deltaV);
                        cands.add(candNode);
                    }
                }

                if(cands.size() == 1)
                    return chooseLeaf(cands.get(0), newMbr);
                else {
                    double minArea = Double.MAX_VALUE;
                    Node candidate = null;
                    for (Node candNode : cands) {
                        double vol = candNode.returnMBR().volume();
                        if( vol < minArea ){
                            minArea = vol;
                            candidate = candNode;
                        }
                    }
                    return chooseLeaf(candidate, newMbr);
                }
            }
        }
    }


    /**
     * Υπολογισμός του άξονα τομής των φύλλων 
     * @param entries
     * @return 
     */
    public int chooseLeafSplitAxis(final ArrayList<Point> entries) {
        int splitAxis = 0;
        ArrayList<Point> maxSorting = (ArrayList<Point>) entries.clone();
        ArrayList<Point> minSorting = (ArrayList<Point>) entries.clone();

        double minMargin = Double.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            double margin = 0.0;
            final SpatialComparator compMin = new SpatialComparator(i, Rectangle.MIN_CORD);
            Collections.sort(minSorting, compMin);
            final SpatialComparator compMax = new SpatialComparator(i, Rectangle.MAX_CORD);
            Collections.sort(maxSorting, compMax);

            for (int k = 0; k <= (entries.size() - 2 * Constants.MIN_CHILDREN); k++) {
                Rectangle mbr1 = new Rectangle(dimension, minSorting.subList(0, Constants.MIN_CHILDREN + k));
                Rectangle mbr2 = new Rectangle(dimension, minSorting.subList(Constants.MIN_CHILDREN + k, entries.size()));

                margin += mbr1.margin() + mbr2.margin();

                mbr1 = new Rectangle(dimension, maxSorting.subList(0, Constants.MIN_CHILDREN + k));
                mbr2 = new Rectangle(dimension, maxSorting.subList(Constants.MIN_CHILDREN + k, entries.size()));
                margin += mbr1.margin() + mbr2.margin();
            }

            if (margin < minMargin) {
                splitAxis = i;
                minMargin = margin;
            }
        }
        return splitAxis;
    }

    public int chooseInternalSplitAxis(ArrayList<Node> children) {
        int splitAxis = 0;
        ArrayList<Node> maxSorting = (ArrayList<Node>) children.clone();
        ArrayList<Node> minSorting = (ArrayList<Node>) children.clone();

        // καλύτερη τιμή για συνολικό margin
        double minMargin = Double.MAX_VALUE;

        for (int i = 0; i < dimension; i++) {
            double margin = 0.0;
            // ταξινόμηση των καταχωρήσεων
            final SpatialComparator compMin = new SpatialComparator(i, Rectangle.MIN_CORD);
            Collections.sort(minSorting, compMin);
            final SpatialComparator compMax = new SpatialComparator(i, Rectangle.MAX_CORD);
            Collections.sort(maxSorting, compMax);

            for (int k = 0; k <= (children.size() - 2 * Constants.MIN_CHILDREN); k++) {
                Rectangle mbr1 = new Rectangle(dimension, minSorting.subList(0, Constants.MIN_CHILDREN + k));
                Rectangle mbr2 = new Rectangle(dimension, minSorting.subList(Constants.MIN_CHILDREN + k, children.size()));

                margin += mbr1.margin() + mbr2.margin();

                mbr1 = new Rectangle(dimension, maxSorting.subList(0, Constants.MIN_CHILDREN + k));
                mbr2 = new Rectangle(dimension, maxSorting.subList(Constants.MIN_CHILDREN + k, children.size()));
                margin += mbr1.margin() + mbr2.margin();
            }

            if (margin < minMargin) {
                splitAxis = i;
                minMargin = margin;
            }
        }
        return splitAxis;
    }

    /**
     * Επιλογή σημείου τομής για φύλλο, 
     * και θέτεται η τιμή του bestSort σε 0 ή 1 ανάλογα αν πρέπει να πραγματοποιηθεί split
     * @param entries
     * @param splitAxis
     * @return 
     */
    public int chooseLeafSplitpoint(final ArrayList<Point> entries, final int splitAxis)
    {
        int splitPoint;
        int numEntries = entries.size();

        ArrayList<Point> maxSorting = (ArrayList<Point>) entries.clone();
        ArrayList<Point> minSorting = (ArrayList<Point>) entries.clone();

        final SpatialComparator compMin = new SpatialComparator(splitAxis, Rectangle.MIN_CORD);
        Collections.sort(minSorting, compMin);
        final SpatialComparator compMax = new SpatialComparator(splitAxis, Rectangle.MAX_CORD);
        Collections.sort(maxSorting, compMax);

        // το σημείο τομής
        splitPoint = Constants.MIN_CHILDREN;
        double minOverlap = Double.MAX_VALUE;
        double volume = 0.0;
        int minEntries = Constants.MIN_CHILDREN;

        bestSortOrder = -1;

        for (int i = 0; i <= numEntries - 2 * minEntries; i++) {
            Rectangle mbr1 = new Rectangle(dimension, minSorting.subList(0, minEntries + i));
            Rectangle mbr2 = new Rectangle(dimension, minSorting.subList(minEntries + i, entries.size()));

            double currentOverlap = mbr1.overlap(mbr2);
            if (currentOverlap < minOverlap || (currentOverlap == minOverlap && (mbr1.volume() + mbr2.volume()) < volume)) {
                minOverlap = currentOverlap;
                splitPoint = minEntries + i;
                bestSortOrder = Rectangle.MIN_CORD;
                volume = mbr1.volume() + mbr2.volume();
            }
            mbr1 = new Rectangle(dimension, maxSorting.subList(0, minEntries + i));
            mbr2 = new Rectangle(dimension, maxSorting.subList(minEntries + i, entries.size()));

            currentOverlap = mbr1.overlap(mbr2);
            if (currentOverlap < minOverlap || (currentOverlap == minOverlap && (mbr1.volume() + mbr2.volume()) < volume)) {
                minOverlap = currentOverlap;
                splitPoint = minEntries + i;
                bestSortOrder = Rectangle.MAX_CORD;
                volume = mbr1.volume() + mbr2.volume();
            }
        }
        return splitPoint;
    }

    /**
     * Επιλογή σημείου τομής για εσωτερικούς κόμβους
     * @param children
     * @param splitAxis
     * @return 
     */
    public int chooseInternalSplitpoint(ArrayList<Node> children, int splitAxis) {
        int splitPoint;
        // αριθμός καταχωρήσεων
        int numEntries = children.size();

        ArrayList<Node> maxSorting = (ArrayList<Node>) children.clone();
        ArrayList<Node> minSorting = (ArrayList<Node>) children.clone();

        
        final SpatialComparator compMin = new SpatialComparator(splitAxis, Rectangle.MIN_CORD);
        Collections.sort(minSorting, compMin);
        final SpatialComparator compMax = new SpatialComparator(splitAxis, Rectangle.MAX_CORD);
        Collections.sort(maxSorting, compMax);

        
        splitPoint = Constants.MIN_CHILDREN;
        // καλύτερη τιμή για αλληλοεπικάλυψη
        double minOverlap = Double.MAX_VALUE;
        double volume = 0.0;
        int minEntries = Constants.MIN_CHILDREN;

        bestSortOrder = -1;

        for (int i = 0; i <= numEntries - 2 * minEntries; i++) {
            Rectangle mbr1 = new Rectangle(dimension, minSorting.subList(0, minEntries + i));
            Rectangle mbr2 = new Rectangle(dimension, minSorting.subList(minEntries + i, children.size()));

            double currentOverlap = mbr1.overlap(mbr2);
            if (currentOverlap < minOverlap || (currentOverlap == minOverlap && (mbr1.volume() + mbr2.volume()) < volume)) {
                minOverlap = currentOverlap;
                splitPoint = minEntries + i;
                bestSortOrder = Rectangle.MIN_CORD;
                volume = mbr1.volume() + mbr2.volume();
            }
            mbr1 = new Rectangle(dimension, maxSorting.subList(0, minEntries + i));
            mbr2 = new Rectangle(dimension, maxSorting.subList(minEntries + i, children.size()));

            currentOverlap = mbr1.overlap(mbr2);
            if (currentOverlap < minOverlap || (currentOverlap == minOverlap && (mbr1.volume() + mbr2.volume()) < volume)) {
                minOverlap = currentOverlap;
                splitPoint = minEntries + i;
                bestSortOrder = Rectangle.MAX_CORD;
                volume = mbr1.volume() + mbr2.volume();
            }
        }
        return splitPoint;
    }

    /**
     * split σε κόμβο φύλλο, επιστρέφεται το φύλλο
     * @param splittingLeaf
     * @param newPoint
     * @return
     * @throws AssertionError 
     */
    public Leaf splitLeaf(Leaf splittingLeaf, Point newPoint) throws AssertionError{
        ArrayList<Long> childPointers = splittingLeaf.pointersToChildren;
        if (childPointers.size() <= 0) {
            throw new AssertionError();
        }

        ArrayList<Point> children = new ArrayList<Point>(childPointers.size());
        
        for (long childId : childPointers) {
            PointDTO dto = disk.loadPoint(childId);
            children.add(new Point(dto));
        }

        children.add(newPoint);
        int splitAxis = chooseLeafSplitAxis(children);
        int splitPoint = chooseLeafSplitpoint(children, splitAxis);

        Object[] sorting = children.toArray();
        final SpatialComparator comp = new SpatialComparator(splitAxis, bestSortOrder);
        sort(sorting, comp);

        splittingLeaf.loadedChildren = new ArrayList<Point>();
        splittingLeaf.pointersToChildren = new ArrayList<Long>();
        Leaf newChild = new Leaf(dimension);

        Rectangle newMbr1 = new Rectangle(dimension);     //προσαρμοσμένο rectangle για φύλλο τομής
        Rectangle newMbr2 = new Rectangle(dimension);     //προσαρμοσμένο rectangle για νέο παιδί

        for (int i = 0; i < sorting.length; i++) {
            Point spatialPoint = (Point) sorting[i];
            if (i < splitPoint) {
                if (spatialPoint == newPoint) {
                    splittingLeaf.loadedChildren.add(spatialPoint);
                } else {
                    splittingLeaf.pointersToChildren.add(childPointers.get(children.indexOf(spatialPoint)));
                }
                newMbr1.update(spatialPoint);
            } else {
                if (spatialPoint == newPoint) {
                    newChild.loadedChildren.add(spatialPoint);
                } else {
                    newChild.pointersToChildren.add(childPointers.get(children.indexOf(spatialPoint)));
                }
                newMbr2.update(spatialPoint);
            }
        }
        splittingLeaf.setMbr(newMbr1);
        newChild.setMbr(newMbr2);

        disk.saveNode(splittingLeaf);
        return newChild;
    }

    /**
     * split σε εσωτερικούς κόμβους
     * @param splittingNode
     * @param node
     * @return
     * @throws FileNotFoundException 
     */
    public Node splitInternalNode(Internal splittingNode, Node node) throws FileNotFoundException {
        ArrayList<Long> childPointers = splittingNode.pointersToChildren;
        if (childPointers.size() <= 0) {
            throw new AssertionError();
        }

        ArrayList<Node> children = new ArrayList<Node>(childPointers.size());
        //φόρτωση των παιδιών
        for (long childNodeId : childPointers) {
            children.add(disk.loadNode(childNodeId));
        }

        children.add(node);
        int splitAxis = chooseInternalSplitAxis(children);
        int splitPoint = chooseInternalSplitpoint(children, splitAxis);

        Object[] sorting = children.toArray();
        final SpatialComparator comp = new SpatialComparator(splitAxis, bestSortOrder);
        sort(sorting, comp);

        splittingNode.pointersToChildren = new ArrayList<Long>();
        Internal createdNode = new Internal(dimension);

        Rectangle newMbr1 = new Rectangle(dimension);
        Rectangle newMbr2 = new Rectangle(dimension);

        for (int i = 0; i < sorting.length; i++) {
            Node childNode = (Node) sorting[i];
            if (i < splitPoint) {
                splittingNode.pointersToChildren.add(childNode.getNodeId());
                childNode.setParentId(splittingNode.getNodeId());
                newMbr1.update(childNode.returnMBR());
            } else {
                createdNode.pointersToChildren.add(childNode.getNodeId());
                childNode.setParentId(createdNode.getNodeId());
                newMbr2.update(childNode.returnMBR());
            }
            disk.saveNode(childNode);            //αποθήκευση
        }

        splittingNode.setMbr(newMbr1);
        createdNode.setMbr(newMbr2);

        disk.saveNode(splittingNode);
        return createdNode;
    }
}

