package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class IPartHostTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public IPartHostTransformer()
    {
        super("appeng.api.parts.IPartHost");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        node.version = JAVA8;
        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.appeng.PartHost".replace('.','/'));

        String mod = "br.com.gamemods.minecity.forge.base.MineCityForge".replace('.','/');
        String pos = "br.com.gamemods.minecity.api.world.BlockPos".replace('.','/');
        String hook = ModEnv.hookClass.replace('.','/');
        String dim = "appeng.api.util.DimensionalCoord".replace('.','/');
        String coord = "appeng.api.util.WorldCoord".replace('.','/');

        MethodNode method = new MethodNode(ACC_PUBLIC,
                "getPos", "(L"+mod+";)L"+pos+";", null, null
        );
        Label start = new Label();
        Label end = new Label();
        method.visitLabel(start);
        method.visitLocalVariable("coord", "L"+dim+";", null, start, end, 2);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEINTERFACE, name.replace('.','/'), "getLocation", "()L"+dim+";", true);
        method.visitVarInsn(ASTORE, 2);
        method.visitVarInsn(ALOAD, 2);
        method.visitMethodInsn(INVOKEVIRTUAL, dim, "getWorld", "()Lnet/minecraft/world/World;", false);
        method.visitVarInsn(ALOAD, 2);
        method.visitFieldInsn(GETFIELD, coord, "x", "I");
        method.visitVarInsn(ALOAD, 2);
        method.visitFieldInsn(GETFIELD, coord, "y", "I");
        method.visitVarInsn(ALOAD, 2);
        method.visitFieldInsn(GETFIELD, coord, "z", "I");
        method.visitMethodInsn(INVOKESTATIC, hook, "toPos", "(Ljava/lang/Object;III)L"+pos+";", false);
        method.visitInsn(ARETURN);
        method.visitLabel(end);
        method.visitEnd();
        node.methods.add(method);
    }
}
