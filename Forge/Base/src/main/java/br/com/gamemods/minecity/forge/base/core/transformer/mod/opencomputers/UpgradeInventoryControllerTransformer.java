package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

public class UpgradeInventoryControllerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(transformedName.equals("li.cil.oc.server.component.UpgradeInventoryController$Drone"))
            return patch(transformedName, "Lli/cil/oc/api/internal/Agent;", basicClass);
        else if(transformedName.equals("li.cil.oc.server.component.UpgradeInventoryController$Robot"))
            return patch(transformedName, "Lli/cil/oc/common/tileentity/Robot;", basicClass);
        return new byte[0];
    }

    private byte[] patch(String name, String sig, byte[] bytes)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/protection/opencomputers/Hosted");
        MethodNode host = new MethodNode(ACC_PUBLIC, "host", "()Ljava/lang/Object;", null, null);
        host.visitCode();
        host.visitVarInsn(ALOAD, 0);
        host.visitFieldInsn(GETFIELD, name.replace('.','/'), "host", sig);
        host.visitInsn(ARETURN);
        host.visitEnd();
        node.methods.add(host);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = ModEnv.saveClass(name, writer.toByteArray());
        return bytes;
    }
}
