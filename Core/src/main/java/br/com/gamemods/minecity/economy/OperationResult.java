package br.com.gamemods.minecity.economy;

public class OperationResult
{
    public final boolean success;
    public final double amount;
    public final String error;

    public OperationResult(boolean success, double amount, String error)
    {
        this.success = success;
        this.amount = amount;
        this.error = error;
    }

    public OperationResult(boolean success, double amount)
    {
        this.success = success;
        this.amount = amount;
        this.error = null;
    }
}
