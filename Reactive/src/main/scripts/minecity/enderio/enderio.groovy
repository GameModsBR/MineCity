package minecity.enderio

import crazypants.enderio.conduit.*
import crazypants.enderio.conduit.facade.BlockConduitFacade
import minecity.forge.Wrench

blockType(['enderio:blockTravelAnchor', 'enderio:blockDialingDevice']) {
    setReactive modifiableBlock
}

itemType(AbstractConduit) {
    setReactive new Conduit()
}

blockType([BlockConduitBundle, BlockConduitFacade]) {
    setReactive new Conduit()
}

itemType('enderio:itemYetaWrench') {
    setReactive new Wrench()
}
