package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class TileEntityMinerTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public TileEntityMinerTransformer()
    {
        super(Arrays.asList(
                "ic2.core.block.machine.tileentity.TileEntityMiner",
                "ic2.core.block.machine.tileentity.TileEntityAdvMiner"
        ));
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String icHookds = "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/');
        String tile = "br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity".replace('.','/');
        String icPos = "ic2.core.util.Ic2BlockPos".replace('.','/');

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("canPump") || method.name.equals("canMine"))
            {
                InsnList list = new InsnList();
                // 1.7.10
                if(method.desc.equals("(III)Z"))
                {
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ILOAD, 1));
                    list.add(new VarInsnNode(ILOAD, 2));
                    list.add(new VarInsnNode(ILOAD, 3));
                }
                // 1.10.2
                else
                {
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, icPos, "getX", "()I", false));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, icPos, "getY", "()I", false));
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, icPos, "getZ", "()I", false));
                }
                list.add(new MethodInsnNode(INVOKESTATIC,
                        icHookds, "onMinerModify", "(L"+tile+";III)Z", false
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
