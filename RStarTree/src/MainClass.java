import rStarTree.RStarTree;
import rStarTree.ResourceManager;
import rStarTree.StorageManager;
import rStarTree.dataToObject.TreeDTO;
import rStarTree.nodes.Leaf;
import rStarTree.nodes.Node;
import rStarTree.spatial.Point;
import Utilities.Utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class MainClass {

    private static String[] settings;
    private static String filename;
    private RStarTree tree;
    private int dimensions;
    private String inputFile;
    private List<Long> rangeRuntime;
    private List<Long> knnRuntime;
    private ArrayList <Long> list;
    private HashMap<Long,Boolean> copies;
    
    /**
     * Η βασικότερη συνάρτηση main, ορίζει τα settings για τη κλάση που θα κληθεί
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException{
        Scanner scan = new Scanner(System.in);
        filename = "auth.osm";
        System.out.println("Your filename is: " + filename);


        ResourceManager rm = new ResourceManager();
        rm.filterOsm(filename);
        rm.blockifyOsm(filename);


        System.out.println("Enter number of dimensions.");
        int dim = scan.nextInt();
        String line = "\n";

        int exit = 0;
        while(exit!=3) {
            System.out.println("Press: ");
            System.out.println("#1 for range search\n#2 for K-NN search\n#3 to continue");
            System.out.println("The option #3 will not work if you don't select the query you want to do");
            System.out.println("If you have selected Range or K-NN press #3");
            exit = scan.nextInt();
            System.out.println(exit);
            switch (exit)
            {
                case 1: {
                    line += "1 ";
                    for (int i = 0; i < dim; i++) {
                        System.out.println("Enter the point's coordinations [with space between them]");
                        String s = scan.next();
                        line += s;
                        line += " ";
                    }
                    System.out.println("Enter the range");
                    String s = scan.next();
                    line += s;
                    rm.addQueries("datafile.txt", line);
                    line = "\n";
                    break;
                }
                case 2:{
                    line += "2 ";
                    for (int i = 0; i < dim; i++) {
                        System.out.println("Enter the point's coordinations [with space between them]");
                        String s = scan.next();
                        line += s;
                        line += " ";
                    }
                    System.out.println("Enter the number of neighbors");
                    String s = scan.next();
                    line += s;
                    rm.addQueries("datafile.txt", line);
                    line = "\n";
                    break;
                }
                case 3:
                    break;
            }
        }

        settings = new String[2];
        settings[0] = "datafile.txt";
        settings[1] = String.valueOf(dim);
        MainClass handler = new MainClass(settings);

        System.out.println("");
        System.out.println("*******************");
        System.out.println("Input processing and building R*-tree\nThis might take a while\nWait...");
        System.out.println("*******************");
        handler.processInput();
        System.out.println("Done!");

    }

    /**
     * Η κλάση της Main, αρχικοποιούνται όλα 
     * @param settings 
     */
    public MainClass(String[] settings) {
        if(settings.length == 2){
            this.inputFile = settings[0];
            this.dimensions = Integer.parseInt(settings[1]);
        } else {
            this.printUsage();
            System.exit(1);
        }
        tree = new RStarTree(dimensions);
        this.rangeRuntime = new ArrayList<Long>();
        this.knnRuntime = new ArrayList<Long>();
        copies = new HashMap<>();
    }
    
    
    /**
     * Δημιουργία του index file, με ειδική διαχείριση για πιθανά exceptions
     */
    private void  makeIndexFile(){

        FileInputStream fis = null;
        try {
            fis = new FileInputStream("MyRStarTree.rstar");
            ObjectInputStream buffR = new ObjectInputStream(fis);
            TreeDTO treeSaved = (TreeDTO) buffR.readObject();
            StorageManager treeManager = tree.returnManager();

            Node root = treeManager.loadNode(treeSaved.pointerToRoot);

            BufferedWriter buffW = new BufferedWriter(new FileWriter("indexfile.txt"));
            list = new ArrayList<>();
            buffW.write("Root Node ID : " + root.getNodeId() + " ,children IDs are :" );
            buffW.newLine();
            ArrayList <Long> children = root.returnChildPointers();
            for (Long child : children) {


                Node childNode = treeManager.loadNode(child);
                buffW.write("ID : " + childNode.getNodeId());
                buffW.newLine();
                list.add(childNode.getNodeId());
            }
            int i=0;
            do {
                writeNode(treeManager.loadNode(list.get(i)),treeManager,buffW);
                i++;
            }while (list.size()>i);
            buffW.close();
            buffR.close();



        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    
    /**
     * I/O συνάρτηση, γράφει σε αρχείο τους κόμβους.
     * Μπορεί να πετάξει I/O exception λόγω της αλληλεπίδρασης με αρχεία
     * @param node
     * @param treeManager
     * @param buffW
     * @throws IOException 
     */
    private void writeNode(Node node,StorageManager treeManager ,BufferedWriter buffW) throws IOException {
        if(node.isLeaf()){
            Leaf leaf = (Leaf)node;

            buffW.write("Leaf ID : " + leaf.getNodeId() +  " ,points are :");
            List<Point> pointsOfInterest = new ArrayList<>();
            double[][] points = leaf.returnMBR().getPoints();
            double[] center = new double[points.length];
            for(int i=0; i<points.length;i++){

                center[i] = (points[i][0]+points[i][1])/2;
            }
            //σημείο που αναπαριστά το κέντρο
            Point searchCenter = new Point(center);


            pointsOfInterest =  tree.rangeSearch(searchCenter, leaf.returnMBR().diagonal());
            for (Point spatialPoint : pointsOfInterest) {
                    buffW.newLine();
                    buffW.write("ID : " + spatialPoint.getOid() + " ");
                    buffW.write("Coords are :");
                    for (double cord : spatialPoint.getCords()) {
                        buffW.write(" " + cord);
                    }
            }


        }
        else
        {

            ArrayList<Long> children = new ArrayList<>();
            children = node.returnChildPointers();
            buffW.write("Node ID : " + node.getNodeId() +  " ,children IDs are :");
            for (Long child : children) {
                Node childNode = treeManager.loadNode(child);
                buffW.write("ID : " + childNode.getNodeId() );
                buffW.newLine();
                list.add(childNode.getNodeId());

            }
        }
        buffW.newLine();



    }

    /**
     * Επεξεργάζεται τα input του χρήστη από το πληκτρολόγιο 
     * Τα δίνει μορφή ώστε να μπορούν να αξιοποιηθούν από τις κλάσεις και τις συναρτήσεις μας
     * 
     */
    protected void processInput() {
        long  oid, begin, end;
        float typeOfOperation, k;
        double range;
        double[] point;
        int lineNum = 0;

        try {
            BufferedReader input =  new BufferedReader(new FileReader(this.inputFile));
            String line;
            String[] lineSplit;
            input.readLine();
            input.readLine();
            input.readLine();

            while ((line = input.readLine()) != null) {
                if(isBlockLine(line)){

                    continue;
                }
                lineNum++;
                lineSplit = line.split(" ");
                typeOfOperation = Float.parseFloat(lineSplit[0]);

                switch ((int)typeOfOperation) {
                    case 0:
                    {       //εισαγωγή
                        try {
                            if (lineSplit.length != (this.dimensions + 2)) {
                                throw new AssertionError();
                            }


                            oid = Long.parseLong(lineSplit[1]);
                            point = extractPoint(lineSplit, 2);

                            tree.insert(new Point(point, oid));

                            break;

                        } catch (Exception e) {
                            System.err.println("Exception on line " + lineNum +
                                    ".Insertion Skipped. message: "+e.getMessage());
                            break;
                        }
                        catch (AssertionError error){
                            System.err.println("Error on line " + lineNum +
                                    ".Insertion Skipped. message: "+ error.getMessage());
                            break;
                        }
                    }


                    case 1:
                    {   //περιοχή
                        try{
                            if (lineSplit.length != this.dimensions + 2) {
                                throw new AssertionError();
                            }

                            point = extractPoint(lineSplit, 1);
                            range = Double.parseDouble(lineSplit[this.dimensions + 1]);
                            Point center = new Point(point);

                            begin = System.currentTimeMillis();
                            List<Point> result = tree.rangeSearch(center, range);
                            end = System.currentTimeMillis();

                            System.out.println("Range Search(" + range + ", " + center + "): " + Utilities.PointListToString(result));

                            System.out.println("For range R= " + range + " the time taken is: " + (end - begin) + " ms.\n");

                        } catch (Exception e) {
                            System.err.println("Exception on line " + lineNum +
                                    ".Range search skipped. message: "+e.getMessage());
                        }
                        catch (AssertionError error){
                            System.err.println("Error while processing line " + lineNum +
                                    ".Range search skipped. message: "+error.getMessage());
                        }
                        break;
                    }

                    case 2:
                    {   //k-γείτονες 
                        try{
                            if (lineSplit.length != this.dimensions + 2) {
                                throw new AssertionError();
                            }

                            point = extractPoint(lineSplit, 1);
                            k = Float.parseFloat(lineSplit[this.dimensions + 1]);
                            Point center = new Point(point);

                            begin = System.currentTimeMillis();
                            List<Point> result = tree.knnSearch(center, (int)k);
                            end = System.currentTimeMillis();

                            System.out.println("Knn Search(" + k + ", " + center + "): " + Utilities.PointListToString(result));

                            System.out.println("For k= " + k + " nearest neighbors the time taken is: " + (end - begin) + " ms.\n");

                        } catch (Exception e) {
                            System.err.println("Exception on line " + lineNum +
                                    ".knn search skipped. message: "+e.getMessage());
                        }
                        catch (AssertionError error){
                            System.err.println("Error while processing line " + lineNum +
                                    ".knn search skipped. message: "+error.getMessage());
                        }
                        break;
                    }

                    default:
                        System.err.println("Wrong type of query" + typeOfOperation + " at line " + lineNum + ". Skipped");
                        break;
                }
            }
            input.close();
            tree.save();
            makeIndexFile();

        }
        catch (Exception e) {

            System.err.println("Error while reading input file. Line " + lineNum + " Skipped\nError Details:");
        }
    }
    /**
     * Ελέγχει αν υπάρχει block
     * @param line
     * @return 
     */
    private boolean isBlockLine(String line){
        String[] isBlock = line.split(" ");

        if(isBlock[0].equals("Block")){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Εξαγωγή σημείου x διαστάσεων
     * @param points
     * @param startPos
     * @return
     * @throws NumberFormatException 
     */
    private double[] extractPoint(String[] points, int startPos) throws NumberFormatException
    {
        double[] tmp = new double[this.dimensions];
        for (int i = startPos, lineSplitLength = points.length;
             ((i < lineSplitLength) && (i < (startPos + this.dimensions))); i++)
        {
            tmp[i-startPos] = Double.parseDouble(points[i]);
        }
        return tmp;
    }

    protected void printUsage() {
        System.err.println("Usage: "+ this.getClass().getSimpleName() +
                " <path to input file> <dimension of points> [output file].");
    }



}
