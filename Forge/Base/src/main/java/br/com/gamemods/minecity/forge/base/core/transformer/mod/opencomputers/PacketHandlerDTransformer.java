package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static jdk.internal.org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class PacketHandlerDTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!"li.cil.oc.server.PacketHandler$".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode keyDown = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$keyDown",
                "(Lli/cil/oc/api/internal/TextBuffer;CILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        keyDown.visitCode();
        keyDown.visitVarInsn(ALOAD, 0);
        keyDown.visitVarInsn(ALOAD, 3);
        keyDown.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        Label label = new Label();
        keyDown.visitJumpInsn(IFEQ, label);
        keyDown.visitInsn(RETURN);
        keyDown.visitLabel(label);
        keyDown.visitVarInsn(ALOAD, 0);
        keyDown.visitVarInsn(ILOAD, 1);
        keyDown.visitVarInsn(ILOAD, 2);
        keyDown.visitVarInsn(ALOAD, 3);
        keyDown.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "keyDown", "(CILnet/minecraft/entity/player/EntityPlayer;)V", true);
        keyDown.visitInsn(RETURN);
        keyDown.visitEnd();

        MethodNode keyUp = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$keyUp",
                "(Lli/cil/oc/api/internal/TextBuffer;CILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        keyUp.visitCode();
        keyUp.visitVarInsn(ALOAD, 0);
        keyUp.visitVarInsn(ALOAD, 3);
        keyUp.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        keyUp.visitJumpInsn(IFEQ, label);
        keyUp.visitInsn(RETURN);
        keyUp.visitLabel(label);
        keyUp.visitVarInsn(ALOAD, 0);
        keyUp.visitVarInsn(ILOAD, 1);
        keyUp.visitVarInsn(ILOAD, 2);
        keyUp.visitVarInsn(ALOAD, 3);
        keyUp.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "keyUp", "(CILnet/minecraft/entity/player/EntityPlayer;)V", true);
        keyUp.visitInsn(RETURN);
        keyUp.visitEnd();

        MethodNode clipboard = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$clipboard",
                "(Lli/cil/oc/api/internal/TextBuffer;Ljava/lang/String;Lnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        clipboard.visitCode();
        clipboard.visitVarInsn(ALOAD, 0);
        clipboard.visitVarInsn(ALOAD, 2);
        clipboard.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        clipboard.visitJumpInsn(IFEQ, label);
        clipboard.visitInsn(RETURN);
        clipboard.visitLabel(label);
        clipboard.visitVarInsn(ALOAD, 0);
        clipboard.visitVarInsn(ALOAD, 1);
        clipboard.visitVarInsn(ALOAD, 2);
        clipboard.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "clipboard", "(Ljava/lang/String;Lnet/minecraft/entity/player/EntityPlayer;)V", true);
        clipboard.visitInsn(RETURN);
        clipboard.visitEnd();

        MethodNode mouseDrag = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$mouseDrag",
                "(Lli/cil/oc/api/internal/TextBuffer;DDILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        mouseDrag.visitCode();
        mouseDrag.visitVarInsn(ALOAD, 0);
        mouseDrag.visitVarInsn(ALOAD, 6);
        mouseDrag.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        mouseDrag.visitJumpInsn(IFEQ, label);
        mouseDrag.visitInsn(RETURN);
        mouseDrag.visitLabel(label);
        mouseDrag.visitVarInsn(ALOAD, 0);
        mouseDrag.visitVarInsn(DLOAD, 1);
        mouseDrag.visitVarInsn(DLOAD, 3);
        mouseDrag.visitVarInsn(ILOAD, 5);
        mouseDrag.visitVarInsn(ALOAD, 6);
        mouseDrag.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "mouseDrag", "(DDILnet/minecraft/entity/player/EntityPlayer;)V", true);
        mouseDrag.visitInsn(RETURN);
        mouseDrag.visitEnd();

        MethodNode mouseDown = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$mouseDown",
                "(Lli/cil/oc/api/internal/TextBuffer;DDILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        mouseDown.visitCode();
        mouseDown.visitVarInsn(ALOAD, 0);
        mouseDown.visitVarInsn(ALOAD, 6);
        mouseDown.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        mouseDown.visitJumpInsn(IFEQ, label);
        mouseDown.visitInsn(RETURN);
        mouseDown.visitLabel(label);
        mouseDown.visitVarInsn(ALOAD, 0);
        mouseDown.visitVarInsn(DLOAD, 1);
        mouseDown.visitVarInsn(DLOAD, 3);
        mouseDown.visitVarInsn(ILOAD, 5);
        mouseDown.visitVarInsn(ALOAD, 6);
        mouseDown.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "mouseDown", "(DDILnet/minecraft/entity/player/EntityPlayer;)V", true);
        mouseDown.visitInsn(RETURN);
        mouseDown.visitEnd();

        MethodNode mouseUp = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$mouseUp",
                "(Lli/cil/oc/api/internal/TextBuffer;DDILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        mouseUp.visitCode();
        mouseUp.visitVarInsn(ALOAD, 0);
        mouseUp.visitVarInsn(ALOAD, 6);
        mouseUp.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        mouseUp.visitJumpInsn(IFEQ, label);
        mouseUp.visitInsn(RETURN);
        mouseUp.visitLabel(label);
        mouseUp.visitVarInsn(ALOAD, 0);
        mouseUp.visitVarInsn(DLOAD, 1);
        mouseUp.visitVarInsn(DLOAD, 3);
        mouseUp.visitVarInsn(ILOAD, 5);
        mouseUp.visitVarInsn(ALOAD, 6);
        mouseUp.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "mouseUp", "(DDILnet/minecraft/entity/player/EntityPlayer;)V", true);
        mouseUp.visitInsn(RETURN);
        mouseUp.visitEnd();

        MethodNode mouseScroll = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$mouseScroll",
                "(Lli/cil/oc/api/internal/TextBuffer;DDILnet/minecraft/entity/player/EntityPlayer;)V",
                null, null
        );
        mouseScroll.visitCode();
        mouseScroll.visitVarInsn(ALOAD, 0);
        mouseScroll.visitVarInsn(ALOAD, 6);
        mouseScroll.visitMethodInsn(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.opencomputers.OCHooks".replace('.','/'),
                "onPlayerInteract",
                "(Lbr.com.gamemods.minecity.forge.base.protection.opencomputers.ITextBuffer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;)Z".replace('.','/'),
                false
        );
        label = new Label();
        mouseScroll.visitJumpInsn(IFEQ, label);
        mouseScroll.visitInsn(RETURN);
        mouseScroll.visitLabel(label);
        mouseScroll.visitVarInsn(ALOAD, 0);
        mouseScroll.visitVarInsn(DLOAD, 1);
        mouseScroll.visitVarInsn(DLOAD, 3);
        mouseScroll.visitVarInsn(ILOAD, 5);
        mouseScroll.visitVarInsn(ALOAD, 6);
        mouseScroll.visitMethodInsn(INVOKEINTERFACE, "li/cil/oc/api/internal/TextBuffer", "mouseScroll", "(DDILnet/minecraft/entity/player/EntityPlayer;)V", true);
        mouseScroll.visitInsn(RETURN);
        mouseScroll.visitEnd();

        for(MethodNode method : node.methods)
        {
            switch(method.name)
            {
                case "onKeyDown":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("keyDown"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = keyDown.name;
                                ins.desc = keyDown.desc;
                                return true;
                            });
                    break;
                case "onKeyUp":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("keyUp"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = keyUp.name;
                                ins.desc = keyUp.desc;
                                return true;
                            });
                    break;
                case "onClipboard":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("clipboard"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = clipboard.name;
                                ins.desc = clipboard.desc;
                                return true;
                            });
                    break;
                case "onMouseClick":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("mouseDrag"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = mouseDrag.name;
                                ins.desc = mouseDrag.desc;
                                return true;
                            });

                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("mouseDown"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = mouseUp.name;
                                ins.desc = mouseUp.desc;
                                return true;
                            });
                    break;
                case "onMouseUp":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("mouseUp"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = mouseUp.name;
                                ins.desc = mouseUp.desc;
                                return true;
                            });
                    break;
                case "onMouseScroll":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("mouseScroll"))
                            .filter(ins-> ins.owner.equals("li/cil/oc/api/internal/TextBuffer"))
                            .anyMatch(ins-> {
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = transformedName.replace('.','/');
                                ins.name = mouseScroll.name;
                                ins.desc = mouseScroll.desc;
                                return true;
                            });
                    break;
            }
        }

        node.methods.add(keyUp);
        node.methods.add(keyDown);
        node.methods.add(clipboard);
        node.methods.add(mouseDrag);
        node.methods.add(mouseUp);
        node.methods.add(mouseDown);
        node.methods.add(mouseScroll);


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
