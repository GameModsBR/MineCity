package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class SevenEntityPlayerTransformer extends BasicTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityPlayerTransformer()
    {
        super("net.minecraft.entity.player.EntityPlayer");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        name = name.replace('.','/');

        FieldNode itemInUse = node.fields.stream().filter(fd -> fd.desc.equals("Lnet/minecraft/item/ItemStack;"))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find the itemInUse field"));
        FieldNode itemInUseCount = Objects.requireNonNull(node.fields.get(node.fields.indexOf(itemInUse)+1), "itemInUseCount not found");
        if(!itemInUseCount.desc.equals("I"))
            throw new UnsupportedOperationException("Found "+itemInUseCount+" instead of itemInUseCount");

        String stack = "br.com.gamemods.minecity.forge.base.accessors.item.IItemStack".replace('.','/');
        MethodNode method = new MethodNode(ACC_PUBLIC, "getActiveItemStack", "()L"+stack+";", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name, itemInUse.name, itemInUse.desc);
        method.visitTypeInsn(CHECKCAST, "java/lang/Object");
        method.visitTypeInsn(CHECKCAST, stack);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getActiveItemUseCount", "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name, itemInUseCount.name, "I");
        method.visitInsn(IRETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
