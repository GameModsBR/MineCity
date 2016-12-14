package br.com.gamemods.minecity.forge.base;

public class Task
{
    private final Runnable task;
    private int wait;

    public Task(Runnable task, int wait)
    {
        this.task = task;
        this.wait = wait;
    }

    public boolean execute()
    {
        if(--wait <= 0)
        {
            task.run();
            return true;
        }

        return false;
    }
}
