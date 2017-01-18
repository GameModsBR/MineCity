package minecity

import minecity.forge.Wrench

itemType('bigreactors:wrench') {
    setReactive new Wrench()
}

blockType([
        'bigreactors:reactorController', 'bigreactors:reactorAccessPort'
]) {
    setReactive modifiableBlock
}
