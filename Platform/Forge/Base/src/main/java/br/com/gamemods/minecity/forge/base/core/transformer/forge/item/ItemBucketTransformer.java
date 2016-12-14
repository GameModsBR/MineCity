package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class ItemBucketTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("net.minecraft.item.ItemBucket"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String itf = "br.com.gamemods.minecity.forge.base.accessors.item.IItemBucket".replace('.','/');
        String block = "br.com.gamemods.minecity.forge.base.accessors.block.IBlock".replace('.','/');
        node.interfaces.add(itf);

        FieldNode liquid = node.fields.stream()
                .filter(field -> field.desc.equals("Lnet/minecraft/block/Block;"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the isFull field"));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getLiquidBlock", "()L"+block+";", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, transformedName.replace('.','/'), liquid.name, liquid.desc);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
