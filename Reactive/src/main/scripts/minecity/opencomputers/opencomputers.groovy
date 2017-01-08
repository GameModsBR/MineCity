package minecity.opencomputers

import minecity.forge.Wrench

blockType('opencomputers:waypoint') {
    setReactive modifiableBlock
}

blockType('opencomputers:robot') {
    setReactive new RobotBlock()
}

itemType('opencomputers:wrench') {
    setReactive new Wrench()
}
