package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

import java.util.Map;

@Referenced
public class SevenInterfaceTransformer extends ForgeInterfaceTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenInterfaceTransformer()
    {
        Map<String, String> r = getReplacements();

        r.put("net.minecraft.entity.passive.EntityTameable",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityTameable");

        r.put("net.minecraft.entity.passive.EntityHorse",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityHorse");

        r.put("net.minecraft.entity.projectile.EntityArrow",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityArrow");

        r.put("net.minecraft.entity.item.EntityItem",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityItem");

        r.put("net.minecraft.block.BlockStem",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockStem");

        r.put("net.minecraft.block.BlockNetherWart",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockNetherWart");

        r.put("net.minecraft.entity.EntityLiving",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityLiving");

        r.put("net.minecraft.potion.PotionEffect",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenPotionEffect");

        r.put("net.minecraft.entity.projectile.EntityFishHook",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityFishHook");

        r.put("net.minecraftforge.common.util.BlockSnapshot",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockSnapshot");

        r.put("net.minecraft.block.Block",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlock");

        r.put("net.minecraft.entity.Entity",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntity");

        r.put("net.minecraft.item.ItemBlock",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.item.SevenItemBlock");

        r.put("net.minecraft.item.ItemStack",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.item.SevenItemStack");

        r.put("net.minecraft.entity.EntityLivingBase",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntityLivingBase");

        r.put("net.minecraft.server.management.ServerConfigurationManager",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenPlayerList");

        r.put("net.minecraft.util.MovingObjectPosition",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenMovingObjectPosition");

        r.put("net.minecraft.block.BlockCrops",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenBlockCrops");

        r.put("net.minecraft.server.MinecraftServer",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenMinecraftServer");

        setReplacements(r);
        printReplacements();
    }
}
