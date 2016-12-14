package br.com.gamemods.minecity.forge.base.core.transformer.mod.pamharvestcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced
public class BlockPamSaplingTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("com.pam.harvestcraft.BlockPamSapling"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        String hookClass = ModEnv.hookClass.replace('.','/');

        for(MethodNode method : node.methods)
        {
            // 1.7.10
            if(method.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V") && method.name.equals("growTree"))
            {
                if(method.instructions.size() == 0)
                    break;

                System.out.println("\n | - Inserting try-finally block to "+srg+"#"+method.name+method.desc);

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

                InsnList list = new InsnList();
                LabelNode labelStart = new LabelNode();
                LabelNode labelEnd = new LabelNode();
                LabelNode labelCatch = new LabelNode();

                list.add(labelStart);
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false
                ));
                method.instructions.insert(list);

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
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
