package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ClassHasNoToStringMethod")
@Mixin(World.class)
public abstract class MixinWorld implements MixedWorld
{
    @Nullable
    private WorldData worldData;

    @NotNull
    @Override
    public WorldData getWorldData()
    {
        if(worldData != null)
            return worldData;

        return worldData = ReactiveLayer.getServerManipulator().getWorldData(this).get();
    }
}
