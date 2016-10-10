package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class BlockTaintTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockTaintTransformer()
    {
        super("thaumcraft.common.blocks.BlockTaint");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        AtomicReference<MethodNode> setBlock = new AtomicReference<>();

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("tryToFall"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(ILOAD, 3));
                list.add(new VarInsnNode(ILOAD, 4));
                list.add(new VarInsnNode(ILOAD, 5));
                list.add(new VarInsnNode(ILOAD, 6));
                list.add(new VarInsnNode(ILOAD, 7));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                        "onTaintTryToFall",
                        "(Lnet/minecraft/world/World;IIIIII)Z",
                        false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(IRETURN));
                list.add(labelNode);
                method.instructions.insert(list);
                break;
            }
            else if(method.desc.equals("(Lnet/minecraft/world/World;IIILjava/util/Random;)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("thaumcraft/common/blocks/BlockTaintFibres"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/world/World;III)Z"))
                        .filter(ins -> ins.name.equals("spreadFibres"))
                        .anyMatch(ins ->
                        {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            method.instructions.insertBefore(ins, list);
                            ins.name = "mineCity$spreadFibres";
                            ins.desc = "(Lnet/minecraft/world/World;IIIIII)Z";
                            return true;
                        });

                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                            .filter(ins -> ins.owner.equals("net/minecraft/world/World"))
                            .filter(ins -> ins.desc.equals("(IIILnet/minecraft/block/Block;II)Z"))
                            .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                            .map(i-> (MethodInsnNode) method.instructions.get(i))
                            .forEachOrdered(ins -> {
                                MethodNode wrapper = setBlock.get();
                                if(wrapper == null)
                                {
                                    setBlock.set(wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,
                                            "mineCity$setBlock",
                                            "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;IIIII)Z",
                                            null, null
                                    ));
                                    wrapper.visitCode();
                                    wrapper.visitVarInsn(ALOAD, 0);
                                    wrapper.visitVarInsn(ILOAD, 1);
                                    wrapper.visitVarInsn(ILOAD, 2);
                                    wrapper.visitVarInsn(ILOAD, 3);
                                    wrapper.visitVarInsn(ILOAD, 7);
                                    wrapper.visitVarInsn(ILOAD, 8);
                                    wrapper.visitVarInsn(ILOAD, 9);
                                    wrapper.visitMethodInsn(INVOKESTATIC,
                                            "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                            "onBlockChangeOther",
                                            "(Lnet/minecraft/world/World;IIIIII)Z",
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
                                    wrapper.visitVarInsn(ILOAD, 5);
                                    wrapper.visitVarInsn(ILOAD, 6);
                                    wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                    wrapper.visitInsn(IRETURN);
                                    wrapper.visitEnd();
                                }

                                InsnList list = new InsnList();
                                list.add(new VarInsnNode(ILOAD, 2));
                                list.add(new VarInsnNode(ILOAD, 3));
                                list.add(new VarInsnNode(ILOAD, 4));
                                method.instructions.insertBefore(ins, list);
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = name.replace('.','/');
                                ins.name = wrapper.name;
                                ins.desc = wrapper.desc;
                            });
            }
        }

        node.methods.add(Objects.requireNonNull(setBlock.get(), "mineCity$setBlock was not generated"));
    }
}
