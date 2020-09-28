package Utilities;

import rStarTree.spatial.Point;

import java.util.List;

public class Utilities {
    private static long idCounter = 1;

    /**
     * Aύξηση μετρητή για id
     * @return 
     */
    public static synchronized long getRandomId() {
        return idCounter++;
    }

    /**
     * Η λίστα με τα σημεία σε string
     * @param list
     * @return 
     */
    public static String PointListToString(List<Point> list) {
        String result = "";
        for (Point point : list) {
            result += point.toString() +",\n";
        }
        return result;
    }
}
