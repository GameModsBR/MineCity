package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Referenced(at = PathFinderTransformer.class)
public class NodeProcessorTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.pathfinding.NodeProcessor"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        FieldNode block = node.fields.stream().filter(fd-> fd.desc.equals("Lnet/minecraft/world/IBlockAccess;"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the blockAccess field"));

        FieldNode entity = node.fields.stream().filter(fd-> fd.desc.equals("Lnet/minecraft/entity/EntityLiving;"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the entity field"));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getBlockAccess$MC", "()Lnet/minecraft/world/IBlockAccess;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, "net/minecraft/pathfinding/NodeProcessor", block.name, block.desc);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getEntity$MC", "()Lnet/minecraft/entity/EntityLiving;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, "net/minecraft/pathfinding/NodeProcessor", entity.name, entity.desc);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
