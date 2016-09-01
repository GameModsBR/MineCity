package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface Projectile
{
    /**
     * @deprecated This method does not persists the data, use {@link #setShooter(ProjectileShooter)}
     */
    @Deprecated
    void setMineCityShooter(@Nullable ProjectileShooter shooter);

    /**
     * @deprecated This method does not load data from the persistence, use {@link #getShooter()}
     */
    @Deprecated
    @Nullable
    ProjectileShooter getMineCityShooter();

    default void detectShooter(MineCityForge mod)
    {
        if(this instanceof IEntity)
        {
            IEntity entity = (IEntity) this;
            setShooter(new ProjectileShooter(entity.getEntityPos(mod)));
        }
    }

    @SuppressWarnings("deprecation")
    default void setShooter(@NotNull ProjectileShooter shooter)
    {
        try
        {
            setMineCityShooter(shooter);
        }
        catch(AbstractMethodError error)
        {
            System.err.println("[MineCity] The class "+this+" does not implements setMineCityShooter(), using NBT Tags without caches!");
        }

        try
        {
            byte[] bytes;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(shooter);
            out.close();
            bytes = stream.toByteArray();
            NBTTagCompound nbt = ((Entity) this).getEntityData();
            nbt.setByteArray("MineCityShooter", bytes);
        }
        catch(Exception e)
        {
            System.err.println("[MineCity] Failed to persist the shooter "+shooter+" in "+this);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    default ProjectileShooter getShooter()
    {
        try
        {
            ProjectileShooter shooter = getMineCityShooter();
            if(shooter != null)
                return shooter;
        }
        catch(AbstractMethodError e)
        {
            System.err.println("[MineCity] The class "+this+" does not implements getMineCityShooter(), using NBT Tags without caches!");
        }

        try
        {
            byte[] bytes = ((Entity) this).getEntityData().getByteArray("MineCityShooter");
            if(bytes.length == 0)
                return null;

            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(stream);
            ProjectileShooter shooter = (ProjectileShooter) in.readObject();
            try
            {
                setMineCityShooter(shooter);
            }
            catch(AbstractMethodError error)
            {
                System.err.println("[MineCity] The class "+this+" does not implements setMineCityShooter(), using NBT Tags without caches!");
            }

            return shooter;
        }
        catch(Exception e)
        {
            System.err.println("[MineCity] Failed to load the shooter from "+this);
            e.printStackTrace();
            return null;
        }
    }
}
