package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class UpgradePistonTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.server.component.UpgradePiston"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String hookClass = ModEnv.hookClass.replace('.','/');

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("push"))
            {
                // 1.10.2
                if(!CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/block/BlockPistonBase"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,"mineCity$doMove",
                                    "(Lnet/minecraft/block/BlockPistonBase;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;ZLjava/lang/Object;)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 5);
                            wrapper.visitMethodInsn(INVOKESTATIC, "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks", "getPermissible", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            wrapper.visitMethodInsn(INVOKESTATIC, hookClass, "setPistonMovedBy", "(Ljava/lang/Object;)V", false);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitVarInsn(ALOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 4);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
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
                        .filter(ins-> ins.owner.equals("net/minecraft/block/BlockPistonBase"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;IIII)Z"))
                        .filter(ins-> ins.name.equals("func_150079_i") || ins.name.equals("tryExtend"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,"mineCity$doMove",
                                    "(Lnet/minecraft/block/BlockPistonBase;Lnet/minecraft/world/World;IIIILjava/lang/Object;)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 6);
                            wrapper.visitMethodInsn(INVOKESTATIC, "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks", "getPermissible", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            wrapper.visitMethodInsn(INVOKESTATIC, hookClass, "setPistonMovedBy", "(Ljava/lang/Object;)V", false);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 4);
                            wrapper.visitVarInsn(ILOAD, 5);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
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

        node.interfaces.add("br/com/gamemods/minecity/forge/base/protection/opencomputers/IUpgradePiston");

        MethodNode wrapper = new MethodNode(ACC_PUBLIC, "host", "()Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IEnvironmentHost;", null, null);
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "host", "()Lli/cil/oc/api/network/EnvironmentHost;", false);
        wrapper.visitInsn(ARETURN);
        wrapper.visitEnd();
        node.methods.add(wrapper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
