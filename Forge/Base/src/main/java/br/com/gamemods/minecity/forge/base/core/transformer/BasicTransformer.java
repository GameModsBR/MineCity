package br.com.gamemods.minecity.forge.base.core.transformer;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BasicTransformer implements IClassTransformer
{
    public static final int JAVA8 = 52;

    @MagicConstant(flagsFromClass = ClassWriter.class)
    protected int writerFlags = ClassWriter.COMPUTE_MAXS;
    private final Set<String> accept;

    public BasicTransformer(Collection<String> transformedNames)
    {
        Set<String> names = transformedNames.stream().filter(str -> str != null && !str.isEmpty()).collect(Collectors.toSet());
        int size = names.size();
        if(size == 0)
            throw new IllegalArgumentException();

        if(size == 1)
            accept = Collections.singleton(names.iterator().next());
        else
            accept = names;
    }

    public BasicTransformer(String accept)
    {
        if(Objects.requireNonNull(accept).isEmpty())
            throw new IllegalArgumentException();

        this.accept = Collections.singleton(accept);
    }

    public boolean accept(String name)
    {
        return this.accept.contains(name);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!accept(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        System.out.println("Patching "+transformedName);
        patch(transformedName, node, reader);

        ClassWriter writer = new ClassWriter(writerFlags);
        node.accept(writer);
        return finalize(ModEnv.saveClass(transformedName, writer.toByteArray()));
    }

    protected abstract void patch(String name, ClassNode node, ClassReader reader);

    protected byte[] finalize(byte[] bytes)
    {
        return bytes;
    }
}
