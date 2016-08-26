package br.com.gamemods.minecity.forge.base.core.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Collections;

/**
 * Makes a class implements an interface.
 * <p>Example:</p>
 * <pre><code>
 *     public class WorldServer extends World
 *         implements IWorldServer // <- Added
 *     {
 *         // ... original fields and methods
 *     }
 * </code></pre>
 */
public class InsertInterfaceTransformer implements IClassTransformer
{
    /**
     * The SRG name of the classes that will be transformed
     */
    private Collection<String> classNames;

    /**
     * The class name of the interface that will be injected to the class
     */
    private String interfaceClass;

    public InsertInterfaceTransformer(String className, String interfaceClass)
    {
        this.classNames = Collections.singleton(className);
        this.interfaceClass = interfaceClass;
    }

    public InsertInterfaceTransformer(String interfaceClass, Collection<String> classNames)
    {
        this.classNames = classNames;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public byte[] transform(String s, String srgName, byte[] bytes)
    {
        if(!classNames.contains(srgName))
            return bytes;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        classNode.interfaces.add(interfaceClass.replace('.','/'));

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
