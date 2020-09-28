package rStarTree.spatial;

import rStarTree.nodes.Node;
import rStarTree.dataToObject.MbrDTO;
import rStarTree.interfaces.DtoConvertible;
import Utilities.Constants;

import java.util.List;

public class Rectangle implements DtoConvertible {
    private int dimensions;
    private double[][] points;
    public static final int MAX_CORD = 0;
    public static final int MIN_CORD = 1;



    //Επιστρέφεται ο πίνακας με τα points ενός rectangle
    public double[][] getPoints() {
        return points;
    }

    //θέτουμε έναν νέο πίνακα με τα points
    public void setPoints(double[][] points) {
        this.points = points;
    }

    public Rectangle(int dimension) {
        this.dimensions = dimension;
        points = new double[dimension][2];
    }

    public Rectangle(int dimension, Point[] points) {
        this.dimensions = dimension;
        this.points = new double[dimension][2];

        update(points);
    }

    public <T> Rectangle(int dimension, List<T> points) {
        this.dimensions = dimension;
        this.points = new double[dimension][2];

        update(points);
    }

    public Rectangle(MbrDTO dto) {
        this.dimensions = Constants.DIMENSION;
        this.points = dto.points;
    }

    public Rectangle(double[] cords) {
        this.dimensions = cords.length;
        points = new double[dimensions][2];
        for (int i = 0; i < dimensions; i++) {
            points[i][MAX_CORD] = cords[i];
            points[i][MIN_CORD] = cords[i];
        }
    }

    /**
     * Κάνουμε update στο rectangle προσθέτωντας ένα νέο σημείο
     * @param newPoint 
     */
    public void update(Point newPoint) {
        Point[] newPoints = new Point[1];
        newPoints[0] = newPoint;
        update(newPoints);
    }

    /**
     * Κάνουμε update στο rectangle προσθέτωντας έναν πίνακα σημείων
     * @param newPoints 
     */
    private void update(Point[] newPoints) {
        for (Point newPoint : newPoints) {
            double[] cord = newPoint.getCords();
            assert cord.length == dimensions;
            for (int i = 0; i < cord.length; i++) {
                if (points[i][MAX_CORD] == 0 || points[i][MAX_CORD] < cord[i]) {
                    points[i][MAX_CORD] = cord[i];
                }
                if (points[i][MIN_CORD] == 0 || points[i][MIN_CORD] > cord[i]) {
                    points[i][MIN_CORD] = cord[i];
                }
            }
        }
    }

    /**
     * Κάνουμε update στο rectangle προσθέτωντας μία λίστα σημείων
     * @param <T>
     * @param newPoints 
     */
    private <T> void update(List<T> newPoints) {
        if (newPoints.get(0) instanceof Point) {
            for (T newPoint : newPoints) {
                double[] cord = ((Point) newPoint).getCords();
                assert cord.length == dimensions;
                for (int i = 0; i < cord.length; i++) {
                    if (points[i][MAX_CORD] == 0 || points[i][MAX_CORD] < cord[i]) {
                        points[i][MAX_CORD] = cord[i];
                    }
                    if (points[i][MIN_CORD] == 0 || points[i][MIN_CORD] > cord[i]) {
                        points[i][MIN_CORD] = cord[i];
                    }
                }
            }
        } else if (newPoints.get(0) instanceof Node) {
            for (T node : newPoints) {
                update(((Node) node).returnMBR());
            }
        }
    }

    /**
     * Κάνουμε update στο rectangle προσθέτωντας ένα νέο Rectangle μέσα στο υπάρχον
     * Πολύ χρήσιμο για τα splits αργότερα
     * @param addedRegion 
     */
    public void update(Rectangle addedRegion) {
        double[][] newPoints = addedRegion.getPoints();
        assert newPoints.length == dimensions;
        for (int j = 0; j < dimensions; j++) {
            if (points[j][MAX_CORD] == 0 || points[j][MAX_CORD] < newPoints[j][MAX_CORD]) {
                points[j][MAX_CORD] = newPoints[j][MAX_CORD];
            }
            if (points[j][MIN_CORD] == 0 || points[j][MIN_CORD] > newPoints[j][MIN_CORD]) {
                points[j][MIN_CORD] = newPoints[j][MIN_CORD];
            }
        }
    }

