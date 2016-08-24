package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

/**
 * Makes {@link net.minecraft.world.chunk.Chunk} implements {@link br.com.gamemods.minecity.forge.base.accessors.IChunk}
 * <pre><code>
 *     public class Chunk
 *         implements IChunk // <- Added
 *     {
 *         // ... original fields and methods
 *         public ClaimedChunk mineCity;
 *         public ClaimedChunk getMineCityClaim(){ return this.mineCity; }
 *         public void setMineCityClaim(ClaimedChunk claim){ this.mineCity = claim; }
 *     }
 * </code></pre>
 */
public class ChunkTransformer extends InsertInterfaceTransformer
{
    public ChunkTransformer(String interfaceClass)
    {
        super(
                "net.minecraft.world.chunk.Chunk",
                "br.com.gamemods.minecity.structure.ClaimedChunk", "mineCity",
                interfaceClass, "setMineCityClaim", "getMineCityClaim"
        );
    }

    public ChunkTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IChunk");
    }
}
