package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.GrowMonitorTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

@Referenced
public class BlockCustomPlantTransformer extends GrowMonitorTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockCustomPlantTransformer()
    {
        super("thaumcraft.common.blocks.BlockCustomPlant");
    }

    @Override
    protected void patch(String srg, ClassNode node, ClassReader reader)
    {
        if(!ModEnv.seven)
        {
            abort = true;
            return;
        }

        node.methods.stream()
                .filter(method -> method.name.equals("growGreatTree") || method.name.equals("growSilverTree"))
                .forEachOrdered(method -> patchGrowSeven(srg, method, 0, 1, 2, 3, 4))
        ;
    }
}
