package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemMinecartTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;

@Referenced(at = ItemMinecartTransformer.class)
public interface IItemMinecart extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos block, Direction face)
    {
        if(state.getIBlock() instanceof BlockRailBase)
        {
            if(getUnlocalizedName().equals("item.minecart"))
            {
                SingleBlockReaction reaction = new SingleBlockReaction(block, PermissionFlag.VEHICLE);
                MineCityForge mod = player.getMineCityPlayer().getServer();
                reaction.addAllowListener((reaction1, permissible, flag, pos, message) ->
                    mod.addPostSpawnListener(pos.precise(), 2, EntityMinecart.class, 2, cart ->
                            mod.setOwnerIfAbsent(cart, player.getIdentity())
                ));
                return reaction;
            }
            else
                return new SingleBlockReaction(block, PermissionFlag.MODIFY);
        }

        return NoReaction.INSTANCE;
    }
}
