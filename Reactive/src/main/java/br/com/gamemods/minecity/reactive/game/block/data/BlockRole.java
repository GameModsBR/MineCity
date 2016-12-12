package br.com.gamemods.minecity.reactive.game.block.data;

/**
 * The main purpose of a block to exists in the game.
 */
public enum BlockRole
{
    /**
     * An aesthetic block that does nothing alone. Like stone and wood
     */
    DECORATIVE,

    /**
     * A block that storage things inside it. Like chests and furnaces
     */
    CONTAINER,

    /**
     * A block that can be clicked. Like buttons and wooden doors
     */
    CLICKABLE,

    /**
     * A block that modify itself when clicked. Like repeaters and comparators
     */
    MODIFIABLE
}
