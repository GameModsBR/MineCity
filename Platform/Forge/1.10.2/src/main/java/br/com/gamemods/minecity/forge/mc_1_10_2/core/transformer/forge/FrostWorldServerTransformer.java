package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.WorldServerTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class FrostWorldServerTransformer extends WorldServerTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostWorldServerTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostWorldServer");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        super.patch(name, node, reader);

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("func_175660_a") || method.name.equals("isBlockModifiable"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 2));
                list.add(new MethodInsnNode(INVOKESTATIC, ModEnv.hookClass.replace('.','/'),
                        "canMineBlock", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;)Z", false
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
