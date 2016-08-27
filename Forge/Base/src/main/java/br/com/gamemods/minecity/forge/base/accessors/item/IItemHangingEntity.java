package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemHangingEntityTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Referenced(at = ItemHangingEntityTransformer.class)
public interface IItemHangingEntity extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos block, Direction face)
    {
        if(face == Direction.DOWN || face == Direction.UP)
            return NoReaction.INSTANCE;

        SingleBlockReaction reaction = new SingleBlockReaction(block.add(face), PermissionFlag.MODIFY);
        MineCityForge mod = player.getMineCityPlayer().getServer();
        reaction.addAllowListener((reaction1, permissible, flag, pos, message) ->
            mod.addSpawnListener(entity -> {
                if(entity instanceof EntityPainting)
                {
                    mod.callSyncMethod(()->{
                        EntityPainting painting = (EntityPainting) entity;
                        EntityPainting.EnumArt currentArt = painting.art;
                        BlockPos blockPos = ((IEntity) painting).getBlockPos(mod);
                        double distance = blockPos.distance(pos);
                        double sqrt = (Math.sqrt(currentArt.sizeX*currentArt.sizeX + currentArt.sizeY*currentArt.sizeY)/16)-1;
                        if(distance < sqrt)
                        {
                            if(currentArt.sizeX == 16 && currentArt.sizeY == 16)
                                return;

                            ForgePlayer forgePlayer = player.getMineCityPlayer();

                            Direction right = face.right();

                            AtomicReference<ClaimedChunk> claim = new AtomicReference<>(mod.mineCity.provideChunk(pos.getChunk()));
                            Predicate<EntityPainting.EnumArt> check = art ->
                            {
                                if(art != currentArt && (
                                            art.sizeX > currentArt.sizeX || art.sizeY > currentArt.sizeY
                                        ||  art.sizeX == currentArt.sizeX && art.sizeY == currentArt.sizeY
                                ))
                                {
                                    return false;
                                }

                                int width = art.sizeX/16;
                                int height = art.sizeY/16;

                                for(int w = 0; w < width; w++)
                                    for(int h = 0; h < height; h++)
                                    {
                                        if(w == 0 && h == 0)
                                            continue;

                                        BlockPos extra = pos.add(right.x*w, h, right.z*w);
                                        ClaimedChunk chunk = mod.mineCity.provideChunk(extra.getChunk(), claim.get());
                                        claim.set(chunk);

                                        if(chunk.getFlagHolder(extra).can(forgePlayer, PermissionFlag.MODIFY).isPresent())
                                            return false;
                                    }

                                return true;
                            };

                            if(check.test(currentArt))
                                return;

                            List<EntityPainting.EnumArt> arts = Arrays.asList(EntityPainting.EnumArt.values());
                            Collections.shuffle(arts);
                            Optional<EntityPainting.EnumArt> validArt = arts.stream().filter(check).findFirst();
                            if(validArt.isPresent())
                            {
                                painting.art = validArt.get();
                                NBTTagCompound nbt = new NBTTagCompound();
                                entity.writeNBT(nbt);
                                nbt.removeTag("UUIDLeast");
                                nbt.removeTag("UUIDMost");
                                WorldServer world = entity.getWorld();
                                painting.setDead();
                                painting = new EntityPainting(world);
                                ((IEntity) painting).readNBT(nbt);
                                world.spawnEntityInWorld(painting);
                            }
                        }
                    });

                    return true;
                }

                return false;
            }, 2)
        );

        return reaction;
    }
}
