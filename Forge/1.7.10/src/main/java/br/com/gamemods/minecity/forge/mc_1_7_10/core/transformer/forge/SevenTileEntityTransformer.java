package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

@Referenced
@MethodPatcher
public class SevenTileEntityTransformer extends InsertSetterGetterTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenTileEntityTransformer()
    {
        super("net.minecraft.tileentity.TileEntity",
                "net.minecraft.nbt.NBTTagCompound", "mineCityNBT",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenTileEntity",
                "setMineCityCustomData", "getMineCityCustomData"
        );
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        super.patch(name, node, reader);

        String nbtBase = "net.minecraft.nbt.NBTTagCompound".replace('.','/');
        String itf = "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block.SevenTileEntity".replace('.','/');

        boolean read = true;
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(L"+nbtBase+";)V"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                if(read)
                {
                    read = false;
                    list.add(new MethodInsnNode(INVOKEINTERFACE, itf, "loadMineCityData", "(L"+nbtBase+";)V", true));
                    method.instructions.insert(list);
                }
                else
                {
                    list.add(new MethodInsnNode(INVOKEINTERFACE, itf, "saveMineCityData", "(L"+nbtBase+";)V", true));
                    method.instructions.insert(list);
                    break;
                }
            }
        }
    }
}
