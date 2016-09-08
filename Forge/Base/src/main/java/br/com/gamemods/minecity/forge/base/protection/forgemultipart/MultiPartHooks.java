package br.com.gamemods.minecity.forge.base.protection.forgemultipart;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.BlockMultiPartTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.ButtonPartTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart.EventHandlerTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@Referenced
public class MultiPartHooks
{
    private static Method TMultiPart$tile;

    public static ITileMultiPart tile(ITMultiPart part)
    {
        try
        {
            if(TMultiPart$tile == null)
                TMultiPart$tile = Class.forName("codechicken.multipart.TMultiPart").getDeclaredMethod("tile");
            Object invoke = TMultiPart$tile.invoke(part);
            return (ITileMultiPart) invoke;
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = ButtonPartTransformer.class)
    public static List<EntityProjectile> onWoodButtonFindEntity(List<EntityProjectile> list, IButtonPart part)
    {
        if(list.isEmpty())
            return list;

        ModEnv.entityProtections.onArrowActivate(list, part.tileI().getBlockPos(ModEnv.entityProtections.mod));
        return list;
    }

    @Referenced(at = BlockMultiPartTransformer.class)
    public static boolean onPartClick(ITMultiPart part, EntityPlayer entityPlayer, ItemStack stack)
    {
        if(entityPlayer.worldObj.isRemote)
            return false;

        ForgePlayer player = ModEnv.entityProtections.mod.player(entityPlayer);
        Optional<Message> denial = part.reactPlayerClick((IEntityPlayerMP) entityPlayer, (IItemStack) (Object) stack)
                .can(player.getServer().mineCity, player);

        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    @Referenced(at = BlockMultiPartTransformer.class)
    public static boolean onPartActivate(ITMultiPart part, EntityPlayer entityPlayer, ItemStack stack)
    {
        if(entityPlayer.worldObj.isRemote)
            return false;

        ForgePlayer player = ModEnv.entityProtections.mod.player(entityPlayer);
        Optional<Message> denial = part.reactPlayerActivate((IEntityPlayerMP) entityPlayer, (IItemStack) (Object) stack)
                .can(player.getServer().mineCity, player);

        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    @Referenced(at = EventHandlerTransformer.class)
    public static boolean onPartPlace(EntityPlayer entityPlayer, World world, int x, int y, int z)
    {
        if(entityPlayer.worldObj.isRemote)
            return false;

        MineCityForge mod = ModEnv.entityProtections.mod;
        ForgePlayer player = mod.player(entityPlayer);
        FlagHolder holder = mod.mineCity.provideChunk(new ChunkPos(mod.world(world), x >> 4, z >> 4)).getFlagHolder(x, y, z);
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            return true;
        }

        return false;
    }
}
