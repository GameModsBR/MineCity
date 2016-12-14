package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class BiomeUtilTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public BiomeUtilTransformer()
    {
        super("ic2.core.util.BiomeUtil");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("setBiome"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC, ModEnv.hookClass.replace('.','/'), "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;", false));
                list.add(new MethodInsnNode(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                        "onChangeBiome", "(Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z", false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(RETURN));
                list.add(labelNode);
                method.instructions.insert(list);
                break;
            }
        }
    }
}
