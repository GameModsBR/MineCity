package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

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
public class TileRobotProxyTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String transformedName, byte[] basicClass)
    {
        if(!"li.cil.oc.common.tileentity.RobotProxy".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String itf = "br.com.gamemods.minecity.forge.base.protection.opencomputers.IRobotProxyTile".replace('.','/');
        String name = "li.cil.oc.common.tileentity.RobotProxy".replace('.','/');
        node.interfaces.add(itf);

        MethodNode method = new MethodNode(ACC_PUBLIC, "robotTile", "()Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IRobotTile;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEVIRTUAL, name, "robot", "()Lli/cil/oc/common/tileentity/Robot;", false);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
