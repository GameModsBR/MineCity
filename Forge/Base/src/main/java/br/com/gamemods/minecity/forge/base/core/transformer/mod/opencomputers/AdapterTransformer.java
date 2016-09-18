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
public class AdapterTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.common.tileentity.Adapter"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String hookClass = ModEnv.hookClass.replace('.', '/');

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("neighborChanged"))
            {
                // 1.10.2
                if(!CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC, "mineCity$getTileEntity",
                                    "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAdapter;)Lnet/minecraft/tileentity/TileEntity;",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/accessors/world/IWorldServer");
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitMethodInsn(INVOKESTATIC, hookClass, "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;", false);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                                    "onAdapterAccess", "(Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAdapter;Lbr/com/gamemods/minecity/forge/base/accessors/world/IWorldServer;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                    false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitTypeInsn(CHECKCAST, "net/minecraft/tileentity/TileEntity");
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = transformedName.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        }))

                // 1.7.10
                CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;"))
                    .anyMatch(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC, "mineCity$getTileEntity",
                                "(Lnet/minecraft/world/World;IIILbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAdapter;)Lnet/minecraft/tileentity/TileEntity;",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 4);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/accessors/world/IWorldServer");
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(INVOKESTATIC, hookClass, "toPoint", "(III)Lbr/com/gamemods/minecity/api/shape/Point;", false);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                                "onAdapterAccess", "(Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAdapter;Lbr/com/gamemods/minecity/forge/base/accessors/world/IWorldServer;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitVarInsn(ALOAD, 4);
                        wrapper.visitTypeInsn(CHECKCAST, "net/minecraft/tileentity/TileEntity");
                        wrapper.visitInsn(ARETURN);
                        wrapper.visitLabel(label);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(ARETURN);
                        wrapper.visitEnd();
                        node.methods.add(wrapper);

                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                        return true;
                    });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
