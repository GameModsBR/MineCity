package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class PartAnnihilationPaneTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public PartAnnihilationPaneTransformer()
    {
        super("appeng.parts.automation.PartAnnihilationPlane");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("canHandleBlock"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/'),
                        "onPartModify",
                        "(Lbr.com.gamemods.minecity.forge.base.protection.appeng.IAEBasePart;Lnet/minecraft/world/WorldServer;III)Z"
                            .replace('.','/'),
                        false
                ));
                LabelNode label = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, label));
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(IRETURN));
                list.add(label);
                method.instructions.insert(list);
                break;
            }
        }
    }
}