    /**
     * Βρίσκουμε τομές μεταξύ δύο rectangles.
     * @param otherMBR 
     * @return 
     */
    public Rectangle getCrossing(Rectangle otherMBR) {
        double[][] crossingPoints = new double[dimensions][2];
        double[][] newPoints = otherMBR.getPoints();
        assert newPoints.length == dimensions;

        boolean crossingExists = true;
        for (int i = 0; i < dimensions; i++) {
            if ((points[i][MAX_CORD] < newPoints[i][MIN_CORD]) || (points[i][MIN_CORD] > newPoints[i][MAX_CORD])) {
                crossingExists = false;
                break;
            }
            crossingPoints[i][MAX_CORD] = Math.min(newPoints[i][MAX_CORD], points[i][MAX_CORD]);
            crossingPoints[i][MIN_CORD] = Math.max(newPoints[i][MIN_CORD], points[i][MIN_CORD]);
        }

        if (!crossingExists) {
            return null;
        }
        Rectangle crossing = new Rectangle(dimensions);
        crossing.setPoints(crossingPoints);
        return crossing;
    }

    /**
     * Υπολογίζεται κατά πόσο αυξένεται ο όγκος (volume) ενός rectangle
     * @return
     */
    public double incrementCalculation(Rectangle otherMBR) {
        Rectangle tmpMbr = new Rectangle(dimensions);
        tmpMbr.setPoints(points);
        tmpMbr.update(otherMBR);

        return tmpMbr.volume() - this.volume();
    }

    /**
     * Υπολογίζεται ο όγκος(volume) ενός rectangle
     * @return
     */
    public double volume() {
        double volume = 1;
        for (double[] point : points) {
            volume *= point[MAX_CORD] - point[MIN_CORD];
        }
        return volume;
    }
    
    /**
     * Υπολογισμός της διαγωνίου του rectangle
     * @return 
     */
    public double diagonal(){
        double diag=0;
        for(double[] point : points){
            diag +=  Math.pow(point[MAX_CORD] - point[MIN_CORD],2);
        }
        return  Math.sqrt(diag);
    }

    /**
     * Υπολογίζεται το περιθώριο (margin) ενός rectangle.
     * @return το περιθώριο του mbr
     */
    public double margin() {
        double margin = 0;
        for (double[] point : points) {
            margin += point[MAX_CORD] - point[MIN_CORD];
        }
        return margin;
    }

    /**
     * Υπολογίζεται ο όγκος της επικάλυψης μεταξύ δύο 
     * @param mbr
     * @return 
     */
    public double overlap(Rectangle mbr) {
        Rectangle crossing = this.getCrossing(mbr);
        if (crossing == null) {
            return 0;
        }
        return crossing.volume();
    }

    /**
     * Υπολογίζεται η ένωση (union) δύο rectangle
     * @param mbr 
     * @return
     */
    public Rectangle union(Rectangle mbr) {
        if (this.dimensions != mbr.dimensions)
            throw new IllegalArgumentException("Different dimensions.");

        double[][] otherPoints = mbr.getPoints();
        double[][] unionPoints = new double[dimensions][2];

        for (int i = 0; i < this.dimensions; i++) {
            unionPoints[i][MIN_CORD] = Math.min(this.points[i][MIN_CORD], otherPoints[i][MIN_CORD]);
            unionPoints[i][MAX_CORD] = Math.max(this.points[i][MAX_CORD], otherPoints[i][MAX_CORD]);
        }
        Rectangle union = new Rectangle(dimensions);
        union.setPoints(unionPoints);
        return union;
    }

    /**
     * Μετατροπή ενός rectangle σε DTO για την εγγραφή του στο δίσκο
     * @return 
     */
    @Override
    public MbrDTO toDTO() {
        return new MbrDTO(points);
    }
}

