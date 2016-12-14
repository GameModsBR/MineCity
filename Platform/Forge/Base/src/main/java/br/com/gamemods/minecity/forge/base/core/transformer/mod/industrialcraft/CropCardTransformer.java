package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class CropCardTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("ic2.api.crops.CropCard"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropCard".replace('.','/'));

        MethodNode method = new MethodNode(ACC_PUBLIC, "canBeHarvested", "(Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.CropTile;)Z".replace('.','/'),null,null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitVarInsn(ALOAD, 1);
        method.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "canBeHarvested", "(Lic2/api/crops/ICropTile;)Z", false);
        method.visitInsn(IRETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getGain", "(Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.CropTile;)Lbr.com.gamemods.minecity.forge.base.accessors.item.IItemStack;".replace('.','/'),null,null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitVarInsn(ALOAD, 1);
        method.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "getGain", "(Lic2/api/crops/ICropTile;)Lnet/minecraft/item/ItemStack;", false);
        method.visitTypeInsn(CHECKCAST, "java/lang/Object");
        method.visitTypeInsn(CHECKCAST, "br.com.gamemods.minecity.forge.base.accessors.item.IItemStack".replace('.','/'));
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getSeeds", "(Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.CropTile;)Lbr.com.gamemods.minecity.forge.base.accessors.item.IItemStack;".replace('.','/'),null,null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitVarInsn(ALOAD, 1);
        method.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "getSeeds", "(Lic2/api/crops/ICropTile;)Lnet/minecraft/item/ItemStack;", false);
        method.visitTypeInsn(CHECKCAST, "java/lang/Object");
        method.visitTypeInsn(CHECKCAST, "br.com.gamemods.minecity.forge.base.accessors.item.IItemStack".replace('.','/'));
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
