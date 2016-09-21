package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class ICropTileTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("ic2.api.crops.ICropTile"))
            return basicClass;

        ClassNode node = new ClassNode(ASM5);
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);
        node.version = 52;

        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.industrialcraft.CropTile".replace('.','/'));

        MethodNode getCropPlanted = new MethodNode(ASM5, ACC_PUBLIC, "getCropPlanted", "()Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropCard;".replace('.','/'),null,null);
        getCropPlanted.visitCode();
        getCropPlanted.visitVarInsn(ALOAD, 0);
        getCropPlanted.visitMethodInsn(INVOKEINTERFACE, transformedName.replace('.','/'), "getCrop", "()Lic2/api/crops/CropCard;", true);
        getCropPlanted.visitInsn(ARETURN);
        getCropPlanted.visitEnd();
        node.methods.add(getCropPlanted);

        MethodNode setCropPlanted = new MethodNode(ASM5, ACC_PUBLIC, "setCropPlanted", "(Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.ICropCard;)V".replace('.','/'),null,null);
        setCropPlanted.visitCode();
        setCropPlanted.visitVarInsn(ALOAD, 0);
        setCropPlanted.visitVarInsn(ALOAD, 1);
        setCropPlanted.visitTypeInsn(CHECKCAST, "ic2/api/crops/CropCard");
        setCropPlanted.visitMethodInsn(INVOKEINTERFACE, transformedName.replace('.','/'), "setCrop", "(Lic2/api/crops/CropCard;)V", true);
        setCropPlanted.visitInsn(RETURN);
        setCropPlanted.visitEnd();
        node.methods.add(setCropPlanted);

        if(node.methods.stream().noneMatch(m-> m.name.equals("isCrossingBase") && m.desc.equals("()Z")))
        {
            MethodNode isCrossingBase = new MethodNode(ASM5, ACC_PUBLIC, "isCrossingBase", "()Z", null, null);
            isCrossingBase.visitCode();
            isCrossingBase.visitVarInsn(ALOAD, 0);
            isCrossingBase.visitTypeInsn(CHECKCAST, "ic2/core/crop/TileEntityCrop");
            isCrossingBase.visitFieldInsn(GETFIELD, "ic2/core/crop/TileEntityCrop", "upgraded", "Z");
            isCrossingBase.visitInsn(IRETURN);
            isCrossingBase.visitEnd();
            node.methods.add(isCrossingBase);
        }

        MethodNode sizeMethod = node.methods.stream()
                .filter(m-> "getCurrentSize".equals(m.name) && m.desc.equals("()I") || "getSize".equals(m.name) && m.desc.equals("()B"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("Failed to find the getSize() method"));

        MethodNode method = new MethodNode(ACC_PUBLIC, "getCropSize", "()I", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKEINTERFACE, transformedName.replace('.','/'), sizeMethod.name, sizeMethod.desc, true);
        method.visitInsn(IRETURN);
        method.visitEnd();
        node.methods.add(method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
