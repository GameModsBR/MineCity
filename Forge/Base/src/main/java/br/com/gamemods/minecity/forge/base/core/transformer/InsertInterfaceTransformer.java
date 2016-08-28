package br.com.gamemods.minecity.forge.base.core.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * The key is a SRG name of the classes that will be transformed,
     * the value is a class name of the interface that will be injected to the class
     */
    private Map<String, String> replacements;

    public InsertInterfaceTransformer(String className, String interfaceClass)
    {
        replacements = Collections.singletonMap(className, interfaceClass);
    }

    public InsertInterfaceTransformer(String interfaceClass, Collection<String> classNames)
    {
        replacements = new HashMap<>(classNames.size());
        classNames.forEach(key-> replacements.put(key, interfaceClass));
    }

    public InsertInterfaceTransformer()
    {
        replacements = Collections.emptyMap();
    }

    public void setReplacements(Map<String, String> replacements)
    {
        this.replacements = replacements;
    }

    public Map<String, String> getReplacements()
    {
        return replacements;
    }

    public void printReplacements()
    {
        System.out.println("The following classes will have these interfaces inserted");
        replacements.forEach((k, v) -> {
            System.out.println(" - "+k);
            System.out.println(" | -- "+v);
        });
    }

    @Override
    public byte[] transform(String s, String srgName, byte[] bytes)
    {
        String interfaceClass = replacements.get(srgName);
        if(interfaceClass == null)
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
