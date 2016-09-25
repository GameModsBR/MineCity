package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class ItemElementalAxeTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public ItemElementalAxeTransformer()
    {
        super("thaumcraft.common.items.equipment.ItemElementalAxe");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;I)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.name.equals("getEntitiesInRange"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onItemMoveEntity",
                                    "(Ljava/util/List;Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;I)Ljava/util/List;",
                                    false
                            ));
                            list.add(new TypeInsnNode(CHECKCAST, "java/util/ArrayList"));
                            method.instructions.insert(ins, list);
                            return true;
                        });
            }
        }
    }
}
