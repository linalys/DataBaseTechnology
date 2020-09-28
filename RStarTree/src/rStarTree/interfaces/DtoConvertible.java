package rStarTree.interfaces;

import rStarTree.dataToObject.AbstractDTO;

public interface DtoConvertible {
    public <T extends AbstractDTO> T toDTO();
}
