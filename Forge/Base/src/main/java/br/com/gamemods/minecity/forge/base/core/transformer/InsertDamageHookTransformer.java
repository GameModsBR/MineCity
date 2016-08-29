package br.com.gamemods.minecity.forge.base.core.transformer;

import br.com.gamemods.minecity.forge.base.MethodPatcher;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.DamageSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
public class InsertDamageHookTransformer implements IClassTransformer
{
    private String className;
    private String hookClass;
    private String hookName;
    private String entityClass;
    private String interfaceName;

    public InsertDamageHookTransformer(String className, Class hookClass, String hookName, Class itf, String entityClass)
    {
        this.className = className;
        this.hookClass = hookClass.getName().replace('.','/');
        this.hookName = hookName;
        this.entityClass = entityClass;
        this.interfaceName = itf == null? null : itf.getName().replace('.','/');
    }

    public InsertDamageHookTransformer(String className, Class hookClass, String hookName, Class itf)
    {
        this(className, hookClass, hookName, itf, "net.minecraft.entity.Entity");
    }

    public InsertDamageHookTransformer(String className, Class hookClass, String hookName)
    {
        this(className, hookClass, hookName, null, "net.minecraft.entity.Entity");
    }

    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals(className))
            return bytes;

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String sourceClass = DamageSource.class.getName().replace('.', '/');

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
        return writer.toByteArray();
    }
}
