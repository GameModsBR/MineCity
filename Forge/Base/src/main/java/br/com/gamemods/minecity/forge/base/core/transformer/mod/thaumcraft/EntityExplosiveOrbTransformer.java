package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

//@Referenced
@MethodPatcher
public class EntityExplosiveOrbTransformer extends BasicTransformer
{
    //@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public EntityExplosiveOrbTransformer()
    {
        super("thaumcraft.common.entities.projectile.EntityExplosiveOrb");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String trace = ModEnv.rayTraceResultClass.replace('.','/');
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("(L"+trace+";)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .map(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .map(ins-> method.instructions.get(method.instructions.indexOf(ins)-13))
                        .filter(ins-> ins.getOpcode() == Opcodes.ACONST_NULL)
                        .anyMatch(ins-> {method.instructions.set(ins, new VarInsnNode(ALOAD, 0)); return true;})
                        ;
                break;
            }
        }
    }
}
