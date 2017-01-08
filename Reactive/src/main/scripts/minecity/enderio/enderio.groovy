package minecity.enderio

import crazypants.enderio.conduit.*
import crazypants.enderio.conduit.facade.BlockConduitFacade

blockType(['enderio:blockTravelAnchor', 'enderio:blockDialingDevice']) {
    setReactive modifiableBlock
}

itemType(AbstractConduit) {
    setReactive new Conduit()
}

blockType([BlockConduitBundle, BlockConduitFacade]) {
    setReactive new Conduit()
}
