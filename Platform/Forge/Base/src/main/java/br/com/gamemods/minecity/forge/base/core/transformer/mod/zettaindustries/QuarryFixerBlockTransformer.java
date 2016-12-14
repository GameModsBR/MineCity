package br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class QuarryFixerBlockTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!"com.bymarcin.zettaindustries.mods.quarryfixer.QuarryFixerBlock".equals(srg))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.desc.equals("(III)Z"))
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .anyMatch(ins -> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC,
                                    "mineCity$setAir", "(Lnet/minecraft/world/World;IIIIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 4);
                            wrapper.visitVarInsn(ILOAD, 5);
                            wrapper.visitVarInsn(ILOAD, 6);
                            wrapper.visitVarInsn(ALOAD, 7);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/zettaindustries/ZettaHooks",
                                    "onQuarryChange",
                                    "(Lnet/minecraft/world/World;IIIIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                                    false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ICONST_0);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKEVIRTUAL, ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new VarInsnNode(ALOAD, 5));
                            method.instructions.insertBefore(ins, list);

                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = srg.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            ins.itf = false;
                            return true;
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.desc.equals("(IIILnet/minecraft/block/Block;)Z"))
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .anyMatch(ins -> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC,
                                    "mineCity$setBlock", "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;IIILnet/minecraft/entity/player/EntityPlayer;)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 5);
                            wrapper.visitVarInsn(ILOAD, 6);
                            wrapper.visitVarInsn(ILOAD, 7);
                            wrapper.visitVarInsn(ALOAD, 8);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/zettaindustries/ZettaHooks",
                                    "onQuarryChange",
                                    "(Lnet/minecraft/world/World;IIIIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                                    false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ICONST_0);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ALOAD, 4);
                            wrapper.visitMethodInsn(INVOKEVIRTUAL, ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            list.add(new VarInsnNode(ALOAD, 5));
                            method.instructions.insertBefore(ins, list);

                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = srg.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            ins.itf = false;
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = ModEnv.saveClass(srg, writer.toByteArray());
        return bytes;
    }
}

