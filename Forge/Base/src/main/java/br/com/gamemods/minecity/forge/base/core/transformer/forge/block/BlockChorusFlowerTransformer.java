package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class BlockChorusFlowerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.block.BlockChorusFlower"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        String hookClass = ModEnv.hookClass.replace('.','/');

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
            {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                int initial = -1;
                while(iter.hasNext())
                {
                    AbstractInsnNode next = iter.next();
                    if(next.getOpcode() == IFNE)
                    {
                        LabelNode label = ((JumpInsnNode) next).label;
                        initial = method.instructions.indexOf(label)+1;
                        iter = method.instructions.iterator(initial);
                        break;
                    }
                }

                CollectionUtil.stream(iter)
                        .filter(ins-> ins.getOpcode() == RETURN)
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                        .forEachOrdered(index -> {
                            InsnList list = new InsnList();
                            list = new InsnList();
                            list.add(new InsnNode(ACONST_NULL));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new VarInsnNode(ALOAD, 3));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onGrowableGrow",
                                    "(Ljava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
                                    false
                            ));
                            method.instructions.insertBefore(method.instructions.get(index), list);
                        });

                InsnList list = new InsnList();

                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                ));
                method.instructions.insertBefore(method.instructions.get(initial), list);

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = ModEnv.saveClass(srg, writer.toByteArray());
        return bytes;
    }
}
