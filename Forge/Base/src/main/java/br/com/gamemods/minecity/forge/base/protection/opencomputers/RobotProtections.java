package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.protection.vanilla.BlockProtections;
import br.com.gamemods.minecity.structure.ClaimedChunk;

public class RobotProtections extends BlockProtections
{
    public RobotProtections(MineCityForge mod)
    {
        super(mod);
    }

    public boolean onRobotMove(IAgent agent, Direction dir)
    {
        int x = (int) agent.xPosition();
        int y = (int) agent.yPosition();
        int z = (int) agent.zPosition();
        ClaimedChunk chunk = mod.mineCity.provideChunk(
                new ChunkPos(mod.world(agent.world()), x >> 4, z >> 4)
        );

        return chunk.getFlagHolder(x + dir.x, y + dir.y, z + dir.z).can(agent.ownerId(), PermissionFlag.MODIFY).isPresent();
    }

    public boolean onRobotPlaceBlock(IAgent agent, IItemStack stack, BlockPos pos)
    {
        PlayerID owner = agent.ownerId();
        return stack.getIItem().reactPrePlace(owner, stack, pos).can(mod.mineCity, owner).isPresent();
    }
}
