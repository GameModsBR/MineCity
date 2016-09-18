package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class TransposerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(transformedName.equals("li.cil.oc.server.component.Transposer$Block"))
            return patch(transformedName, "host", "()Lli/cil/oc/common/tileentity/Transposer;", basicClass, true);
        if(transformedName.equals("li.cil.oc.server.component.Transposer$Upgrade"))
            return patch(transformedName, "host", "()Lli/cil/oc/api/network/EnvironmentHost;", basicClass, false);
        if(transformedName.equals("li.cil.oc.server.component.Transposer$Common"))
            return common(transformedName, basicClass);
        return basicClass;
    }

    private byte[] common(String name, byte[] bytes)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/protection/opencomputers/Hosted");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = ModEnv.saveClass(name, writer.toByteArray());
        return bytes;
    }

    private byte[] patch(String name, String method, String desc, byte[] bytes, boolean pos)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        MethodNode host = new MethodNode(ACC_PUBLIC, "host", "()Ljava/lang/Object;", null, null);
        host.visitCode();
        host.visitVarInsn(ALOAD, 0);
        host.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), method, desc, false);
        /*if(pos)
        {
            host.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "world", "()Lscala/Option;", false);
            host.visitVarInsn(ALOAD, 0);
            host.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), method, desc, false);
            host.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "x", "()I", false);
            host.visitVarInsn(ALOAD, 0);
            host.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), method, desc, false);
            host.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "y", "()I", false);
            host.visitVarInsn(ALOAD, 0);
            host.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), method, desc, false);
            host.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "z", "()I", false);
            host.visitMethodInsn(INVOKESTATIC, ModEnv.hookClass.replace('.','/'), "toPos", "(Ljava/lang/Object;III)Lbr/com/gamemods/minecity/api/world/BlockPos;", false);
        }*/
        host.visitInsn(ARETURN);
        host.visitEnd();
        node.methods.add(host);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = ModEnv.saveClass(name, writer.toByteArray());
        return bytes;
    }
}
