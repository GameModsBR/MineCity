package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

public class EntityArrowTransformer implements IClassTransformer
{
    private String hookClass;

    public EntityArrowTransformer(String hookClass)
    {
        this.hookClass = hookClass.replace('.','/');
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.projectile.EntityArrow"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        MethodNode getArrowStack = null;

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("getArrowStack") || method.name.equals("func_184550_j"))
                getArrowStack = method;
            else if(method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)V"))
            {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while(iter.hasNext())
                {
                    AbstractInsnNode ins = iter.next();
                    int opcode = ins.getOpcode();
                    if(opcode == GETSTATIC)
                    {
                        FieldInsnNode fieldNode = (FieldInsnNode) ins;
                        if(!fieldNode.owner.equals("net/minecraft/init/SoundEvents"))
                            continue;
                    }
                    else if(opcode == LDC)
                    {
                        LdcInsnNode ldcNode = (LdcInsnNode) ins;
                        if(!"random.pop".equals(ldcNode.cst))
                            continue;
                    }
                    else
                        continue;

                    InsnList add = new InsnList();
                    add.add(new VarInsnNode(ALOAD, 1));
                    add.add(new MethodInsnNode(INVOKESTATIC,
                            hookClass, "onPlayerPickupArrow", "(Lnet/minecraft/entity/projectile/EntityArrow;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                            false
                    ));
                    LabelNode elseLabel = new LabelNode(new Label());
                    add.add(new JumpInsnNode(IFEQ, elseLabel));
                    add.add(new InsnNode(RETURN));
                    add.add(elseLabel);
                    add.add(new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(ins, add);
                }
            }
        }

        if(getArrowStack != null)
        {
            MethodNode created = new MethodNode(Opcodes.ACC_PUBLIC, "getIArrowStack",
                    "()Lbr/com/gamemods/minecity/forge/base/accessors/item/IItemStack;",
                    null, null
            );
            created.visitCode();
            created.visitVarInsn(ALOAD, 0);
            created.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/entity/projectile/EntityArrow", getArrowStack.name, getArrowStack.desc, false);
            created.visitTypeInsn(CHECKCAST, "java/lang/Object");
            created.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/accessors/item/IItemStack");
            created.visitInsn(ARETURN);
            created.visitEnd();
            node.methods.add(created);
        }


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
