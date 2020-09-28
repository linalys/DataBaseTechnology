package rStarTree.dataToObject;

public class TreeDTO extends AbstractDTO {
    public int dimensions;
    public int pagesize;
    public long pointerToRoot;

    public TreeDTO(int dimensions, int pagesize, long rootPointer) {
        this.dimensions = dimensions;
        this.pagesize = pagesize;
        this.pointerToRoot = rootPointer;
    }
}
