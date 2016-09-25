package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

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
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
@Referenced
public class EntityPechBlastTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public EntityPechBlastTransformer()
    {
        super("thaumcraft.common.entities.projectile.EntityPechBlast");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String trace = ModEnv.rayTraceResultClass.replace('.','/');

        AtomicReference<MethodNode> wrapperNode = new AtomicReference<>();
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(L"+trace+";)V"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/entity/EntityLivingBase"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/potion/PotionEffect;)V"))
                        .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                        .map(i-> method.instructions.get(i)).map(MethodInsnNode.class::cast)
                        .forEachOrdered(ins-> {
                            if(wrapperNode.get() == null)
                            {
                                MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,
                                        "mineCity$addPotionEffect",
                                        "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/entity/Entity;)V",
                                        null, null
                                );
                                wrapperNode.set(wrapper);
                                wrapper.visitCode();
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ALOAD, 2);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks"
                                                .replace('.', '/'),
                                        "onEntityApplyNegativeEffect",
                                        "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;)Z",
                                        false
                                );
                                Label label = new Label();
                                wrapper.visitJumpInsn(IFEQ, label);
                                wrapper.visitInsn(RETURN);
                                wrapper.visitLabel(label);
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitInsn(RETURN);
                                wrapper.visitEnd();
                            }

                            MethodNode wrapper = wrapperNode.get();
                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                        });
                break;
            }
        }

        node.methods.add(Objects.requireNonNull(wrapperNode.get(), "The patch has failed"));
    }
}
