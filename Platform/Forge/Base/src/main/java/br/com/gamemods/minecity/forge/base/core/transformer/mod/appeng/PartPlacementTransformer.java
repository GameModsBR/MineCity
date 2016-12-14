package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class PartPlacementTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public PartPlacementTransformer()
    {
        super("appeng.parts.PartPlacement");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("place"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ILOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new VarInsnNode(ALOAD, 5));
                list.add(new VarInsnNode(ALOAD, 6));
                list.add(new VarInsnNode(ALOAD, 7));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false));
                list.add(new VarInsnNode(ILOAD, 8));
                list.add(new MethodInsnNode(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/'),
                        "onPartPlace", "(Lnet/minecraft/item/ItemStack;IIIILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;II)Z", false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(IRETURN));
                list.add(labelNode);
                method.instructions.insert(list);
                break;
            }
        }
    }
}
