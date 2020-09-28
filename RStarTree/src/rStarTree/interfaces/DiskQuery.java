package rStarTree.interfaces;

import rStarTree.nodes.Node;
import rStarTree.dataToObject.PointDTO;
import rStarTree.dataToObject.TreeDTO;

import java.io.File;
import java.io.FileNotFoundException;

public interface DiskQuery {
    //αποθήκευση κόμβου
    void saveNode(Node node);
    
    //φόρτωση αντικείμενου κομβου
    Node loadNode(long nodeId) throws FileNotFoundException;

    //αποθήκευση σημείου
    long savePoint(PointDTO pointDTO);

    //φόρτωση σημείου
    PointDTO loadPoint(long pointer);

    //αποθήκευση δένδρου
    int saveTree(TreeDTO tree, File saveFile);

    //φόρτωση αντικειμένου δένδρου
    TreeDTO loadTree(File saveFile);
}
