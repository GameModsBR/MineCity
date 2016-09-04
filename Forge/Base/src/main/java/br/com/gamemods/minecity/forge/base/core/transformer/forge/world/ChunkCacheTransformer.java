package br.com.gamemods.minecity.forge.base.core.transformer.forge.world;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
public class ChunkCacheTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.world.ChunkCache"))
            return bytes;

        String owner = "net/minecraft/world/ChunkCache";

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        String x = null;
        String z = null;
        String array = null;
        String world = null;

        for(FieldNode field : node.fields)
            switch(field.desc)
            {
                case "I":
                    if(x == null)
                        x = field.name;
                    else
                        z = field.name;
                    break;
                case "[[Lnet/minecraft/world/chunk/Chunk;":
                    array = field.name;
                    break;
                case "Lnet/minecraft/world/World;":
                    world = field.name;
                    break;
            }

        Objects.requireNonNull(x, "Failed to find the chunkX field");
        Objects.requireNonNull(z, "Failed to find the chunkZ field");
        Objects.requireNonNull(array, "Failed to find the chunkArray field");
        Objects.requireNonNull(world, "Failed to find the worldObj field");

        MethodNode method = new MethodNode(ACC_PUBLIC, "getChunkX", "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, x, "I");
        method.visitInsn(IRETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getChunkZ", "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, z, "I");
        method.visitInsn(IRETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getChunks", "()[[Lnet/minecraft/world/chunk/Chunk;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, array, "[[Lnet/minecraft/world/chunk/Chunk;");
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getWorld", "()Lnet/minecraft/world/World;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, world, "Lnet/minecraft/world/World;");
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/accessors/world/IChunkCache");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
