package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class BlockTaintTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockTaintTransformer()
    {
        super("thaumcraft.common.blocks.BlockTaint");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("tryToFall"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new VarInsnNode(ILOAD, 5));
                list.add(new VarInsnNode(ILOAD, 6));
                list.add(new VarInsnNode(ILOAD, 7));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                        "onTaintTryToFall",
                        "(Lnet/minecraft/world/World;IIIIII)Z",
                        false
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
