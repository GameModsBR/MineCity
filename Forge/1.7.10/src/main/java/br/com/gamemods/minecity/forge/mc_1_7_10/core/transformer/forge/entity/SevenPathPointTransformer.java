package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class SevenPathPointTransformer extends BasicTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenPathPointTransformer()
    {
        super("net.minecraft.pathfinding.PathPoint");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        FieldNode distance = node.fields.stream()
                .filter(fd -> fd.name.equals("field_75834_g") || fd.name.equals("distanceToTarget"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the distanceToTarget field"));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getDistanceToTarget", "()F", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), distance.name, distance.desc);
        method.visitInsn(FRETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
