package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.MethodPatcher;
import br.com.gamemods.minecity.forge.base.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@MethodPatcher
public class BlockSaplingTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.block.BlockSapling"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        MethodNode growMethod = node.methods.stream().filter(method->
                 method.desc.equals("(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V") ||
                (method.desc.equals("(Lnet/minecraft/world/World;Ljava/util/Random;III)V") && method.name.equals("func_149853_b"))
        ).findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the method IGrowable.grow() in "+srg));

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/block/BlockSapling"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
                        .anyMatch(ins-> {
                            method.instructions.set(ins, new MethodInsnNode(INVOKEVIRTUAL, ins.owner, "MC$grow", ins.desc, false));
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC, "MC$grow", ins.desc, null, null);
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 4);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitVarInsn(ALOAD, 3);
                            wrapper.visitMethodInsn(INVOKEVIRTUAL, ins.owner, growMethod.name, growMethod.desc, false);
                            wrapper.visitInsn(RETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);
                            return true;
                        });
                break;
            }
            else if(method.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/block/BlockSapling"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V"))
                        .anyMatch(ins-> {
                            method.instructions.set(ins, new MethodInsnNode(INVOKEVIRTUAL, ins.owner, "MC$grow", ins.desc, false));
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC, "MC$grow", ins.desc, null, null);
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 5);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 4);
                            wrapper.visitMethodInsn(INVOKEVIRTUAL, ins.owner, growMethod.name, growMethod.desc, false);
                            wrapper.visitInsn(RETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);
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
