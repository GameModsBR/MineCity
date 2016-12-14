package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class EntityLivingBaseTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityLivingBaseTransformer()
    {
        super("net.minecraft.entity.EntityLivingBase");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        List<MethodNode> wrappers = new ArrayList<>(1);
        for(MethodNode method : node.methods)
        {
            if((method.name.equals("func_71038_i") || method.name.equals("swingItem")) && method.desc.equals("()V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/item/Item"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)Z"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,
                                    "mineCity$onEntitySwing",
                                    "(Lnet/minecraft/item/Item;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitTypeInsn(CHECKCAST, "java/lang/Object");
                            String stack = "br.com.gamemods.minecity.forge.base.accessors.item.IItemStack".replace('.','/');
                            String item = "br.com.gamemods.minecity.forge.base.accessors.item.IItem".replace('.','/');
                            String living = "br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase".replace('.','/');
                            String hook = ModEnv.hookClass.replace('.','/');
                            wrapper.visitTypeInsn(CHECKCAST, stack);
                            wrapper.visitMethodInsn(INVOKESTATIC, hook, "onLivingSwing", "(L"+item+";L"+living+";L"+stack+";)Z", false);
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ICONST_1);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ALOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitEnd();
                            wrappers.add(wrapper);

                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        });
                break;
            }
        }

        node.methods.addAll(wrappers);
    }
}
