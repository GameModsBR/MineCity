package br.com.gamemods.minecity.forge.core.transformer.forge;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Arrays;

/**
 * Makes {@link net.minecraft.entity.player.EntityPlayerMP EntityPlayerMP}
 * implements {@link br.com.gamemods.minecity.forge.accessors.IEntityPlayerMP IEntityPlayerMP}
 * <pre><code>
 *     public class EntityPlayerMP extends EntityPlayer
 *         implements IEntityPlayerMP // <- Added
 *     {
 *         // ... original fields and methods
 *         public ForgePlayer mineCity;
 *         public ForgePlayer getMineCityPlayer(){ return this.mineCity; }
 *         public void setMineCityPlayer(ForgePlayer player){ this.mineCity = player; }
 *     }
 * </code></pre>
 */
public class EntityPlayerMPTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String srgName, byte[] bytes)
    {
        if(srgName.equals("net.minecraft.entity.player.EntityPlayerMP"))
        {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);
            String forgePlayer = "br/com/gamemods/minecity/forge/command/ForgePlayer";
            String iEntityPlayerMP = "br/com/gamemods/minecity/forge/accessors/IEntityPlayerMP";
            String entityPlayerMP = "net/minecraft/entity/player/EntityPlayerMP";

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                    interfaces[interfaces.length-1] = iEntityPlayerMP;
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            writer.visitField(Opcodes.ACC_PUBLIC, "mineCity", "L"+forgePlayer+";", null, null).visitEnd();

            MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "getMineCityPlayer", "()L"+forgePlayer+";", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, entityPlayerMP, "mineCity", "L"+forgePlayer+";");
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 2);
            methodVisitor.visitEnd();

            methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, "setMineCityPlayer", "(L"+forgePlayer+";)V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, entityPlayerMP, "mineCity", "L"+forgePlayer+";");
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();

            bytes = writer.toByteArray();
        }

        return bytes;
    }
}
