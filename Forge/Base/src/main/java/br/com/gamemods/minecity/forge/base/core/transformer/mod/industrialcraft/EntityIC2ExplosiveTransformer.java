package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class EntityIC2ExplosiveTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityIC2ExplosiveTransformer()
    {
        super("ic2.core.block.EntityIC2Explosive");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.industrialcraft.IEntityIC2Explosive".replace('.','/'));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getIgniter", "()Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;".replace('.','/'), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), "igniter", "Lnet/minecraft/entity/EntityLivingBase;");
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
