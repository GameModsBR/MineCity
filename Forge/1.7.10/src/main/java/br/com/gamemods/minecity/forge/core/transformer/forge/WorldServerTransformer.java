package br.com.gamemods.minecity.forge.core.transformer.forge;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Arrays;

/**
 * Makes {@link net.minecraft.world.WorldServer} implements {@link br.com.gamemods.minecity.forge.accessors.IWorldServer}
 * <pre><code>
 *     public class WorldServer extends World
 *         implements IWorldServer // <- Added
 *     {
 *         // ... original fields and methods
 *         public WorldDim mineCity;
 *         public WorldDim getMineCityWorld(){ return this.mineCity; }
 *         public void setMineCityWorld(WorldDim world){ this.mineCity = world; }
 *     }
 * </code></pre>
 */
public class WorldServerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String srgName, byte[] bytes)
    {
        if(srgName.equals("net.minecraft.world.WorldServer"))
        {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);
            String worldDim = "br/com/gamemods/minecity/api/world/WorldDim";
            String iWorldServer = "br/com/gamemods/minecity/forge/accessors/IWorldServer";
            String worldServer = "net/minecraft/world/WorldServer";

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                    interfaces[interfaces.length-1] = iWorldServer;
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            writer.visitField(Opcodes.ACC_PUBLIC, "mineCity", "L"+worldDim+";", null, null).visitEnd();

            MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "getMineCityWorld", "()L"+worldDim+";", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, worldServer, "mineCity", "L"+worldDim+";");
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 2);
            methodVisitor.visitEnd();

            methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "setMineCityWorld", "(L"+worldDim+";)V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, worldServer, "mineCity", "L"+worldDim+";");
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();

            bytes = writer.toByteArray();
        }

        return bytes;
    }
}
