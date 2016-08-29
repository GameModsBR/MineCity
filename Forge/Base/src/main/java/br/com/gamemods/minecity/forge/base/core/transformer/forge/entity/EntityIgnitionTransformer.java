package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.MethodPatcher;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * Wraps all calls to entity.setFire(int) in methods that are not static to MineCityHook.onIgnite(entity, int, this)<br/>
 * Example:
 * <code><pre>
 *     public class Anthing {
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
 *     public class Anthing {
 *         public Object something;
 *
 *         public void anyNonStaticMethod(Entity.. anySignature) throws Exception {
 *             Entity anyEntityVariable = anySignature[0];
 *             // Any logic before
 *             MineCityHook.onIgnite(anyEntityVariable, 5, this);
 *             // Any logic after
 *             int anyWay = 5;
 *             MineCityHook.onIgnite(anySignature[3], (int)Math.sqrt((anyWay * 5)/2), this);
 *             // Including casts
 *             MineCityHook.onIgnite((Entity) something, 3, this);
 *             // Will not work with reflections
 *             Entity.class.getDeclaredMethod("setFire", Integer.TYPE).invoke(something, 5);
 *         }
 *     }
 * </code></pre>
 * <p>On Vanilla Minecraft + Forge only EntityMob, EntityZombie, EntityPlayer, EntityArrow and EntitySmallFireball are patched.</p>
 */
@MethodPatcher
public class EntityIgnitionTransformer implements IClassTransformer
{
    private String hookClass;

    public EntityIgnitionTransformer(String hookClass)
    {
        this.hookClass = hookClass.replace('.','/');
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        if(node.superName == null && !srg.equals("net.minecraft.entity.Entity"))
            return bytes;

        boolean modified = false;

        for(MethodNode method : node.methods)
        {
            if((method.access & ACC_STATIC) > 0)
                continue;

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
                            list.add(new VarInsnNode(ALOAD, 0));
                            iter.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onIgnite", "(Lnet/minecraft/entity/Entity;ILjava/lang/Object;)V", false
                            ));
                            method.instructions.insertBefore(ins, list);
                            method.instructions.remove(ins);
                            repeat = true;
                            modified = true;
                            continue repeat;
                        }
                    }
                }
            }
        }

        if(!modified)
            return bytes;

        System.out.println(srg+" had all calls to entity.setFire(int) wrapped to MineCityHook.onIgnite(entity, int, this)");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
