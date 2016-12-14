package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@MethodPatcher
public class EntityPotionTransformer implements IClassTransformer
{
    private int skip;
    private boolean cloud;

    public EntityPotionTransformer(int skip)
    {
        this.skip = skip;
        cloud = skip == 1;
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityPotion"))
            return bytes;

        String rayTraceClass = ModEnv.rayTraceResultClass.replace('.','/');
        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/accessors/entity/projectile/EntityProjectile");

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
                                "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/entity/Entity;)V",
                                false
                        ));
                        method.instructions.insertBefore(ins, add);
                        method.instructions.remove(ins);
                        break;
                    }
                }
            }

            if(cloud)
            {
                iter = method.instructions.iterator();
                int index = -1;
                while(iter.hasNext())
                {
                    index++;
                    AbstractInsnNode ins = iter.next();
                    if(ins instanceof MethodInsnNode)
                    {
                        MethodInsnNode methodIns = (MethodInsnNode) ins;
                        if(methodIns.owner.equals("net/minecraft/entity/EntityAreaEffectCloud")
                                && methodIns.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;)V"))
                        {
                            VarInsnNode var = (VarInsnNode) method.instructions.get(index - 3);
                            InsnList add = new InsnList();
                            add.add(new VarInsnNode(Opcodes.ALOAD, var.var));
                            add.add(new TypeInsnNode(Opcodes.CHECKCAST, "br/com/gamemods/minecity/forge/base/accessors/entity/projectile/Projectile"));
                            add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            add.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                    "br/com/gamemods/minecity/forge/base/accessors/entity/projectile/Projectile",
                                    "getShooter",
                                    "()Lbr/com/gamemods/minecity/forge/base/accessors/entity/projectile/ProjectileShooter;",
                                    true
                            ));
                            add.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                    "br/com/gamemods/minecity/forge/base/accessors/entity/projectile/Projectile",
                                    "setShooter",
                                    "(Lbr/com/gamemods/minecity/forge/base/accessors/entity/projectile/ProjectileShooter;)V",
                                    true
                            ));
                            method.instructions.insert(methodIns, add);
                            break;
                        }
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
