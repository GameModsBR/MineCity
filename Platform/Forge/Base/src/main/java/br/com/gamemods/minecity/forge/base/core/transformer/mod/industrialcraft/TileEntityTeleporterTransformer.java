package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class TileEntityTeleporterTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public TileEntityTeleporterTransformer()
    {
        super("ic2.core.block.machine.tileentity.TileEntityTeleporter");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        name = name.replace('.','/');
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("teleport"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks"
                            .replace('.','/'),
                        "onTeleport",
                        "(Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.ITileEntityTeleporter;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;)Z"
                            .replace('.','/'),
                        false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(RETURN));
                list.add(labelNode);
                method.instructions.insert(list);
                break;
            }
        }

        if(ModEnv.seven)
        {
            node.methods.add(createSevenMethod("targetX", "I", "getTargetX", name));
            node.methods.add(createSevenMethod("targetY", "I", "getTargetY", name));
            node.methods.add(createSevenMethod("targetZ", "I", "getTargetZ", name));
            node.methods.add(createSevenMethod("targetSet", "Z", "isTargetSet", name));
        }
        else
        {
            node.methods.add(createFrostMethod("getTargetX", "x", name));
            node.methods.add(createFrostMethod("getTargetY", "y", name));
            node.methods.add(createFrostMethod("getTargetZ", "z", name));
            MethodNode method = new MethodNode(ACC_PUBLIC, "isTargetSet", "()Z", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, name, "target", "Lnet/minecraft/util/math/BlockPos;");
            Label label = new Label();
            method.visitJumpInsn(IFNULL, label);
            method.visitInsn(ICONST_0);
            method.visitInsn(IRETURN);
            method.visitLabel(label);
            method.visitInsn(ICONST_1);
            method.visitInsn(IRETURN);
            method.visitEnd();
            node.methods.add(method);
        }
    }

    private MethodNode createFrostMethod(String methodName, String pointField, String owner)
    {
        MethodNode method = new MethodNode(ACC_PUBLIC, methodName, "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, "target", "Lnet/minecraft/util/math/BlockPos;");
        method.visitInsn(DUP);
        Label label = new Label();
        method.visitJumpInsn(IFNULL, label);
        method.visitInsn(POP);
        method.visitInsn(ICONST_0);
        method.visitInsn(IRETURN);
        method.visitLabel(label);
        method.visitMethodInsn(INVOKESTATIC, ModEnv.hookClass.replace('.','/'), "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;", false);
        method.visitFieldInsn(GETFIELD, "br/com/gamemods/minecity/api/shape/Point", pointField, "I");
        method.visitInsn(IRETURN);
        method.visitEnd();
        return method;
    }

    private MethodNode createSevenMethod(String field, String sig, String methodName, String owner)
    {
        MethodNode method = new MethodNode(ACC_PUBLIC, methodName, "()"+sig, null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, owner, field, sig);
        method.visitInsn(IRETURN);
        method.visitEnd();
        return method;
    }
}
