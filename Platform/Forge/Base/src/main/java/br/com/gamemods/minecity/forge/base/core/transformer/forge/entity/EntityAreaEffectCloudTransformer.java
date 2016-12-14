package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityAreaEffectCloudTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.EntityAreaEffectCloud"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        String owner = "net/minecraft/entity/EntityLivingBase";

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
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
