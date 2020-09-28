package rStarTree.spatial;

import rStarTree.dataToObject.PointDTO;
import rStarTree.interfaces.DtoConvertible;

public class Point implements DtoConvertible {
    private int _dimension;
    private double[] _coords;
    private long  _oid;

    public Point() {
    }

    public Point(int dimension) {
        this._dimension = dimension;
        this._oid = -1;
    }

    public Point(double[] cords) {
        this._coords = cords;
        this._dimension = cords.length;
        this._oid = -1;
    }

    public Point(double[] c, long oid) {
        this._coords = c;
        this._dimension = c.length;
        this._oid = oid;
    }

    public Point(PointDTO dto) {
        this._coords = dto.coords;
        this._dimension = dto.coords.length;
        this._oid = dto.oid;
    }

    //επιστρέφεται η διάσταση του σημείου
    public int getDimension(){
        return _dimension;
    }

    //θέτουμε τις συντεταγμένες ενός σημείου
    public void setCords(double[] data){
        this._coords = data;
    }

    //επιστρέφονται οι συντεταγμένες
    public double[] getCords() {
        return _coords;
    }

    //επιστρέφεται το id
    public long getOid() {
        return _oid;
    }

    //θέτουμε το id
    public void setOid(long oid) {
        this._oid = oid;
    }

    /**
     * Απόσταση μεταξύ δύο σημείων, με Ευκλίδεια μετρική
     * @param otherPoint 
     * @return
     */
    public float distance(Point otherPoint) {
        double[] otherPoints = otherPoint.getCords();
        float distance = 0;
        for (int i = 0; i < _coords.length; i++) {
            double tmp = (_coords[i] * _coords[i]) - (otherPoints[i] * otherPoints[i]);
            if(tmp < 0)
                tmp = -1 * tmp;

            distance += tmp;
        }
        return (float)Math.pow(distance, 0.5);
    }

    //Ένα σημείο σε string
    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder("[");
        for (double coord : _coords) {
            sBuilder.append(coord).append(",");
        }
        sBuilder.append("]");
        return sBuilder.toString();
    }

    //Ένα σημείο σε αντικείμενο DTO για να γραφτεί στο δίσκο
    @Override
    public PointDTO toDTO() {
        return new PointDTO( _oid, _coords);
    }
}
