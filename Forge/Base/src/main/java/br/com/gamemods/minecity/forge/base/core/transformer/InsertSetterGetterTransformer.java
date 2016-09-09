package br.com.gamemods.minecity.forge.base.core.transformer;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Makes a class implements an interface with a setter and getter.
 * <p>Example:</p>
 * <pre><code>
 *     public class WorldServer extends World
 *         implements IWorldServer // <- Added
 *     {
 *         // ... original fields and methods
 *         public WorldDim mineCity;
 *         public WorldDim getMineCityWorld(){ return this.mineCity; }
 *         public void setMineCityWorld(WorldDim world){ this.mineCity = world; }
 *     }
 * </code></pre>
 */
public class InsertSetterGetterTransformer implements IClassTransformer
{
    /**
     * The SRG name of the class that will be transformed
     */
    private Collection<String> classNames;

    /**
     * The type of the field that will be added to the class
     */
    private String fieldClass;

    /**
     * The name of the field that will be added to the class
     */
    private String fieldName;

    /**
     * The class name of the interface that will be injected to the class
     */
    private String interfaceClass;

    /**
     * The setter method name, must return void and accept the same class as {@link #fieldClass}
     */
    private String setterMethodName;

    /**
     * The getter method name, must return the same class as {@link #fieldClass} and receive no argument
     */
    private String getterMethodName;

    public InsertSetterGetterTransformer(String className, String fieldClass, String fieldName,
                                         String interfaceClass, String setterMethodName, String getterMethodName)
    {
        this.classNames = Collections.singleton(className);
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
        this.interfaceClass = interfaceClass;
        this.setterMethodName = setterMethodName;
        this.getterMethodName = getterMethodName;
    }

    public InsertSetterGetterTransformer(String fieldClass, String fieldName, String interfaceClass,
                                         String setterMethodName, String getterMethodName,
                                         Collection<String> classNames)
    {
        this.classNames = new HashSet<>(classNames);
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
        this.interfaceClass = interfaceClass;
        this.setterMethodName = setterMethodName;
        this.getterMethodName = getterMethodName;
    }

    @Override
    public byte[] transform(String name, String srgName, byte[] bytes)
    {
        if(classNames.contains(srgName))
        {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);
            String fieldClassName = fieldClass.replace('.','/');
            String interfaceClassName = interfaceClass.replace('.','/');
            String thisClassName = srgName.replace('.','/');

            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer)
            {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
                {
                    interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                    interfaces[interfaces.length-1] = interfaceClassName;
                    super.visit(version, access, name, signature, superName, interfaces);
                }
            };

            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            writer.visitField(Opcodes.ACC_PUBLIC, fieldName, "L"+fieldClassName+";", null, null).visitEnd();

            MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, getterMethodName, "()L"+fieldClassName+";", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, thisClassName, fieldName, "L"+fieldClassName+";");
            methodVisitor.visitInsn(Opcodes.ARETURN);
            methodVisitor.visitMaxs(1, 2);
            methodVisitor.visitEnd();

            methodVisitor = visitor.visitMethod(Opcodes.ACC_PUBLIC, setterMethodName, "(L"+fieldClassName+";)V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, thisClassName, fieldName, "L"+fieldClassName+";");
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();

            bytes = writer.toByteArray();
            ModEnv.saveClass(srgName, bytes);
            return bytes;
        }

        return bytes;
    }
}
