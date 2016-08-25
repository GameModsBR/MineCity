package br.com.gamemods.minecity.forge.base.core.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

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
     * The SRG name of the class that will be transformed
     */
    private String className;

    /**
     * The class name of the interface that will be injected to the class
     */
    private String interfaceClass;

    public InsertInterfaceTransformer(String className, String interfaceClass)
    {
        this.className = className;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public byte[] transform(String s, String srgName, byte[] bytes)
    {
        if(!srgName.equals(className))
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
