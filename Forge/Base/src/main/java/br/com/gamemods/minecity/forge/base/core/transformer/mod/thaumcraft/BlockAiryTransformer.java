package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class BlockAiryTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockAiryTransformer()
    {
        super("thaumcraft.common.blocks.BlockAiry");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$attack",
                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLnet/minecraft/world/World;III)Z",
                null, null);
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(FLOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ILOAD, 6);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onAiryDamage", wrapper.desc, false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(IRETURN);
        wrapper.visitLabel(label);
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(FLOAD, 2);
        AtomicBoolean finalized = new AtomicBoolean();

        MethodNode potionWrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$addPotion",
                "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/world/World;III)V",
                null, null
        );
        potionWrapper.visitCode();
        potionWrapper.visitVarInsn(ALOAD, 0);
        potionWrapper.visitVarInsn(ALOAD, 2);
        potionWrapper.visitVarInsn(ILOAD, 3);
        potionWrapper.visitVarInsn(ILOAD, 4);
        potionWrapper.visitVarInsn(ILOAD, 5);
        potionWrapper.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onAiryApplyPotion", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/world/World;III)Z", false
        );
        label = new Label();
        potionWrapper.visitJumpInsn(IFEQ, label);
        potionWrapper.visitInsn(RETURN);
        potionWrapper.visitLabel(label);
        potionWrapper.visitVarInsn(ALOAD, 0);
        potionWrapper.visitVarInsn(ALOAD, 1);

        MethodNode exhaustion = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$addExhaustion",
                "(Lnet/minecraft/entity/player/EntityPlayer;FLnet/minecraft/world/World;III)V",
                null, null
        );
        exhaustion.visitCode();
        exhaustion.visitVarInsn(ALOAD, 0);
        exhaustion.visitVarInsn(ALOAD, 2);
        exhaustion.visitVarInsn(ILOAD, 3);
        exhaustion.visitVarInsn(ILOAD, 4);
        exhaustion.visitVarInsn(ILOAD, 5);
        exhaustion.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onAiryApplyPotion", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/world/World;III)Z", false
        );
        label = new Label();
        exhaustion.visitJumpInsn(IFEQ, label);
        exhaustion.visitInsn(RETURN);
        exhaustion.visitLabel(label);
        exhaustion.visitVarInsn(ALOAD, 0);
        exhaustion.visitVarInsn(FLOAD, 1);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/entity/Entity"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z"))
                        .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                        .map(ins-> (MethodInsnNode) method.instructions.get(ins))
                        .forEachOrdered(ins-> {
                            if(!finalized.get())
                            {
                                finalized.set(true);
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitInsn(IRETURN);
                                wrapper.visitEnd();
                            }

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
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

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/entity/EntityLivingBase"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/potion/PotionEffect;)V"))
                        .anyMatch(ins-> {
                            potionWrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            potionWrapper.visitInsn(RETURN);
                            potionWrapper.visitEnd();

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            method.instructions.insertBefore(ins, list);
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = potionWrapper.name;
                            ins.desc = potionWrapper.desc;
                            return true;
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/entity/player/EntityPlayer"))
                        .filter(ins-> ins.desc.equals("(F)V"))
                        .anyMatch(ins-> {
                            exhaustion.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            exhaustion.visitInsn(RETURN);
                            exhaustion.visitEnd();

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ILOAD, 2));
                            list.add(new VarInsnNode(ILOAD, 3));
                            list.add(new VarInsnNode(ILOAD, 4));
                            method.instructions.insertBefore(ins, list);
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = exhaustion.name;
                            ins.desc = exhaustion.desc;
                            return true;
                        });
                break;
            }
        }

        node.methods.add(wrapper);
        node.methods.add(potionWrapper);
        node.methods.add(exhaustion);
    }
}
