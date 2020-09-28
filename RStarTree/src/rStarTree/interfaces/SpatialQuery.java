package rStarTree.interfaces;

import rStarTree.spatial.Point;

import java.util.List;

public interface SpatialQuery {
    /**
     * εισαγωγή σημείου 
     * @param point
     * @return
     */
    int insert(Point point);

    /**
     * range search
     * @param center
     * @param range
     * @return 
     */
    List<Point> rangeSearch(Point center, double range);

    /**
     * knn search
     * @param center
     * @param k
     * @return 
     */
    List<Point> knnSearch(Point center, int k);
}
