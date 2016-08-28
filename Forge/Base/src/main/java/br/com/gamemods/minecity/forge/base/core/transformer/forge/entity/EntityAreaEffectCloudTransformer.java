package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class EntityAreaEffectCloudTransformer implements IClassTransformer
{
    private String hookClass;

    public EntityAreaEffectCloudTransformer(String hookClass)
    {
        this.hookClass = hookClass.replace('.','/');
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.EntityAreaEffectCloud"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        String owner = EntityLivingBase.class.getName().replace('.','/');

        method:
        for(MethodNode method : node.methods)
        {
            if(!method.desc.equals("()V"))
                continue;

            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while(iter.hasNext())
            {
                AbstractInsnNode ins = iter.next();
                if(ins instanceof MethodInsnNode)
                {
                    MethodInsnNode methodIns = (MethodInsnNode) ins;
                    if(methodIns.owner.equals(owner) && methodIns.desc.equals("(Lnet/minecraft/potion/PotionEffect;)V"))
                    {
                        InsnList add = new InsnList();
                        add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        add.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hookClass,
                                "onPotionApplyEffect",
                                "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/entity/Entity;)V",
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
