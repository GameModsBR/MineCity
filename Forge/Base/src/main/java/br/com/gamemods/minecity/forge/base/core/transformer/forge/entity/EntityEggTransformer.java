package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityEggTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityEgg"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/util/math/RayTraceResult;)V")
            || method.desc.equals("(Lnet/minecraft/util/MovingObjectPosition;)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == BIPUSH).map(IntInsnNode.class::cast)
                        .filter(ins -> ins.operand == 8)
                        .anyMatch(ins -> {
                            int index = method.instructions.indexOf(ins);
                            LabelNode label = ((JumpInsnNode) method.instructions.get(index - 3)).label;

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onEggSpawnChicken",
                                    "(Lnet/minecraft/entity/projectile/EntityEgg;)Z",
                                    false
                            ));
                            list.add(new JumpInsnNode(IFNE, label));

                            method.instructions.insert(method.instructions.get(index + 2), list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
