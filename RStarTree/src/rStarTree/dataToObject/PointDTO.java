package rStarTree.dataToObject;

public class PointDTO extends AbstractDTO{
    public long oid;
    public double[] coords;

    public PointDTO(long oid, double[] coords) {
        this.oid = oid;
        this.coords = coords;
    }
}
