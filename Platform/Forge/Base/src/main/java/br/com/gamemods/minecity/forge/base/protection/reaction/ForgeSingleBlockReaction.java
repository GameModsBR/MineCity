package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class ForgeSingleBlockReaction extends SingleBlockReaction implements ForgeTriggers
{
    public ForgeSingleBlockReaction(@NotNull BlockPos pos, @NotNull PermissionFlag flag)
    {
        super(pos, flag);
    }
}
