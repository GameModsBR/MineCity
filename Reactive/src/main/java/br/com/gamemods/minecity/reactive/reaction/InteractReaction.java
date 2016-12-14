package br.com.gamemods.minecity.reactive.reaction;

import org.jetbrains.annotations.NotNull;

public final class InteractReaction
{
    @NotNull
    private Reaction useBlock = NoReaction.INSTANCE;

    @NotNull
    private Reaction useItem = NoReaction.INSTANCE;

    @NotNull
    private Reaction action = NoReaction.INSTANCE;

    public void combineBlock(Reaction reaction)
    {
        useBlock = useBlock.combine(reaction);
    }

    public void combineItem(Reaction reaction)
    {
        useItem = useItem.combine(reaction);
    }

    public void combineAction(Reaction reaction)
    {
        action = action.combine(reaction);
    }

    @NotNull
    public Reaction getUseBlock()
    {
        return useBlock;
    }

    @NotNull
    public Reaction getUseItem()
    {
        return useItem;
    }

    @NotNull
    public Reaction getAction()
    {
        return action;
    }
}
