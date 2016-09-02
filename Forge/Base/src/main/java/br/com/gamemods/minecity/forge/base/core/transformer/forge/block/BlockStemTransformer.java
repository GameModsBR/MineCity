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

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class BlockStemTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.block.BlockStem"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.', '/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
            {
                LabelNode labelStart = new LabelNode();
                LabelNode labelEnd = new LabelNode();
                LabelNode labelCatch = new LabelNode();

                CollectionUtil.stream(method.instructions.iterator())
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

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKESPECIAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/block/BlockBush"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
                        .anyMatch(ins -> {
                            InsnList list = new InsnList();
                            list.add(labelStart);
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                            ));
                            method.instructions.insert(ins, list);

                            list = new InsnList();
                            list.add(labelEnd);
                            list.add(labelCatch);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new VarInsnNode(ALOAD, 3));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onGrowableGrow",
                                    "(Ljava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
                                    false
                            ));
                            list.add(new InsnNode(RETURN));

                            method.instructions.add(list);
                            method.tryCatchBlocks.add(new TryCatchBlockNode(labelStart, labelEnd, labelCatch, null));
                            return true;
                        });

                break;
            }
            else if(method.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V"))
            {
                LabelNode labelStart = new LabelNode();
                LabelNode labelEnd = new LabelNode();
                LabelNode labelCatch = new LabelNode();

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == RETURN)
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                        .forEachOrdered(index -> {
                            InsnList list = new InsnList();
                            list = new InsnList();
                            list.add(new InsnNode(ACONST_NULL));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onGrowableGrow",
                                    "(Ljava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;III)V",
                                    false
                            ));
                            method.instructions.insertBefore(method.instructions.get(index), list);
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKESPECIAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/block/BlockBush"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V"))
                        .anyMatch(ins -> {
                            InsnList list = new InsnList();
                            list.add(labelStart);
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                            ));
                            method.instructions.insert(ins, list);

                            list = new InsnList();
                            list.add(labelEnd);
                            list.add(labelCatch);
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onGrowableGrow",
                                    "(Ljava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;III)V",
                                    false
                            ));
                            list.add(new InsnNode(RETURN));

                            method.instructions.add(list);
                            method.tryCatchBlocks.add(new TryCatchBlockNode(labelStart, labelEnd, labelCatch, null));
                            return true;
                        });

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
