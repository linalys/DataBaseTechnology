package rStarTree;

import rStarTree.dataToObject.NodeDTO;
import rStarTree.dataToObject.PointDTO;
import rStarTree.dataToObject.TreeDTO;
import rStarTree.interfaces.DiskQuery;
import rStarTree.nodes.Internal;
import rStarTree.nodes.Leaf;
import rStarTree.nodes.Node;
import Utilities.Constants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class StorageManager implements DiskQuery {
    RandomAccessFile dataStore;
    FileChannel dataChannel;

    public StorageManager() {
        try {
            dataStore = new RandomAccessFile(Constants.DATA_FILE, "rw");
            dataChannel = dataStore.getChannel();
        } catch (FileNotFoundException e) {
            System.err.println("Data File failed to be loaded/created. Exiting");
            System.exit(1);
        }
    }

    /**
     * Αποθήκευση ενός κόμβου στο δίσκο
     * Λαμβάνεται ειδική μέριμνα αν ο κόμβος είναι φύλλο ή όχι
     * @param node 
     */
    @Override
    public void saveNode(Node node) {
        if (node.isLeaf()) {
            try {
                Leaf leaf = (Leaf) node;

                if (leaf.hasUnsavedPoints()) {
                    //save unsaved points to disk first.
                    for (int i = leaf.loadedChildren.size() - 1; i >= 0; i--) {
                        leaf.pointersToChildren.add(savePoint(leaf.loadedChildren.remove(i).toDTO()));
                    }
                }

                FileOutputStream fos = new FileOutputStream(new File(constructFilename(leaf.getNodeId())));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(leaf.toDTO());
                oos.flush();

                fos.write(bos.toByteArray());
                oos.close();
                fos.close();
                bos.close();
            } catch (FileNotFoundException e) {
                System.err.println("Exception while saving node to disk");
            } catch (IOException e) {
                System.err.println("Exception while saving node to disk");
            }
        } else {
            try {
                Internal internal = (Internal) node;

                FileOutputStream fos = new FileOutputStream(new File(constructFilename(internal.getNodeId())));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(internal.toDTO());
                oos.flush();

                fos.write(bos.toByteArray());
                oos.close();
                fos.close();
                bos.close();

            } catch (FileNotFoundException e) {
                System.err.println("Exception while saving node to disk");
            } catch (IOException e) {
                System.err.println("Exception while saving node to disk");
            }
        }
    }

    /**
     * Φόρτωση ενός κόμβου στη μνήμη
     * @param nodeId
     * @return
     * @throws FileNotFoundException 
     */
    @Override
    public Node loadNode(long nodeId) throws FileNotFoundException {
        return nodeFromDisk(constructFilename(nodeId));
    }

    /**
     * Αποθήκευση χωρικού σημείου στο δίσκο
     * @param pointDTO 
     * @return 
     */
    @Override
    public long savePoint(PointDTO pointDTO) {
        try {
            long pos = dataStore.length();
            dataStore.seek(pos);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(pointDTO);
            oos.flush();

            dataChannel.write(ByteBuffer.wrap(bos.toByteArray()));
            oos.close();
            bos.close();
            return pos;
        } catch (IOException e) {
            System.err.println("Exception occurred while saving data to disk.");
            return -1;
        }
    }

    /**
     * φόρτωση ενός χωρικού σημείου από αρχείο
     * loads a SpatialPoint from dataFile
     * @param pointer 
     * @return
     */
    @Override
    public PointDTO loadPoint(long pointer) {

        try {
            dataStore.seek(pointer);
            ObjectInputStream ois = getPointObjectStream();
            PointDTO pointDTO = (PointDTO) ois.readObject();
            ois.close();
            return pointDTO;

        } catch (IOException e) {
            System.err.println("Exception occurred while loading point from disk.");
        } catch (ClassNotFoundException e) {
            System.err.println("Exception occurred while loading point from disk.");
        }
        return null;
    }

    /**
     * Φόρτωση αντικειμένου Node από τον δίσκο
     * @param filename
     * @return
     * @throws FileNotFoundException 
     */
    private Node nodeFromDisk(String filename) throws FileNotFoundException {

        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            NodeDTO dto = (NodeDTO) ois.readObject();
            ois.close();
            fis.close();

            Node result;
            if (dto.isLeaf)
                result = new Leaf(dto, nodeIdFromFilename(filename));
            else
                result = new Internal(dto, nodeIdFromFilename(filename));

            return result;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException occurred while loading node from disk");
        }
        return null;
    }

    /**
     * Αποθήκευση του R* tree στον δίσκο
     * @param tree
     * @param saveFile
     * @return 
     */
    @Override
    public int saveTree(TreeDTO tree, File saveFile) {
        int status = -1;
        try {
            if(saveFile.exists()) {
                saveFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(tree);
            oos.flush();
            oos.close();
            fos.close();
            status = 1;             // successful saveNode
        } catch (IOException e) {
            System.err.println("Error while saving Tree to " + saveFile.toURI());
        }
        return status;
    }

    /**
     * Φόρτωση του R* tree από τον δίσκο στη μνήμη
     * @param saveFile
     * @return 
     */
    @Override
    public TreeDTO loadTree(File saveFile) {
        try {
            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            return (TreeDTO) ois.readObject();

        } catch (IOException e) {
            System.err.println("Exception while loading tree from " + saveFile);
        } catch (ClassNotFoundException e) {
            System.err.println("Exception while loading tree from " + saveFile);
        }
        return null;
    }

    /**
     * Δημιουργία ονόματος αρχείου 
     * @param nodeId
     * @return 
     */
    public String constructFilename(long nodeId) {
        return Constants.TREE_DATA_DIRECTORY + "/" + Constants.NODE_FILE_PREFIX + nodeId + Constants.NODE_FILE_SUFFIX;
    }

    /**
     * Φόρτωση κόμβου από αρχείο συγκεκριμένου ονόματος
     * @param filename
     * @return 
     */
    public long nodeIdFromFilename(String filename) {
        int i2 = filename.indexOf(Constants.NODE_FILE_SUFFIX);
        assert i2 != -1;
        return Long.parseLong(filename.substring((Constants.TREE_DATA_DIRECTORY+"/"+Constants.NODE_FILE_PREFIX).length(), i2));
    }

    /**
     * Διάβασμα από Object Stream
     * @return
     * @throws IOException 
     */
    private ObjectInputStream getPointObjectStream() throws IOException {
        return new ObjectInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                return dataStore.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return dataStore.read(b, off, len);
            }
        });
    }

    /**
     * Δημιουργία κατάλογου δεδομένων
     * @param saveFile 
     */
    public void createDataDir(File saveFile) {
        File dataDir = new File(saveFile.getParentFile(), Constants.TREE_DATA_DIRECTORY);
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            if (!dataDir.mkdir()) {
                System.err.println("Failed to create data directory of the tree. Exiting..");
                System.exit(1);
            }
            System.out.println("Data directory created");
        }
    }
}

