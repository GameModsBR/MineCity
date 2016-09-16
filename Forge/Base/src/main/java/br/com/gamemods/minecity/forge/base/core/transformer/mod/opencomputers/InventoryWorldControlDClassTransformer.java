package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class InventoryWorldControlDClassTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.server.component.traits.InventoryWorldControl$class"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$decrStackSize",
                ("(Lnet.minecraft.inventory.IInventory;IILli/cil/oc/server/component/traits/InventoryWorldControl;I)Ljava.lang.Object;")
                    .replace('.','/')
                ,
                null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ILOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitTypeInsn(CHECKCAST, "br.com.gamemods.minecity.forge.base.protection.opencomputers.IAgentComponent".replace('.','/'));
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onRobotDropItem",
                "(Lnet.minecraft.inventory.IInventory;IILbr.com.gamemods.minecity.forge.base.protection.opencomputers.IAgentComponent;I)Z"
                    .replace('.','/'),
                false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitFieldInsn(GETSTATIC, "li/cil/oc/util/ResultWrapper$", "MODULE$", "Lli/cil/oc/util/ResultWrapper$;");
        wrapper.visitFieldInsn(GETSTATIC, "scala/Predef$", "MODULE$", "Lscala/Predef$;");
        wrapper.visitInsn(ICONST_1);
        wrapper.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        wrapper.visitInsn(DUP);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitMethodInsn(INVOKESTATIC, "scala/runtime/BoxesRunTime", "boxToBoolean", "(Z)Ljava/lang/Boolean;", false);
        wrapper.visitInsn(AASTORE);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "scala/Predef$", "genericWrapArray", "(Ljava/lang/Object;)Lscala/collection/mutable/WrappedArray;", false);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "li/cil/oc/util/ResultWrapper$", "result", "(Lscala/collection/Seq;)[Ljava/lang/Object;", false);
        wrapper.visitInsn(ARETURN);
        wrapper.visitInsn(ACONST_NULL);
        wrapper.visitInsn(ARETURN);
        wrapper.visitLabel(label);
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ILOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);

        AtomicReference<String> methodName = new AtomicReference<>();

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("drop"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/inventory/IInventory"))
                        .filter(ins-> ins.desc.equals("(II)Lnet/minecraft/item/ItemStack;"))
                        .anyMatch(ins-> {
                            methodName.set(ins.name);

                            InsnList list = new InsnList();
                            list.add(new InsnNode(DUP));
                            list.add(new TypeInsnNode(INSTANCEOF, "[Ljava/lang/Object;"));
                            LabelNode labelNode = new LabelNode();
                            list.add(new JumpInsnNode(IFEQ, labelNode));
                            list.add(new TypeInsnNode(CHECKCAST, "[Ljava/lang/Object;"));
                            list.add(new InsnNode(ARETURN));
                            list.add(labelNode);
                            list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/item/ItemStack"));
                            method.instructions.insert(ins, list);

                            list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 3));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false));
                            method.instructions.insertBefore(ins, list);

                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = transformedName.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        });
                break;
            }
        }

        wrapper.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/inventory/IInventory", methodName.get(), "(II)Lnet/minecraft/item/ItemStack;", true);
        wrapper.visitInsn(ARETURN);
        wrapper.visitEnd();
        node.methods.add(wrapper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
