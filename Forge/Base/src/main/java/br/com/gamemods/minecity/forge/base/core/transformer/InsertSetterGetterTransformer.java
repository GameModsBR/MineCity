package br.com.gamemods.minecity.forge.base.core.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Arrays;

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
    private String className = "net.minecraft.world.WorldServer";

    /**
     * The type of the field that will be added to the class
     */
    private String fieldClass = "br.com.gamemods.minecity.api.world.WorldDim";

    /**
     * The name of the field that will be added to the class
     */
    private String fieldName = "mineCity";

    /**
     * The class name of the interface that will be injected to the class
     */
    private String interfaceClass = "br.com.gamemods.minecity.forge.base.accessors.IWorldServer";

    /**
     * The setter method name, must return void and accept the same class as {@link #fieldClass}
     */
    private String setterMethodName = "setMineCityWorld";

    /**
     * The getter method name, must return the same class as {@link #fieldClass} and receive no argument
     */
    private String getterMethodName = "getMineCityWorld";

    public InsertSetterGetterTransformer(String className, String fieldClass, String fieldName,
                                         String interfaceClass, String setterMethodName, String getterMethodName)
    {
        this.className = className;
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
        this.interfaceClass = interfaceClass;
        this.setterMethodName = setterMethodName;
        this.getterMethodName = getterMethodName;
    }

    @Override
    public byte[] transform(String name, String srgName, byte[] bytes)
    {
        if(srgName.equals(className))
        {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, Opcodes.ASM4);
            String fieldClassName = fieldClass.replace('.','/');
            String interfaceClassName = interfaceClass.replace('.','/');
            String thisClassName = className.replace('.','/');

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
        }

        return bytes;
    }
}
