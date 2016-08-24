package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

/**
 * Makes {@link net.minecraft.world.WorldServer} implements {@link br.com.gamemods.minecity.forge.base.accessors.IWorldServer}
 * <pre><code>
 *     public class WorldServer extends World
 *         implements IWorldServer // <- Added
 *     {
 *         // ... original fields and methods
 *         public WorldDim mineCity;
 *         public WorldDim getMineCityWorld(){ return this.mineCity; }
 *         public void setMineCityWorld(WorldDim world){ this.mineCity = world; }
 *     }
 * </code></pre>
 */
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCityForge7CoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class WorldServerTransformer extends InsertInterfaceTransformer
{
    public WorldServerTransformer()
    {
        super(
                "net.minecraft.world.WorldServer",
                "br.com.gamemods.minecity.api.world.WorldDim", "mineCity",
                "br.com.gamemods.minecity.forge.base.accessors.IWorldServer",
                "setMineCityWorld", "getMineCityWorld"
        );
    }
}
