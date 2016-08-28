package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

import java.util.Map;

@Referenced
public class FrostInterfaceTransformer extends ForgeInterfaceTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostInterfaceTransformer()
    {
        Map<String, String> r = getReplacements();

        r.put("net.minecraftforge.common.util.BlockSnapshot",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostBlockSnapshot");

        r.put("net.minecraft.block.Block",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostBlock");

        r.put("net.minecraft.entity.Entity",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.entity.FrostEntity");

        r.put("net.minecraft.block.state.IBlockState",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block.FrostState");

        r.put("net.minecraft.server.management.PlayerList",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostPlayerList");

        r.put("net.minecraft.util.math.RayTraceResult",
                "br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostRayTraceResult");

        setReplacements(r);
        printReplacements();
    }
}
