package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import net.minecraft.entity.ai.EntityAIBase;

public interface IEntityAIBase
{
    default EntityAIBase getForgeAI()
    {
        return (EntityAIBase) this;
    }


    default boolean shouldExecute()
    {
        return getForgeAI().shouldExecute();
    }

    default boolean continueExecuting()
    {
        return getForgeAI().continueExecuting();
    }

    default boolean isInterruptible()
    {
        return getForgeAI().isInterruptible();
    }

    default void startExecuting()
    {
        getForgeAI().startExecuting();
    }

    default void resetTask()
    {
        getForgeAI().resetTask();
    }

    default void updateTask()
    {
        getForgeAI().updateTask();
    }

    default void setMutexBits(int bits)
    {
        getForgeAI().setMutexBits(bits);
    }

    default int getMutexBits()
    {
        return getForgeAI().getMutexBits();
    }
}
