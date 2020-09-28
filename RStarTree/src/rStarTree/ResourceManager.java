package rStarTree;

import java.io.*;
import java.util.ArrayList;


public class ResourceManager {
    private  ArrayList<String[]> toFile;
    private File data;
    public ResourceManager(){
        toFile = new ArrayList<>();
        data = new File("datafile.txt");
    }
    /**
     * Μέγεθος της arraylist 
     * @return 
     */
    private int sizeOfArrayList(){
        int totalBytes =0;
        for (String[] strings : toFile) {
            totalBytes += strings[0].length();
            totalBytes += strings[1].length();
            totalBytes += strings[2].length();
        }
        return totalBytes/32768;
    }

    /**
     * κάνουμε blocks το osm
     * @param filename
     * @throws IOException 
     */
    public void blockifyOsm(String filename) throws IOException {
        int totalBytesWritten;
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        out.write("Block " +0);
        out.newLine();
        out.write("Number of nodes:" +toFile.size());
        out.newLine();
        int numOfBlocks = sizeOfArrayList()+1;
        out.write("Number of Blocks:" +numOfBlocks);
        int node=0;
        out.newLine();
        for (int i=1;i<=numOfBlocks;i++){

            out.write("Block "+i);

            totalBytesWritten=0;
            while(totalBytesWritten<32768&&node<toFile.size()){
                out.newLine();
                totalBytesWritten+= toFile.get(node)[0].length();
                totalBytesWritten+= toFile.get(node)[1].length();
                totalBytesWritten+= toFile.get(node)[2].length();
                if(totalBytesWritten>32768){
                    break;
                }
                out.write("0 "+toFile.get(node)[0]+" "+toFile.get(node)[1]+" "+toFile.get(node)[2]);
                node++;
            }
        }
        out.close();

    }

    /**
     * φιλτράρισμα του osm για να το έχουμε σε μορφή κατανοητή για τις συναρτήσεις μας 
     * @param filename
     * @throws IOException 
     */
    public void filterOsm(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            if(line.contains("<node")) {
                ArrayList<Character> aList = new ArrayList<>();
                for (char c : line.toCharArray()) {
                    aList.add(c);
                }
                String[] temp = new String[3];
                temp[0] = "";
                temp[1] = "";
                temp[2] = "";
                int i = line.indexOf("id=\"")+4;
                while (aList.get(i) != '\"') {
                    temp[0] += aList.get(i);
                    i++;
                }
                i = line.indexOf("lat=\"")+5;
                while (aList.get(i) != '\"') {
                    temp[1] += aList.get(i);
                    i++;
                }
                i = line.indexOf("lon=\"")+5;
                while (aList.get(i) != '\"') {
                    temp[2] += aList.get(i);
                    i++;
                }
                toFile.add(temp);
            }
        }
        br.close();

    }

    /**
     * προσθήκη ερωτημάτων
     * @param name
     * @param content 
     */
    public void addQueries (String name, String content){
        File file = new File(name);

        try(FileWriter fw = new FileWriter(name, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.print(content);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    //Κλείνει ένα buffered reader
    public void closeReader(String filename)throws IOException{
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        out.close();
    }

}



