package minecity

String block(name) {
    "net.malisis.doors.block.$name"
}

blockType([
        block('BigDoor'), 'malisisdoors:carriage_door', 'malisisdoors:medieval_door',
        block('RustyHatch'), 'malisisdoors:rustyHatch',
        block('ModelDoor'), 'malisisdoors:hitechdoor'
]) {
    setReactive clickableBlock
}

blockType([
        block('SaloonDorBlock'), 'malisisdoors:saloon'
]) {
    setReactive decorativeBlock
}
