package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wraps all calls to entity.setFire(int) in methods to MineCityHook.onIgnite(entity, int, this, ClassName.class, "methodName", "methodSignature")<br/>
 * Example:
 * <code><pre>
 *     public class Anything {
 *         public Object something;
 *
 *         public void anyNonStaticMethod(Entity.. anySignature) throws Exception {
 *             Entity anyEntityVariable = anySignature[0];
 *             // Any logic before
 *             anyEntityVariable.setFire(5);
 *             // Any logic after
 *             int anyWay = 5;
 *             anySignature[3].setFire((int)Math.sqrt((anyWay * 5)/2));
 *             // Including casts
 *             ((Entity) something).setFire(3);
 *             // Will not work with reflections
 *             Entity.class.getDeclaredMethod("setFire", Integer.TYPE).invoke(something, 5);
 *         }
 *     }
 * </pre></code>
 * <p>Will be transformed to:
 * <pre><code>
 *     public class Anything {
 *         public Object something;
 *
 *         public void anyNonStaticMethod(Entity.. anySignature) throws Exception {
 *             Entity anyEntityVariable = anySignature[0];
 *             // Any logic before
 *             MineCityHook.onIgnite(anyEntityVariable, 5, this, Anything.class, "anyNonStaticMethod", "([Lnet/minecraft/entity/Entity;)V");
 *             // Any logic after
 *             int anyWay = 5;
 *             MineCityHook.onIgnite(anySignature[3], (int)Math.sqrt((anyWay * 5)/2), this, Anything.class, "anyNonStaticMethod", "([Lnet/minecraft/entity/Entity;)V");
 *             // Including casts
 *             MineCityHook.onIgnite((Entity) something, 3, this, Anything.class, "anyNonStaticMethod", "([Lnet/minecraft/entity/Entity;)V");
 *             // Will not work with reflections
 *             Entity.class.getDeclaredMethod("setFire", Integer.TYPE).invoke(something, 5);
 *         }
 *     }
 * </code></pre>
 * <p>On Vanilla Minecraft + Forge only EntityMob, EntityZombie, EntityPlayer, EntityArrow and EntitySmallFireball are patched.</p>
 */
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityIgnitionTransformer implements IClassTransformer
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

        String javaName = srg.replace('.','/');

        Map<String, MethodNode> wrappers = new HashMap<>();
        for(MethodNode method : node.methods)
        {
            if(method.name.toLowerCase().contains("minecity"))
                continue;

            boolean stat = (method.access & ACC_STATIC) > 0;

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.desc.equals("(I)V"))
                    .filter(ins-> ins.name.equals("func_70015_d") || ins.name.equals("setFire"))
                    .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                    .map(i-> method.instructions.get(i)).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = wrappers.get(ins.owner);
                        if(wrapper == null)
                        {
                            wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$"+ins.owner.replaceAll("[^a-zA-Z0-9]","\\$")+"$setFire",
                                    "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V",
                                    null, null
                            );
                            wrappers.put(ins.owner, wrapper);
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitTypeInsn(INSTANCEOF, "net/minecraft/entity/Entity");
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitTypeInsn(CHECKCAST, "net/minecraft/entity/Entity");
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ALOAD, 2);
                            wrapper.visitVarInsn(ALOAD, 3);
                            wrapper.visitVarInsn(ALOAD, 4);
                            wrapper.visitVarInsn(ALOAD, 5);
                            wrapper.visitVarInsn(ALOAD, 6);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    hookClass, "onIgnite",
                                    "(Lnet/minecraft/entity/Entity;ILjava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Z",
                                    false
                            );
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(RETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitTypeInsn(CHECKCAST, ins.owner);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(RETURN);
                            wrapper.visitEnd();
                        }

                        InsnList list = new InsnList();
                        if(stat)
                            list.add(new InsnNode(ACONST_NULL));
                        else
                            list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new LdcInsnNode(Type.getObjectType(javaName)));
                        list.add(new LdcInsnNode(method.name));
                        list.add(new LdcInsnNode(method.desc));
                        list.add(BasicTransformer.arrayOfParams(stat, method.desc));
                        method.instructions.insertBefore(ins, list);

                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = javaName;
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });
        }

        if(wrappers.isEmpty())
            return bytes;

        wrappers.values().forEach(node.methods::add);
        System.out.println("| - "+srg+" had calls to setFire() wrapped!");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
