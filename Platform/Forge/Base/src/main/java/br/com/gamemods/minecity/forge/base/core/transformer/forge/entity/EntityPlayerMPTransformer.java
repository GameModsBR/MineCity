package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;

/**
 * Makes {@link net.minecraft.entity.player.EntityPlayerMP EntityPlayerMP}
 * implements {@link br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP IEntityPlayerMP}
 * <pre><code>
 *     public class EntityPlayerMP extends EntityPlayer
 *         implements IEntityPlayerMP // <- Added
 *     {
 *         // ... original fields and methods
 *         public ForgePlayer mineCity;
 *         public ForgePlayer getMineCityPlayer(){ return this.mineCity; }
 *         public void setMineCityPlayer(ForgePlayer player){ this.mineCity = player; }
 *     }
 * </code></pre>
 */
public class EntityPlayerMPTransformer extends InsertSetterGetterTransformer
{
    public EntityPlayerMPTransformer(String interfaceName)
    {
        super(
                "net.minecraft.entity.player.EntityPlayerMP",
                "br/com/gamemods/minecity/forge/base/command/ForgePlayer", "mineCity",
                interfaceName, "setMineCityPlayer", "getMineCityPlayer"
        );
    }
}
