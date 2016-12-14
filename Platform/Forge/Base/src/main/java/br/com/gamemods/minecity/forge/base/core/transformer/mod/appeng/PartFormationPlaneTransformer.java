package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.*;

public class PartFormationPlaneTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public PartFormationPlaneTransformer()
    {
        super("appeng.parts.automation.PartFormationPlane");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String hook = "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/');
        String self = name.replace('.', '/');
        MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$onItemUse",
                "(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFFL"+self+";)Z",
                null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 11);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ILOAD, 6);
        wrapper.visitVarInsn(ILOAD, 7);
        wrapper.visitMethodInsn(INVOKESTATIC,
                hook, "onPrePlace",
                "(Lbr/com/gamemods/minecity/forge/base/protection/appeng/IAEBasePart;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIII)Z",
                false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(IRETURN);
        wrapper.visitLabel(label);
        AtomicBoolean finalized = new AtomicBoolean();

        MethodNode spawn = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$spawnEntity", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;L"+self+";)Z", null, null);
        spawn.visitCode();
        spawn.visitVarInsn(ALOAD, 2);
        spawn.visitVarInsn(ALOAD, 1);
        spawn.visitMethodInsn(INVOKESTATIC, hook, "onSpawn", "(Lbr/com/gamemods/minecity/forge/base/protection/appeng/IAEBasePart;Lnet/minecraft/entity/Entity;)Z", false);
        label = new Label();
        spawn.visitJumpInsn(IFEQ, label);
        spawn.visitInsn(ICONST_0);
        spawn.visitInsn(IRETURN);
        spawn.visitLabel(label);
        AtomicBoolean spawnFinalized = new AtomicBoolean();

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("injectItems"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/item/Item"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z"))
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).map(i-> (MethodInsnNode) method.instructions.get(i))
                        .forEachOrdered(ins-> {
                            if(!finalized.getAndSet(true))
                            {
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitVarInsn(ALOAD, 2);
                                wrapper.visitVarInsn(ALOAD, 3);
                                wrapper.visitVarInsn(ILOAD, 4);
                                wrapper.visitVarInsn(ILOAD, 5);
                                wrapper.visitVarInsn(ILOAD, 6);
                                wrapper.visitVarInsn(ILOAD, 7);
                                wrapper.visitVarInsn(FLOAD, 8);
                                wrapper.visitVarInsn(FLOAD, 9);
                                wrapper.visitVarInsn(FLOAD, 10);
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            }

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = self;
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/entity/Entity;)Z"))
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).map(i-> (MethodInsnNode) method.instructions.get(i))
                        .forEachOrdered(ins-> {
                            if(!spawnFinalized.getAndSet(true))
                            {
                                spawn.visitVarInsn(ALOAD, 0);
                                spawn.visitVarInsn(ALOAD, 1);
                                spawn.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                spawn.visitInsn(IRETURN);
                                spawn.visitEnd();
                            }

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = self;
                            ins.name = spawn.name;
                            ins.desc = spawn.desc;
                        });
                break;
            }
        }

        wrapper.visitVarInsn(ALOAD, 11);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ILOAD, 6);
        wrapper.visitVarInsn(ILOAD, 7);
        wrapper.visitMethodInsn(INVOKESTATIC,
                hook, "onPostPlace",
                "(ZLbr/com/gamemods/minecity/forge/base/protection/appeng/IAEBasePart;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIII)Z",
                false
        );
        wrapper.visitInsn(IRETURN);
        wrapper.visitEnd();

        node.methods.add(wrapper);
        node.methods.add(spawn);
    }
}
