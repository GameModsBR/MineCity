package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityXPOrbTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.item.EntityXPOrb"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        method:
        for(MethodNode method : node.methods)
        {
            if(method.access != ACC_PUBLIC || !method.desc.equals("()V"))
                continue;

            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while(iter.hasNext())
            {
                AbstractInsnNode ins = iter.next();
                if(ins.getOpcode() == INVOKEVIRTUAL)
                {
                    MethodInsnNode methodNode = (MethodInsnNode) ins;
                    if(methodNode.owner.equals("net/minecraft/world/World") && methodNode.desc.equals("(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/EntityPlayer;"))
                    {
                        InsnList add = new InsnList();
                        add.add(new VarInsnNode(ALOAD, 0));
                        add.add(new MethodInsnNode(INVOKESTATIC,
                                hookClass, "onXpOrbTargetPlayer", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/item/EntityXPOrb;)Lnet/minecraft/entity/player/EntityPlayer;",
                                false
                        ));
                        method.instructions.insert(ins, add);
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
