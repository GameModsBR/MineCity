package minecity.minecraft

def block(name) {
    return "net.minecraft.block.Block$name"
}

////////////////////////////// CONTAINERS //////////////////////////////
blockType([
        // Extends Container
        block('Container'),
        block('Chest'), 'minecraft:chest',
        block('Furnace'), 'minecraft:furnace',
        block('BrewingStand'), 'minecraft:brewing_stand',
        block('Hopper'), 'minecraft:hopper',
        block('Dispenser'), 'minecraft:dispenser',
        block('Dropper'), 'minecraft:dropper',
        block('Jukebox'), 'minecraft:jukebox',

        // Does not extends Container
        block('Cauldron'), 'minecraft:cauldron',
        block('Cake'), 'minecraft:cake'
]) {
    setReactive containerBlock
}

//////////////////////// DECORATIVE CONTAINERS ////////////////////////
// Restore the decorative properties of decorative blocks that extends
// the Container class.
blockType([
        block('Banner'), 'minecraft:banner',
        block('Banner.BlockBannerHanging'), block('Banner.BlockBannerStanding'),
        block('EndGateway'), 'minecraft:end_portal',
        block('Skull'), 'minecraft:skull',
        block('Sign'), 'minecraft:sign',
        block('PistonMoving'), 'minecraft:piston_extension',
        block('MobSpawner'), 'minecraft:mob_spawner'
]) {
    setReactive decorativeBlock
}

////////////////////////////// MODIFIABLE //////////////////////////////
blockType([
        // Extends Container
        block('CommandBlock'), 'minecraft:command',
        block('EndPortal'), 'minecraft:end_portal_frame',
        block('Beacon'), 'minecraft:beacon',
        block('DaylightDetector'), 'minecraft:daylight_detector',
        block('FlowerPot'), 'minecraft:flower_pot',
        block('Note'), 'minecraft:noteblock',
        block('Structure'), 'minecraft:structure_block',

        // Does not extends Container
        block('RedstoneComparator'), 'minecraft:unpowered_comparator',
        'minecraft:powered_comparator',
        block('RedstoneRepeater'), 'minecraft:unpowered_repeater',
        'minecraft:powered_repeater'
]) {
    setReactive modifiableBlock
}

blockType([
        block('TNT'), 'minecraft:tnt'
]) {
    setReactive new TNT()
}

////////////////////////////// CLICKABLE //////////////////////////////
blockTrait(['minecraft:door_half', 'minecraft:trap_door_half']) {
    setReactive clickableBlock
}
blockType([
        block('Door'), 'minecraft:wooden_door', 'minecraft:spruce_door',
        'minecraft:birch_door', 'minecraft:jungle_door',
        'minecraft:acacia_door', 'minecraft:dark_oak_door',
]) {
    // TODO: Send block update to block above and below
    setReactive clickableBlock
}

blockType([
        block('Button'),
        block('ButtonStone'), 'minecraft:stone_button',
        block('ButtonWood'), 'minecraft:wooden_button',
        block('Lever'), 'minecraft:lever',

        block('Workbench'), 'minecraft:crafting_table',
        block('EnchantmentTable'), 'minecraft:enchanting_table',
        block('EnderChest'), 'minecraft:ender_chest',

        block('TrapDoor'), 'minecraft:trapdoor',

        block('FenceGate'), 'minecraft:fence_gate',
        'minecraft:spruce_fence_gate', 'minecraft:birch_fence_gate',
        'minecraft:jungle_fence_gate', 'minecraft:dark_oak_fence_gate',
        'minecraft:acacia_fence_gate'
]) {
    setReactive clickableBlock
}

blockType([
        block('Bed'), 'minecraft:bed'
]) {
    setReactive new Bed()
}

/////////////////////////////// GRAVITY ///////////////////////////////
blockType([
        block('Sand'), 'minecraft:sand',
        block('Gravel'), 'minecraft:gravel'
]) {
    setReactive new Sand()
}

blockType([
        block('Anvil'), 'minecraft:anvil',
]) {
    setReactive new Anvil()
}

blockType([
        block('DragonEgg'), 'minecraft:dragon_egg',
]) {
    setReactive new DragonEgg()
}
