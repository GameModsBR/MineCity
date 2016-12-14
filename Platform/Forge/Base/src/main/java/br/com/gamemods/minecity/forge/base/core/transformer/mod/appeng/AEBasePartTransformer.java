package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

public class AEBasePartTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public AEBasePartTransformer()
    {
        super("appeng.parts.AEBasePart");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.appeng.IAEBasePart".replace('.','/'));
        String host = "br.com.gamemods.minecity.forge.base.protection.appeng.PartHost".replace('.','/');

        MethodNode method = new MethodNode(ACC_PUBLIC|ACC_BRIDGE, "getHost", "()L"+host+";", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), "getHost", "()Lappeng/api/parts/IPartHost;", false);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
