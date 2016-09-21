package br.com.gamemods.minecity.forge.base.core.transformer.forge.world;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced
public class ExplosionTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public ExplosionTransformer()
    {
        super("net.minecraft.world.Explosion");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        FieldNode world = node.fields.stream().filter(f -> f.desc.equals("Lnet/minecraft/world/World;"))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find the world field"));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getWorld", "()Lbr.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;".replace('.','/'), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), world.name, world.desc);
        method.visitTypeInsn(CHECKCAST, "br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer".replace('.','/'));
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
