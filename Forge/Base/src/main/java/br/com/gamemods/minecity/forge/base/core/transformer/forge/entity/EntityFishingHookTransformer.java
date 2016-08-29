package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.MethodPatcher;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.util.ListIterator;

@MethodPatcher
public class EntityFishingHookTransformer implements IClassTransformer
{
    private String hookClass;
    private String rayTracerClass;

    public EntityFishingHookTransformer(String rayTracerClass, String hookClass)
    {
        this.rayTracerClass = rayTracerClass.replace('.','/');
        this.hookClass = hookClass.replace('.','/');
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityFishHook"))
            return bytes;

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        method:
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("()V"))
            {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while(iter.hasNext())
                {
                    AbstractInsnNode ins = iter.next();
                    if(ins.getOpcode() == Opcodes.GETFIELD)
                    {
                        FieldInsnNode fieldNode = (FieldInsnNode) ins;
                        if(fieldNode.owner.equals(rayTracerClass)
                                && fieldNode.desc.equals("Lnet/minecraft/entity/Entity;"))
                        {
                            InsnList add = new InsnList();
                            add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            add.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    hookClass, "onFishingHookHitEntity",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/projectile/EntityFishHook;)Lnet/minecraft/entity/Entity;",
                                    false
                            ));

                            method.instructions.insert(fieldNode, add);
                            break method;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();

        try(FileOutputStream out = new FileOutputStream(srg+".class"))
        {
            out.write(bytes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return bytes;
    }
}
