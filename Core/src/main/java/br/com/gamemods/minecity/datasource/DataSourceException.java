package br.com.gamemods.minecity.datasource;

public class DataSourceException extends Exception
{
    private static final long serialVersionUID = -4316157990842793035L;

    public DataSourceException()
    {
    }

    public DataSourceException(String message)
    {
        super(message);
    }

    public DataSourceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataSourceException(Throwable cause)
    {
        super(cause);
    }
}
