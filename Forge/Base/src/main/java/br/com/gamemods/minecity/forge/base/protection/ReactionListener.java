package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;

public interface ReactionListener
{
    void postReaction(TriggeredReaction reaction, Permissible permissible, PermissionFlag flag, BlockPos pos, Message message);
}
