package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class TankWorldControlDClassTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.server.component.traits.TankWorldControl$class"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$fluidHandlerAt",
                "(Lli/cil/oc/util/FluidUtils$;Lli/cil/oc/util/BlockPosition;Lli/cil/oc/server/component/traits/TankWorldControl;)Lscala/Option;",
                null, null
        );
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/opencomputers/IAgentComponent");
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "world", "()Lscala/Option;", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "x", "()I", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "y", "()I", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "z", "()I", false);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                "onRobotAccessTank", "(Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAgentComponent;Lscala/Option;III)Z",
                false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitFieldInsn(GETSTATIC, "scala/None$", "MODULE$", "Lscala/None$;");
        wrapper.visitInsn(ARETURN);
        wrapper.visitLabel(label);
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/FluidUtils$", "fluidHandlerAt",
                "(Lli/cil/oc/util/BlockPosition;)Lscala/Option;", false
        );
        wrapper.visitInsn(ARETURN);
        wrapper.visitEnd();

        for(MethodNode method : node.methods)
        {
            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.name.equals("fluidHandlerAt"))
                    .filter(ins-> ins.owner.equals("li/cil/oc/util/FluidUtils$"))
                    .filter(ins-> ins.desc.equals("(Lli/cil/oc/util/BlockPosition;)Lscala/Option;"))
                    .anyMatch(ins-> {
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                        return true;
                    });
        }

        node.methods.add(wrapper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
