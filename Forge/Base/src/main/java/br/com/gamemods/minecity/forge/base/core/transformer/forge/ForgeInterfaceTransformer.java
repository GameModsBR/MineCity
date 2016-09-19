package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

import java.util.HashMap;
import java.util.Map;

public class ForgeInterfaceTransformer extends InsertInterfaceTransformer
{
    public ForgeInterfaceTransformer()
    {
        Map<String, String> r = new HashMap<>();

        r.put("net.minecraftforge.fml.common.registry.IThrowableEntity",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ThrowableEntity");

        r.put("net.minecraft.entity.EntityLeashKnot",
                "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityLeashKnot");

        r.put("net.minecraft.item.ItemLead",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemLead");

        r.put("net.minecraft.block.BlockCake",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockCake");

        r.put("net.minecraft.nbt.NBTTagCompound",
                "br.com.gamemods.minecity.forge.base.accessors.nbt.INBTTagCompound");

        r.put("net.minecraft.nbt.NBTBase",
                "br.com.gamemods.minecity.forge.base.accessors.nbt.INBTBase");

        r.put("net.minecraft.tileentity.TileEntity",
                "br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity");

        r.put("net.minecraft.block.BlockMushroom",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockMushroom");

        r.put("net.minecraft.block.BlockSapling",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockSapling");

        r.put("net.minecraft.item.ItemGlassBottle",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemGlassBottle");

        r.put("net.minecraft.block.BlockDragonEgg",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockDragonEgg");

        r.put("net.minecraft.entity.passive.EntityOcelot",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityOcelot");

        r.put("net.minecraft.entity.passive.EntityWolf",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityWolf");

        r.put("net.minecraft.entity.passive.EntityTameable",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityTameable");

        r.put("net.minecraft.entity.monster.EntityZombie",
                "br.com.gamemods.minecity.forge.base.accessors.entity.mob.IEntityZombie");

        r.put("net.minecraft.entity.monster.EntityMob",
                "br.com.gamemods.minecity.forge.base.accessors.entity.mob.IEntityMob");

        r.put("net.minecraft.entity.EntityCreature",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityCreature");

        r.put("net.minecraft.entity.passive.EntityAnimal",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityAnimal");

        r.put("net.minecraft.item.ItemSaddle",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemSaddle");

        r.put("net.minecraft.entity.passive.EntityPig",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityPig");

        r.put("net.minecraft.entity.passive.EntityVillager",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityVillager");

        r.put("net.minecraft.block.BlockColored",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockWool");

        r.put("net.minecraft.entity.passive.EntitySheep",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntitySheep");

        r.put("net.minecraft.entity.passive.EntityMooshroom",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityMushroom");

        r.put("net.minecraft.entity.passive.EntityCow",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityCow");

        r.put("net.minecraft.block.BlockPumpkin",
                "br.com.gamemods.minecity.forge.base.accessors.block.BlockStemProduct");

        r.put("net.minecraft.block.BlockMelon",
                "br.com.gamemods.minecity.forge.base.accessors.block.BlockStemProduct");

        r.put("net.minecraft.block.BlockStem",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockStem");

        r.put("net.minecraft.item.ItemSeedFood",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemSeedFood");

        r.put("net.minecraft.item.ItemSeeds",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemSeeds");

        r.put("net.minecraft.item.ItemBlockSpecial",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemBlockSpecial");

        r.put("net.minecraft.block.BlockNetherWart",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockNetherWart");

        r.put("net.minecraft.block.BlockReed",
                "br.com.gamemods.minecity.forge.base.accessors.block.BlockTallHarvest");

        r.put("net.minecraft.block.BlockCactus",
                "br.com.gamemods.minecity.forge.base.accessors.block.BlockTallHarvest");

        r.put("net.minecraft.entity.item.EntityItem",
                "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem");

        r.put("net.minecraft.item.ItemEndCrystal",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemEndCrystal");

        r.put("net.minecraft.entity.item.EntityMinecartContainer",
                "br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicleContainer");

        r.put("net.minecraft.entity.item.EntityMinecartEmpty",
                "br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicleRideable");

        r.put("net.minecraft.entity.item.EntityBoat",
                "br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicleRideable");

        r.put("net.minecraft.potion.PotionEffect",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IPotionEffect");

        r.put("net.minecraft.entity.projectile.EntityArrow",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityArrow");

        r.put("net.minecraft.entity.EntityAgeable",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAgeable");

        r.put("net.minecraft.entity.passive.EntityHorse",
                "br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityHorse");

        r.put("net.minecraft.entity.projectile.EntityFireball",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityFireball");

        r.put("net.minecraft.entity.projectile.EntityFishHook",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityFishHook");

        r.put("net.minecraft.entity.EntityHanging",
                "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityHanging");

        r.put("net.minecraft.entity.EntityLiving",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLiving");

        r.put("net.minecraft.entity.projectile.EntityShulkerBullet",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityShulkerBullet");

        r.put("net.minecraft.entity.projectile.EntityThrowable",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityThrowable");

        r.put("net.minecraft.entity.item.EntityTNTPrimed",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityTNTPrimed");

        r.put("net.minecraft.entity.item.EntityXPOrb",
                "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityXPOrb");

        r.put("net.minecraft.item.ItemDye",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemDye");

        r.put("net.minecraft.item.ItemSnow",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemSnow");

        r.put("net.minecraft.block.BlockDoor",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockDoor");

        r.put("net.minecraft.item.ItemMinecart",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemMinecart");

        r.put("net.minecraft.block.Block",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlock");

        r.put("net.minecraft.entity.Entity",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity");

        r.put("net.minecraft.item.ItemBlock",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock");

        r.put("net.minecraft.entity.IProjectile",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.Projectile");

        r.put("net.minecraft.block.state.IBlockState",
                "br.com.gamemods.minecity.forge.base.accessors.block.IState");

        r.put("net.minecraft.server.MinecraftServer",
                "br.com.gamemods.minecity.forge.base.accessors.IMinecraftServer");

        r.put("net.minecraft.entity.EntityLivingBase",
                "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase");

        r.put("net.minecraft.item.ItemBoat",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemBoat");

        r.put("net.minecraft.block.BlockCrops",
                "br.com.gamemods.minecity.forge.base.accessors.block.IBlockCrops");

        r.put("net.minecraft.item.ItemStack",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemStack");

        r.put("net.minecraft.item.Item",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItem");

        r.put("net.minecraft.entity.item.EntityItemFrame",
                "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItemFrame");

        r.put("net.minecraft.block.properties.IProperty",
                "br.com.gamemods.minecity.forge.base.accessors.block.IProp");

        r.put("net.minecraft.item.ItemHangingEntity",
                "br.com.gamemods.minecity.forge.base.accessors.item.IItemHangingEntity");

        setReplacements(r);
    }
}
