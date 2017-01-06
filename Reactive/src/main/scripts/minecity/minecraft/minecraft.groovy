package minecity.minecraft

String block(name) {
    "net.minecraft.block.Block$name"
}

String item(name) {
    "net.minecraft.item.Item$name"
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
        block('ShulkerBox'),
        'minecraft:white_shulker_box', 'minecraft:black_shulker_box',
        'minecraft:blue_shulker_box', 'minecraft:cyan_shulker_box',
        'minecraft:brown_shulker_box', 'minecraft:orange_shulker_box',
        'minecraft:gray_shulker_box', 'minecraft:silver_shulker_box',
        'minecraft:green_shulker_box', 'minecraft:lime_shulker_box',
        'minecraft:red_shulker_box', 'minecraft:light_blue_shulker_box',
        'minecraft:magenta_shulker_box', 'minecraft:yellow_shulker_box',
        'minecraft:pink_shulker_box', 'minecraft:purple_shulker_box',

        // Does not extends Container
        block('Cauldron'), 'minecraft:cauldron',
]) {
    setReactive containerBlock
}

blockType([
        block('Cake'), 'minecraft:cake'
]) {
    setReactive new Cake()
}

itemType([
        item('Record'),
        'minecraft:record_13', 'minecraft:record_cat',
        'minecraft:record_blocks', 'minecraft:record_chirp',
        'minecraft:record_far', 'minecraft:record_mall',
        'minecraft:record_mellohi', 'minecraft:record_stal',
        'minecraft:record_strad', 'minecraft:record_ward',
        'minecraft:record_11', 'minecraft:record_wait',
]) {
    setReactive new Record()
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
        block('FlowerPot'), 'minecraft:flower_pot',
]) {
    setReactive new FlowerPot()
}

blockType([
        block('TNT'), 'minecraft:tnt'
]) {
    setReactive new TNT()
}

////////////////////////////// CLICKABLE //////////////////////////////
// Traits
blockTrait(['minecraft:lever_variant', 'minecraft:trap_door_half']) {
    setReactive clickableBlock
}

blockTrait('minecraft:door_half') {
    setReactive new Door()
}

// Types
blockType([
        block('Door'), 'minecraft:wooden_door', 'minecraft:spruce_door',
        'minecraft:birch_door', 'minecraft:jungle_door',
        'minecraft:acacia_door', 'minecraft:dark_oak_door',
]) {
    setReactive new Door()
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
