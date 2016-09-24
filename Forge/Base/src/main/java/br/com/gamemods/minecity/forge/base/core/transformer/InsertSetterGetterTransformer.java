package br.com.gamemods.minecity.forge.base.core.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

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
public class InsertSetterGetterTransformer extends BasicTransformer
{
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
        super(className);
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
        this.interfaceClass = interfaceClass == null? "" : interfaceClass;
        this.setterMethodName = setterMethodName;
        this.getterMethodName = getterMethodName;
    }

    public InsertSetterGetterTransformer(String fieldClass, String fieldName, String interfaceClass,
                                         String setterMethodName, String getterMethodName,
                                         Collection<String> classNames)
    {
        super(classNames);
        this.fieldClass = fieldClass;
        this.fieldName = fieldName;
        this.interfaceClass = interfaceClass == null? "" : interfaceClass;
        this.setterMethodName = setterMethodName;
        this.getterMethodName = getterMethodName;
    }

    @Override
    protected void patch(String srgName, ClassNode node, ClassReader reader)
    {
        String fieldClassName = fieldClass.replace('.','/');
        String interfaceClassName = interfaceClass.replace('.','/');
        String thisClassName = srgName.replace('.','/');

        if(!interfaceClassName.isEmpty())
            node.interfaces.add(interfaceClassName);

        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, fieldName, "L"+fieldClassName+";", null, null));

        MethodNode methodVisitor = new MethodNode(Opcodes.ACC_PUBLIC, getterMethodName, "()L"+fieldClassName+";", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, thisClassName, fieldName, "L"+fieldClassName+";");
        methodVisitor.visitInsn(Opcodes.ARETURN);
        methodVisitor.visitMaxs(1, 2);
        methodVisitor.visitEnd();
        node.methods.add(methodVisitor);

        methodVisitor = new MethodNode(Opcodes.ACC_PUBLIC, setterMethodName, "(L"+fieldClassName+";)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, thisClassName, fieldName, "L"+fieldClassName+";");
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
        node.methods.add(methodVisitor);
    }
}
