package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedServer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class MixinServer implements MixedServer
{
    @Nullable
    private ServerData serverData;

    @NotNull
    @Override
    public ServerData getServerData()
    {
        if(serverData != null)
            return serverData;

        return serverData = ReactiveLayer.getServerManipulator().getServerData(this).get();
    }
}
