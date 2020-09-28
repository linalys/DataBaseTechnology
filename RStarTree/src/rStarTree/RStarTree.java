package rStarTree;

import rStarTree.dataToObject.PointDTO;
import rStarTree.dataToObject.TreeDTO;
import rStarTree.interfaces.DtoConvertible;
import rStarTree.interfaces.SpatialQuery;
import rStarTree.nodes.Internal;
import rStarTree.nodes.Leaf;
import rStarTree.nodes.Node;
import rStarTree.nodes.Split;
import rStarTree.spatial.Rectangle;
import rStarTree.spatial.Point;
import Utilities.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RStarTree implements SpatialQuery, DtoConvertible {

    private int dimension;
    private File saveFile;
    private StorageManager storage;
    private Node root;
    private long pointerToRoot = -1;
    private Split splitManager;

    private ArrayList<Point> rangeSearchResultList;
    private List<Point> knnSearchResultList;

    public RStarTree(int dimension) {
        this.dimension = dimension;
        this.saveFile = new File(Constants.TREE_FILE);
        this.storage = new StorageManager();
        this.splitManager = new Split(dimension, storage);

        storage.createDataDir(saveFile);
        setCapacities();
    }

    /**
     * Αρχικοποίηση κάποιων σταθερών
     */
    private void setCapacities(){
        Constants.DIMENSION = dimension;
        Constants.MAX_CHILDREN = 10;
        Constants.MIN_CHILDREN = 4;
    }
    public StorageManager returnManager(){
        return this.storage;
    }

    

    /**
     * Εισαγωγή και αποθήκευση σημείου στην δευτερεύουσα μνήμη.
     * @param point
     * @return 
     */
    @Override
    public int insert(Point point) {
        Leaf leaf = chooseLeaf(point);

        if (leaf.isNotFull()) {
            leaf.insert(point);
            storage.saveNode(leaf);
            //προσαρμογή ρίζας
            if (leaf.getNodeId() == pointerToRoot) {
                root = leaf;
            }
            adjustParentOf(leaf);
            return 1;
        } else {
            return leafOverflowTreatment(leaf, point);
        }
    }

    /**
     * τοποθετεί έναν κόμβο εκεί που δείχνει ο pointer για τα nodes.
     * @param nodePointer
     * @param nodeToInsert
     * @return
     */
    private int insertAt(Long nodePointer, Node nodeToInsert) {
        storage.saveNode(nodeToInsert);
        Internal target = (Internal) loadNode(nodePointer);

        if (target.isNotFull()) {
            target.insert(nodeToInsert);

            if (target.getNodeId() == pointerToRoot) {
                root = target; // αλλάζει η ρίζα
            }

            storage.saveNode(target);
            adjustParentOf(target);
            return 1; // επιτυχής εισαγωγή
        } else {
            return internalOverflowTreatment(target, nodeToInsert);
        }
    }


    /**
     * range query για ένα σημείο κεντρικό
     * @param point 
     * @param range 
     * @return 
     */
    @Override
    public List<Point> rangeSearch(Point point, double range) {

        double[] points = point.getCords();
        double[][] mbrPoints = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            mbrPoints[i][0] = points[i] + (float) range;
            mbrPoints[i][1] = points[i] - (float) range;
        }
        Rectangle area = new Rectangle(dimension);
        area.setPoints(mbrPoints);

        rangeSearchResultList = new ArrayList<Point>();
        loadRoot();
        doRangeSearch(root, area);
        return rangeSearchResultList;
    }

    /**
     * Πραμγατοποίηση range search με αναδρομικό τρόπο χρησιμοποιώντας τις βοηθητικές συναρτήσεις
     * @param start
     * @param searchRegion 
     */
    private void doRangeSearch(Node start, Rectangle searchRegion) {
        Rectangle crossing = start.returnMBR().getCrossing(searchRegion);
        if (crossing != null) {
            if (start.isLeaf()) {
                for (Long pointer : start.pointersToChildren) {
                    PointDTO dto = storage.loadPoint(pointer);
                    Point point = new Point(dto);
                    Rectangle pointMbr = new Rectangle(dto.coords);

                    if(pointMbr.getCrossing(searchRegion) != null)
                        rangeSearchResultList.add(point);
                }
            }
            else {
                for (Long pointer : start.pointersToChildren) {
                    try {
                        Node childNode = storage.loadNode(pointer); //αναδρομή
                        doRangeSearch(childNode, searchRegion);

                    } catch (FileNotFoundException e) {
                        System.err.println("Error, can't load node.");
                    }
                }
            }
        }
    }

    /**
     * k-nn query με βάση κάποια κενρτικό σημείο
     * @param centralPoint 
     * @param k
     * @return
     */
    @Override
    public List<Point> knnSearch(Point centralPoint, int k) {
        loadRoot();
        doknnSearch(root, centralPoint, k, 1);
        rangeSearchResultList = new ArrayList<Point>();
        return knnSearchResultList;
    }

    /**
     * Πραμγατοποίηση k-nn search με αναδρομικό τρόπο χρησιμοποιώντας τις βοηθητικές συναρτήσεις
     * @param start
     * @param center
     * @param k
     * @param range 
     */
    private void doknnSearch(Node start, Point center, int k, float range) {
        rangeSearchResultList = new ArrayList<Point>();

        double[] points = center.getCords();
        double[][] mbrPoints = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            mbrPoints[i][0] = points[i] + range;
            mbrPoints[i][1] = points[i] - range;
        }
        Rectangle searchArea = new Rectangle(dimension);
        searchArea.setPoints(mbrPoints);

        doRangeSearch(start, searchArea);

        if (rangeSearchResultList.size() < k) {
            doknnSearch(start, center, k, 2 * range); // αναδρομή
        } else {
            final Point fcenter = center;
            Comparator<? super Point> paramComparator = new Comparator<Point>() {
                @Override
                public int compare(Point point1, Point point2) {
                    float deltaDist = fcenter.distance(point1) - fcenter.distance(point2);
                    if(deltaDist == 0)
                        return 0;
                    else
                        return (int)(deltaDist /(Math.abs(deltaDist)));
                }
            };
            Collections.sort(rangeSearchResultList, paramComparator);
            knnSearchResultList = rangeSearchResultList.subList(0, k);
        }
    }

    /**
     * Ειδική αναφορά για overflow στα φύλλα που οδηγεί σε ανάλογο split
     * @param target
     * @param point
     * @return 
     */
    private int leafOverflowTreatment(Leaf target, Point point) {
        try {
            splitLeafNode(target, point);
            return 1;
        } catch (AssertionError e) {
            return -1;
        }
    }

    /**
     * Ειδική αναφορά για overflow στους εσωτερικούς κόμβους που οδηγεί σε ανάλογο split
     * @param fullNode
     * @param newChild
     * @return 
     */
    private int internalOverflowTreatment(Internal fullNode, Node newChild) {
        try {
            splitInternalNode(fullNode, newChild);
            return 1;
        } catch (AssertionError e) {
            return -1;
        }
    }

    /**
     * Split ενός φύλλου
     * @param splittingLeaf
     * @param newPoint
     */
    private void splitLeafNode(Leaf splittingLeaf, Point newPoint) throws AssertionError {
        Leaf newChild = splitManager.splitLeaf(splittingLeaf, newPoint);
        if (splittingLeaf.getNodeId() == pointerToRoot) {
            root = splittingLeaf;
            createRoot(newChild);
        } else {
            newChild.setParentId(splittingLeaf.getParentId());
            insertAt(splittingLeaf.getParentId(), newChild);
        }
    }

    /**
     * Split ενός εσωτερικού κόμβου
     * @param splittingNode
     * @param node
     */
    private void splitInternalNode(Internal splittingNode, Node node) {
        Node newNode;
        try {
            newNode = splitManager.splitInternalNode(splittingNode, node);
            if (splittingNode.getNodeId() == pointerToRoot) {
                root = splittingNode;
                createRoot(newNode);
            } else {
                newNode.setParentId(splittingNode.getParentId());
                insertAt(splittingNode.getParentId(), newNode);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Can't lode node from disk. message: "+e.getMessage());
        }
    }

    /**
     * Δημιουργία καινούργιας ρίζας.
     * @param newNode 
     */
    private void createRoot(Node newNode) {
        Internal newRoot = new Internal(dimension);
        newRoot.setParentId(newRoot.getNodeId());
        newRoot.insert(root);
        newRoot.insert(newNode);
        storage.saveNode(root);
        storage.saveNode(newNode);
        storage.saveNode(newRoot);
        root = newRoot;
        pointerToRoot = newRoot.getNodeId();
    }

    /**
     * Βρίσκει το φύλλο που θα μπει το σημείο
     * @param newPoint
     * @return
     */
    private Leaf chooseLeaf(Point newPoint) {
        loadRoot();
        Point[] tmp = new Point[1];
        tmp[0] = newPoint;
        return splitManager.chooseLeaf(root, new Rectangle(dimension, tmp));
    }

    /**
     * Αλλαγή στους γονείς των κόμβων με αναδρομή τρόπο
     * @param target
     */
    private void adjustParentOf(Node target) {
        if (target.getNodeId() != pointerToRoot) {
            Node parent = loadNode(target.getParentId());
            Rectangle mbr = parent.returnMBR();
            mbr.update(target.returnMBR());
            parent.setMbr(mbr);
            storage.saveNode(parent);
            if (parent.getNodeId() == pointerToRoot) {
                root = parent;
            }
            adjustParentOf(parent);
        }
    }

    /**
     * load ρίζα από το δίσκο
     */
    private void loadRoot() {
        if (root == null) {
            //adeio
            root = loadNode(pointerToRoot);
            if (root == null)
            {
                root = new Leaf(dimension);
                root.setParentId(root.getNodeId());
            }
            pointerToRoot = root.getNodeId();
        }
    }

    /**
     * load κόμβων από τον δίσκο
     * @param nodeId
     * @return 
     */
    public Node loadNode(long nodeId) {
        //ελέγχουμε αν υπάρχει ο κόμβος, αν δεν υπάρχει το id του είναι -1
        if (nodeId != -1) {
            try {
                if (nodeId == pointerToRoot) {
                    loadRoot();
                    return root;
                } else {
                    return storage.loadNode(nodeId);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error while loading R* Tree node from file " + storage.constructFilename(nodeId));
            }
        }
        return null;
    }

    /**
     * save το δένδρο στον δίσκο
     * @return 
     */
    public int save() {
        return storage.saveTree(this.toDTO(), saveFile);
    }

    /**
     * Μετατροπή του δένδρου σε αντικείμενο και μετά εγγραφή του στον δίσκο
     * @return 
     */
    @Override
    public TreeDTO toDTO() {
        return new TreeDTO(dimension, Constants.PAGESIZE, pointerToRoot);
    }
}

