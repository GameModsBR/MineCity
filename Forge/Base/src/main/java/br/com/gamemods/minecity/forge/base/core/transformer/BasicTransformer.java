package br.com.gamemods.minecity.forge.base.core.transformer;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import net.minecraft.launchwrapper.IClassTransformer;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public abstract class BasicTransformer implements IClassTransformer
{
    public static final int JAVA8 = 52;
    private static Pattern allParamsPattern = Pattern.compile("(\\(.*?\\))");
    private static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|(:?L[^;]+;))");

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
    final public byte[] transform(String name, String transformedName, byte[] basicClass)
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

    public static AbstractInsnNode intIns(int i)
    {
        if(i >= 0 && i <= 5)
            return new InsnNode(ICONST_0 + i);
        else if(i == -1)
            return new InsnNode(ICONST_M1);
        else
            return new IntInsnNode(BIPUSH, i);
    }

    public static InsnList arrayOfParams(boolean staticMethod, String desc)
    {
        InsnList list = new InsnList();
        Matcher paramMatcher = allParamsPattern.matcher(desc);
        if(!paramMatcher.find())
        {
            list.add(new InsnNode(ICONST_0));
            list.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
            return list;
        }
        else
        {
            String paramGroup = paramMatcher.group(1);
            Matcher matcher = paramsPattern.matcher(paramGroup);
            InsnList load = new InsnList();

            int count = 0;
            int i = staticMethod? 0 : 1;
            while(matcher.find())
            {
                String array = matcher.group(1);
                String type = matcher.group(2);

                load.add(new InsnNode(DUP));
                load.add(intIns(count++));

                if(!array.isEmpty())
                    load.add(new VarInsnNode(ALOAD, i++));
                else
                    switch(type.charAt(0))
                    {
                        case 'L':
                            load.add(new VarInsnNode(ALOAD, i++));
                            break;

                        case 'I':
                            load.add(new VarInsnNode(ILOAD, i++));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
                            break;

                        case 'Z':
                            load.add(new VarInsnNode(ILOAD, i++));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                            break;

                        case 'F':
                            load.add(new VarInsnNode(FLOAD, i++));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
                            break;

                        case 'D':
                            load.add(new VarInsnNode(DLOAD, i));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false));
                            i += 2;
                            break;

                        case 'S':
                            load.add(new VarInsnNode(ILOAD, i++));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false));
                            break;

                        case 'J':
                            load.add(new VarInsnNode(LLOAD, i));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
                            i += 2;
                            break;

                        case 'C':
                            load.add(new VarInsnNode(ILOAD, i++));
                            load.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false));
                            break;

                        default:
                            throw new UnsupportedOperationException("Unsupported param type: "+type);
                    }

                load.add(new InsnNode(AASTORE));
            }

            list.add(intIns(count));
            list.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
            list.add(load);
            return list;
        }
    }
}
