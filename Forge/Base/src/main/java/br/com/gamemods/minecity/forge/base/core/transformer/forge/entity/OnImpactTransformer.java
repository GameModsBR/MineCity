package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class OnImpactTransformer implements IClassTransformer
{
    private String hookClass = ModEnv.hookClass.replace('.','/');

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(srg.startsWith("br.com.gamemods.minecity"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        AtomicReference<MethodInsnNode> patched = new AtomicReference<>();
        node.methods.stream()
                .flatMap(method -> CollectionUtil.stream(method.instructions.iterator()))
                .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                .filter(ins -> ins.name.equals("func_70184_a") || ins.name.equals("func_70227_a") || ins.name.equals("onImpact"))
                .filter(ins -> ins.desc.equals("(Lnet/minecraft/util/math/RayTraceResult;)V") || ins.desc.equals("(Lnet/minecraft/util/MovingObjectPosition;)V"))
                .forEachOrdered(ins -> {
                    if(patched.get() == null)
                        patched.set(new MethodInsnNode(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf));

                    ins.setOpcode(INVOKESTATIC);
                    if(ins.owner.equals("net/minecraft/entity/projectile/EntityThrowable"))
                    {
                        ins.name = "onThrowableImpact";
                        ins.desc = "(Lnet/minecraft/entity/projectile/EntityThrowable;"+ins.desc.substring(1);
                    }
                    else
                    {
                        ins.name = "onFireBallImpact";
                        ins.desc = "(Lnet/minecraft/entity/projectile/EntityFireball;"+ins.desc.substring(1);
                    }
                    ins.owner = hookClass;
                });

        MethodInsnNode methodInsnNode = patched.get();
        if(methodInsnNode == null)
            return bytes;

        MethodNode accessor = new MethodNode(ACC_PUBLIC, "mineCityOnImpact", "(Ljava/lang/Object;)V", null, null);
        accessor.visitCode();
        accessor.visitVarInsn(ALOAD, 0);
        accessor.visitVarInsn(ALOAD, 1);
        accessor.visitTypeInsn(CHECKCAST, methodInsnNode.desc.substring(2, methodInsnNode.desc.length()-3));
        accessor.visitMethodInsn(methodInsnNode.getOpcode(), methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc, methodInsnNode.itf);
        accessor.visitInsn(RETURN);
        accessor.visitEnd();

        node.methods.add(accessor);
        node.interfaces.add("br/com/gamemods/minecity/forge/base/accessors/entity/projectile/OnImpact");

        System.out.println("\n |- "+srg+" had calls to EntityThrowable.onImpact() and EntityFireball.onImpact() wrapped");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
