package br.com.gamemods.minecity.forge.base.core.transformer;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
public class InsertDamageHookTransformer implements IClassTransformer
{
    private String className;
    private String hookName;
    private String entityClass;
    private String interfaceName;

    public InsertDamageHookTransformer(String className, String hookName, String itf, String entityClass)
    {
        this.className = className;
        this.hookName = hookName;
        this.entityClass = entityClass;
        this.interfaceName = itf == null? null : itf.replace('.','/');
    }

    public InsertDamageHookTransformer(String className, String hookName, String itf)
    {
        this(className, hookName, itf, "net.minecraft.entity.Entity");
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals(className))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String sourceClass = "net/minecraft/util/DamageSource";

        node.methods.stream().filter(method -> method.desc.equals("(L"+sourceClass+";F)Z")).forEach(method ->
        {
            InsnList instructions = new InsnList();
            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new VarInsnNode(ALOAD, 1));
            instructions.add(new VarInsnNode(FLOAD, 2));
            instructions.add(new MethodInsnNode(INVOKESTATIC,
                    hookClass, hookName, "(L"+entityClass.replace('.', '/')+";L"+sourceClass+";F)Z", false
            ));
            Label elseLabel = new Label();
            LabelNode elseNode = new LabelNode(elseLabel);
            instructions.add(new JumpInsnNode(IFEQ, elseNode));
            instructions.add(new InsnNode(ICONST_0));
            instructions.add(new InsnNode(IRETURN));
            instructions.add(elseNode);

            method.instructions.insertBefore(method.instructions.get(1), instructions);
        });

        if(interfaceName != null)
            node.interfaces.add(interfaceName);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
