package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.datasource.api.CityCreationResult;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class City
{
    @NotNull
    public final MineCity mineCity;
    private ICityStorage storage;

    /**
     * ID defined by the data source implementation, may be zero but cannot be negative
     */
    private int id;
    @NotNull
    private String name;
    private PlayerID owner;
    private BlockPos spawn;
    private List<Island> islands = new ArrayList<>(1);

    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn) throws IllegalArgumentException
    {
        this.mineCity = mineCity;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        ClaimedChunk other = mineCity.getChunk(spawn);
        if(other != null && !(other.owner instanceof Nature))
            throw new IllegalArgumentException("The chunk "+spawn.getChunk()+" is reserved to "+other.owner);
    }

    public City(@NotNull MineCity mineCity, @NotNull String name, @Nullable PlayerID owner, @NotNull BlockPos spawn,
                Collection<Island> islands, int id, @NotNull ICityStorage storage)
    {
        this.mineCity = mineCity;
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        setId(id);
        this.storage = storage;
        this.islands = new ArrayList<>(islands);
    }

    public Island getIsland(int id)
    {
        return islands.stream().filter(i->i.getId() == id).findAny().orElse(null);
    }

    public Collection<Island> islands()
    {
        return Collections.unmodifiableCollection(islands);
    }

    @Nullable
    public PlayerID getOwner()
    {
        return owner;
    }

    public int getSizeX()
    {
        if(storage == null) return 1;
        return islands.stream().mapToInt(Island::getSizeX).sum();
    }

    public int getSizeZ()
    {
        if(storage == null) return 1;
        return islands.stream().mapToInt(Island::getSizeZ).sum();
    }

    public int getChunkCount()
    {
        if(storage == null) return 1;
        return islands.stream().mapToInt(Island::getChunkCount).sum();
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    public BlockPos getSpawn()
    {
        return spawn;
    }

    public void claim(ChunkPos chunk) throws IllegalArgumentException
    {
        if(mineCity.getChunk(chunk) != null)
            throw new IllegalArgumentException("The chunk "+chunk+" is reserved");

        //TODO
        throw new UnsupportedOperationException();
    }

    public void setSpawn(BlockPos pos) throws DataSourceException,IllegalArgumentException
    {
        ChunkPos chunk = pos.getChunk();
        ClaimedChunk claim = mineCity.getChunk(pos);
        if(claim == null)
            claim = mineCity.dataSource.getCityChunk(chunk);

        if(claim == null || !(claim.owner instanceof Island) || !Objects.equals(this, ((Island) claim.owner).getCity()))
            throw new IllegalArgumentException("The block "+pos+" is not part of the city");

        if(storage != null) storage.setSpawn(this, pos);
        this.spawn = pos;
    }

    /**
     * Changes the owner of the city and saves it immediately
     * @param owner The new owner or {@code null} for server admins
     * @throws DataSourceException If the city is registered and the change failed. The owner will not be set in this case.
     */
    public void setOwner(@Nullable PlayerID owner) throws DataSourceException
    {
        if(storage != null) storage.setOwner(this, owner);
        this.owner = owner;
    }

    /**
     * Register this city as a new city. Must not be called if this city is not new.
     * @return The city ID.
     * @throws DataSourceException If an error occurred while registering
     * @throws IllegalStateException If this city is aready registered
     */
    public int create() throws DataSourceException, IllegalStateException
    {
        if(storage != null) throw new IllegalStateException("The city \""+name+"\" is already registered with ID "+id);
        CityCreationResult result = mineCity.dataSource.createCity(this);
        storage = result.storage;
        islands.add(result.island);
        return id;
    }

    /**
     * Defines the City ID, this can be done only once and should only be done by the {@link IDataSource} implementation.
     * @throws IllegalStateException If the defined ID is different then the passed ID
     * @throws IllegalArgumentException If {@code < 0}
     */
    public void setId(int id) throws IllegalStateException, IllegalArgumentException
    {
        if(id < 0)
            throw new IllegalArgumentException("id = "+id);
        if(this.id > 0 && id != this.id)
            throw new IllegalStateException("Tried to change the city's \""+name+"\" ID from "+this.id+" to "+id);

        this.id = id;
    }

    /**
     * @return The City ID or {@code 0} if it's undefined
     */
    public int getId()
    {
        if(storage == null)
            return 0;
        return id;
    }
}
