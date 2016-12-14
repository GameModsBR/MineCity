package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class BlockCoordinatesTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockCoordinatesTransformer()
    {
        super("thaumcraft.api.BlockCoordinates");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        name = name.replace('.','/');
        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.thaumcraft.IBlockCoordinates".replace('.','/'));
        node.methods.add(getter(name, "getX", "x"));
        node.methods.add(getter(name, "getY", "y"));
        node.methods.add(getter(name, "getZ", "z"));
    }

    private MethodNode getter(String name, String md, String fd)
    {
        MethodNode method = new MethodNode(ACC_PUBLIC, md, "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name, fd, "I");
        method.visitInsn(IRETURN);
        method.visitEnd();
        return method;
    }
}
