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
public class BlockPistonBaseTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("net.minecraft.block.BlockPistonBase"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String hook = ModEnv.hookClass.replace('.', '/');

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == IRETURN)
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
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hook, "onPistonMove",
                                    "(ZLjava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z",
                                    false
                            ));
                            method.instructions.insertBefore(method.instructions.get(index), list);
                        });

                InsnList list = new InsnList();
                LabelNode labelStart = new LabelNode();
                LabelNode labelEnd = new LabelNode();
                LabelNode labelCatch = new LabelNode();

                list.add(labelStart);
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hook, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                ));
                method.instructions.insert(list);

                list = new InsnList();
                list.add(labelEnd);
                list.add(labelCatch);
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(SWAP));
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 2));
                list.add(new VarInsnNode(ALOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hook, "onPistonMove",
                        "(ZLjava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z",
                        false
                ));
                list.add(new InsnNode(IRETURN));

                method.instructions.add(list);
                method.tryCatchBlocks.add(new TryCatchBlockNode(labelStart, labelEnd, labelCatch, null));
                break;
            }
            else if(method.desc.equals("(Lnet/minecraft/world/World;IIII)Z") && (method.name.equals("func_150079_i") || method.name.equals("tryExtend")))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == IRETURN)
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
                            list.add(new VarInsnNode(ILOAD, 5));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hook, "onPistonMove",
                                    "(ZLjava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;IIII)Z",
                                    false
                            ));
                            method.instructions.insertBefore(method.instructions.get(index), list);
                        });

                InsnList list = new InsnList();
                LabelNode labelStart = new LabelNode();
                LabelNode labelEnd = new LabelNode();
                LabelNode labelCatch = new LabelNode();

                list.add(labelStart);
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hook, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                ));
                method.instructions.insert(list);

                list = new InsnList();
                list.add(labelEnd);
                list.add(labelCatch);
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(SWAP));
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new VarInsnNode(ILOAD, 5));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hook, "onPistonMove",
                        "(ZLjava/lang/Throwable;Ljava/lang/Object;Lnet/minecraft/world/World;IIII)Z",
                        false
                ));
                list.add(new InsnNode(IRETURN));

                method.instructions.add(list);
                method.tryCatchBlocks.add(new TryCatchBlockNode(labelStart, labelEnd, labelCatch, null));
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
