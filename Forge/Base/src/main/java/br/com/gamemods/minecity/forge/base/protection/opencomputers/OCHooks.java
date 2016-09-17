package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.InventoryWorldControlDClassTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.PacketHandlerDTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TankWorldControlDClassTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import scala.Option;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class OCHooks
{
    private static Object colorModule;
    private static Method isDye;
    private static Constructor blockPos;
    private static Object wrenchModule;
    private static Method holdsApplicableWrench;

    private static Object pos(int x, int y, int z)
    {
        try
        {
            if(blockPos == null)
            {
                Class<?> c;
                try
                {
                    c = Class.forName("net.minecraft.util.math.BlockPos");
                }
                catch(ClassNotFoundException e)
                {
                    c = Class.forName("li.cil.oc.util.BlockPosition");
                }
                blockPos = c.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }

            return blockPos.newInstance(x, y, z);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean holdsApplicableWrench(IEntityPlayerMP player, int x, int y, int z)
    {
        try
        {
            Object pos = pos(x, y, z);
            if(holdsApplicableWrench == null)
            {
                Class<?> c = Class.forName("li.cil.oc.integration.util.Wrench$");
                wrenchModule = c.getDeclaredField("MODULE$").get(null);
                holdsApplicableWrench = c.getDeclaredMethod("holdsApplicableWrench", EntityPlayer.class, pos.getClass());
            }

            return (boolean) holdsApplicableWrench.invoke(wrenchModule, player, pos);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Object getColorModule()
    {
        try
        {
            if(colorModule == null)
                colorModule = Class.forName("li.cil.oc.util.Color$").getDeclaredField("MODULE$").get(null);

            return colorModule;
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    public static boolean isDye(ItemStack stack)
    {
        try
        {
            Object module = getColorModule();
            if(isDye == null)
                isDye = Class.forName("li.cil.oc.util.Color$").getDeclaredMethod("isDye", ItemStack.class);
            return (boolean) isDye.invoke(module, stack);
        }
        catch(ReflectiveOperationException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Referenced(at = PacketHandlerDTransformer.class)
    public static boolean onPlayerInteract(ITextBuffer buffer, IEntityPlayerMP player)
    {
        MineCityForge mod = player.getServer();

        IEnvironmentHost host = buffer.hostI();
        if(host instanceof IScreen)
        {
            BlockPos pos = host.envBlockPos(mod);
            return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player,
                    PermissionFlag.CLICK
            ).isPresent();
        }

        return false;
    }

    @Referenced(at = InventoryWorldControlDClassTransformer.class)
    public static boolean onRobotDropItem(IInventory inventory, int slot, int amount, IAgentComponent agent, int face)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        IEntityPlayerMP player = (IEntityPlayerMP) agent.fakePlayer();
        BlockPos pos = player.getBlockPos(mod).add(ForgeUtil.toDirection(face));
        return mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player, PermissionFlag.PICKUP).isPresent();
    }

    @Referenced(at = TankWorldControlDClassTransformer.class)
    public static boolean onRobotAccessTank(IAgentComponent agent, Option<IWorldServer> world, int x, int y, int z)
    {
        if(world.isEmpty())
            return true;

        MineCityForge mod = ModEnv.entityProtections.mod;
        return mod.mineCity.provideChunk(new ChunkPos(mod.world(world.get()), x >> 4, z >> 4)).getFlagHolder(x, y, z)
                .can((IEntityPlayerMP) agent.fakePlayer(), PermissionFlag.MODIFY).isPresent();
    }
}
