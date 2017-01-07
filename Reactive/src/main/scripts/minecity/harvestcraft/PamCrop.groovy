package minecity.harvestcraft

import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData
import com.pam.harvestcraft.blocks.growables.PamCropGrowable
import minecity.minecraft.Crop
import net.minecraft.block.state.IBlockState

class PamCrop extends Crop implements PamRightClickHarvesting {

    @Override
    boolean isMature(BlockStateData state) {
        def type = state.blockTypeData.blockType
        if(type instanceof PamCropGrowable)
            type.isMature((IBlockState) state.blockState)
        else
            super.isMature(state)
    }
}
