package br.com.gamemods.minecity.forge.base.core.transformer.forge.world;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;

/**
 * Makes {@link net.minecraft.world.WorldServer} implements {@link br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer}
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
public class WorldServerTransformer extends InsertSetterGetterTransformer
{
    public WorldServerTransformer(String interfaceClass)
    {
        super(
                "net.minecraft.world.WorldServer",
                "br.com.gamemods.minecity.api.world.WorldDim", "mineCity",
                interfaceClass, "setMineCityWorld", "getMineCityWorld"
        );
    }

    public WorldServerTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer");
    }
}
