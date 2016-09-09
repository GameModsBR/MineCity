package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.ListIterator;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityFishingHookTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityFishHook"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

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
                        if((fieldNode.owner.equals("net/minecraft/entity/util/RayTraceResult") || fieldNode.owner.equals("net/minecraft/entity/util/math/MovingObjectPosition"))
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

        boolean skip = true;
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("()I"))
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == Opcodes.INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/entity/Entity;)Z"))
                        .map(ins -> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue)
                        .forEachOrdered(index -> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    hookClass, "onFishingHookSpawnEntity",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/projectile/EntityFishHook;)Lnet/minecraft/entity/Entity;",
                                    false
                            ));
                            method.instructions.insertBefore(method.instructions.get(index), list);
                        });

            if(method.desc.equals("()V") && Modifier.isProtected(method.access))
            {
                if(skip)
                {
                    skip = false;
                    continue;
                }

                InsnList add = new InsnList();
                add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                add.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        hookClass, "onFishingHookBringEntity",
                        "(Lnet/minecraft/entity/projectile/EntityFishHook;)Z",
                        false
                ));
                LabelNode elseLabel = new LabelNode(new Label());
                add.add(new JumpInsnNode(Opcodes.IFEQ, elseLabel));
                add.add(new InsnNode(Opcodes.RETURN));
                add.add(elseLabel);

                method.instructions.insertBefore(method.instructions.get(0), add);
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
