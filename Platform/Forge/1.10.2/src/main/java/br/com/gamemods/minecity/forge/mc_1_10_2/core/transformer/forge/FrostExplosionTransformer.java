package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ExplosionTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced(at = MineCityFrostCoreMod.class)
public class FrostExplosionTransformer extends ExplosionTransformer
{
    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        super.patch(name, node, reader);

        FieldNode exploder = node.fields.stream().filter(f-> f.desc.equals("Lnet/minecraft/entity/Entity;"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the exploder field"));
        MethodNode method = new MethodNode(ACC_PUBLIC, "getExploder", "()Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;".replace('.','/'), null, null);

        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), exploder.name, exploder.desc);
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
