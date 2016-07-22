package br.com.gamemods.minecity.forge.core.transformer.forge;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Arrays;

/**
 * Makes {@link net.minecraft.world.chunk.Chunk} implements {@link br.com.gamemods.minecity.forge.accessors.IChunk}
 * <pre><code>
 *     public class Chunk
 *         implements IChunk // <- Added
 *     {
 *         // ... original fields and methods
 *         public ClaimedChunk mineCity;
 *         public ClaimedChunk getMineCityClaim(){ return this.mineCity; }
 *         public void setMineCityClaim(ClaimedChunk claim){ this.mineCity = claim; }
 *     }
 * </code></pre>
 */
public class ChunkTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String srgName, byte[] bytes)
    {
        if(srgName.equals("net.minecraft.world.chunk.Chunk"))
        {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);
            String claimedChunk = "br/com/gamemods/minecity/structure/ClaimedChunk";
            String iChunk = "br/com/gamemods/minecity/forge/accessors/IChunk";
            String chunk = "net/minecraft/world/chunk/Chunk";

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                    interfaces[interfaces.length-1] = iChunk;
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            writer.visitField(Opcodes.ACC_PUBLIC, "mineCity", "L"+claimedChunk+";", null, null).visitEnd();

            MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "getMineCityClaim", "()L"+claimedChunk+";", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, chunk, "mineCity", "L"+claimedChunk+";");
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 2);
            methodVisitor.visitEnd();

            methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "setMineCityClaim", "(L"+claimedChunk+";)V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, chunk, "mineCity", "L"+claimedChunk+";");
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();

            bytes = writer.toByteArray();
        }

        return bytes;
    }
}
