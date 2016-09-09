package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

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
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        if(srg.equals(ModEnv.hookClass))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        boolean modified = false;

        for(MethodNode method : node.methods)
        {
            boolean stat = (method.access & ACC_STATIC) > 0;

            boolean warned = false;
            boolean repeat = true;
            repeat:
            while(repeat)
            {
                repeat = false;
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while(iter.hasNext())
                {
                    AbstractInsnNode ins = iter.next();
                    if(ins.getOpcode() == INVOKEVIRTUAL)
                    {
                        MethodInsnNode methodNode = (MethodInsnNode) ins;
                        if((methodNode.name.equals("func_70015_d") || methodNode.name.equals("setFire"))
                         && methodNode.owner.equals("net/minecraft/entity/Entity") && methodNode.desc.equals("(I)V")
                        )
                        {
                            InsnList list = new InsnList();
                            if(stat)
                                list.add(new InsnNode(ACONST_NULL));
                            else
                                list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new LdcInsnNode(Type.getObjectType(srg.replace('.','/'))));
                            list.add(new LdcInsnNode(method.name));
                            list.add(new LdcInsnNode(method.desc));
                            iter.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onIgnite", "(Lnet/minecraft/entity/Entity;ILjava/lang/Object;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V", false
                            ));
                            method.instructions.insertBefore(ins, list);
                            method.instructions.remove(ins);
                            repeat = true;
                            modified = true;
                            if(!warned)
                            {
                                System.out.println("\n | - "+srg +
                                        " had calls to entity.setFire(int) wrapped " +
                                        "to MineCityHook.onIgnite(entity, int, "+(stat?"null":"this")+", " +
                                        srg + ".class, \""+method.name+"\", \""+method.desc+"\")");
                                warned = true;
                            }
                            continue repeat;
                        }
                    }
                }
            }
        }

        if(!modified)
            return bytes;


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
