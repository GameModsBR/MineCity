package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class EntityPotionTransformer implements IClassTransformer
{
    private String rayTraceClass;
    private String hookClass;
    private int skip;

    public EntityPotionTransformer(String rayTraceClass, String hookClass, int skip)
    {
        this.rayTraceClass = rayTraceClass.replace('.','/');
        this.hookClass = hookClass.replace('.','/');
        this.skip = skip;
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityPotion"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        method:
        for(MethodNode method : node.methods)
        {
            if(!method.desc.equals("(L"+rayTraceClass+";)V"))
                continue;

            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while(iter.hasNext())
            {
                AbstractInsnNode ins = iter.next();
                if(ins instanceof MethodInsnNode)
                {
                    MethodInsnNode methodIns = (MethodInsnNode) ins;
                    if(methodIns.desc.equals("(Lnet/minecraft/potion/PotionEffect;)V"))
                    {
                        if(skip-- > 0)
                            continue;

                        InsnList add = new InsnList();
                        add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hookClass,
                                "onPotionApplyEffect",
                                "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/entity/projectile/EntityPotion;)V",
                                false
                        ));
                        method.instructions.insertBefore(ins, add);
                        method.instructions.remove(ins);
                        break method;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
