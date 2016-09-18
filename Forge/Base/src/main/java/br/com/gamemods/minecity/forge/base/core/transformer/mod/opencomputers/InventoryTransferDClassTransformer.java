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

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class InventoryTransferDClassTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.server.component.traits.InventoryTransfer$class"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String hookClass = ModEnv.hookClass.replace('.', '/');

        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC, "mineCity$inventoryAt",
                "(Lli/cil/oc/util/InventoryUtils$;Lli/cil/oc/util/BlockPosition;Lli/cil/oc/server/component/traits/InventoryTransfer;)Lscala/Option;",
                null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/opencomputers/Hosted");
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "world", "()Lscala/Option;", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "x", "()I", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "y", "()I", false);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "z", "()I", false);
        wrapper.visitMethodInsn(INVOKESTATIC, hookClass, "toPos", "(Ljava/lang/Object;III)Lbr/com/gamemods/minecity/api/world/BlockPos;", false);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                "onInventoryTransferAccess",
                "(Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/Hosted;Lbr/com/gamemods/minecity/api/world/BlockPos;)Lscala/Option;",
                false
        );
        Label label = new Label();
        wrapper.visitInsn(DUP);
        wrapper.visitJumpInsn(IFNULL, label);
        wrapper.visitInsn(ARETURN);
        wrapper.visitLabel(label);
        wrapper.visitInsn(POP);
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/InventoryUtils$", "inventoryAt", "(Lli/cil/oc/util/BlockPosition;)Lscala/Option;", false);
        wrapper.visitInsn(ARETURN);
        wrapper.visitEnd();

        for(MethodNode method : node.methods)
        {
            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("li/cil/oc/util/InventoryUtils$"))
                    .filter(ins-> ins.name.equals("inventoryAt"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins -> {
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });
        }

        node.methods.add(wrapper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
