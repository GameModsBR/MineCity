package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class DisplayedSelection<B> extends Selection
{
    public Map<BlockPos, B> display = new HashMap<>();
    protected B cornerA;
    protected B cornerB;
    protected B corners;
    protected B linesA;
    protected B linesB;
    protected B lines;
    protected B extension;

    protected DisplayedSelection(@NotNull WorldDim world)
    {
        super(world);
    }

    @Override
    public void select(BlockPos point)
    {
        super.select(point);
        updateDisplay();
    }

    public void hide()
    {
        Map<BlockPos, B> last = display;
        display = new HashMap<>(0);
        send(last);
    }

    @Override
    public void clear()
    {
        super.clear();
        updateDisplay();
    }

    @Async
    public void updateDisplay()
    {
        Map<BlockPos, B> last = display;
        Map<BlockPos, B> display = new HashMap<>(last.size());

        if(a != null)
        {
            if(b == null)
            {
                display.put(a, cornerA);
                for(Direction direction: Direction.cardinal)
                    display.put(a.add(direction), linesA);
            }
            else
            {
                //TODO: Simplify

                int range = 5;
                int dx = b.x - a.x;
                int dy = b.y - a.y;
                int dz = b.z - a.z;
                BlockPos c;

                // Min corner
                // x
                for(int ix = 1; ix < range && a.x+ix < b.x; ix++)
                    display.put(a.add(dx-ix, 0, 0), lines);

                c = a.add(0, dy, 0);
                for(int ix = 1; ix < range && a.x+ix < b.x; ix++)
                    display.put(c.add(ix, 0, 0), lines);

                c = a.add(0, 0, dz);
                for(int ix = 1; ix < range && a.x+ix < b.x; ix++)
                    display.put(c.add(ix, 0, 0), lines);

                // y
                for(int iy = 1; iy < range && a.y+iy < b.y; iy++)
                    display.put(a.add(0, dy-iy, 0), lines);

                c = a.add(dx, 0, 0);
                for(int iy = 1; iy < range && a.y+iy < b.y; iy++)
                    display.put(c.add(0, iy, 0), lines);

                c = a.add(0, 0, dz);
                for(int iy = 1; iy < range && a.y+iy < b.y; iy++)
                    display.put(c.add(0, iy, 0), lines);

                // z
                for(int iz = 1; iz < range && a.z+iz < b.z; iz++)
                    display.put(a.add(0, 0, dz-iz), lines);

                c = a.add(dx, 0, 0);
                for(int iz = 1; iz < range && a.z+iz < b.z; iz++)
                    display.put(c.add(0, 0, iz), lines);

                c = a.add(0, dy, 0);
                for(int iz = 1; iz < range && a.z+iz < b.z; iz++)
                    display.put(c.add(0, 0, iz), lines);

                // Max corner
                // x
                for(int ix = 1; ix < range && b.x-ix > a.x; ix++)
                    display.put(b.subtract(dx-ix, 0, 0), lines);

                c = b.subtract(0, dy, 0);
                for(int ix = 1; ix < range && b.x-ix > a.x; ix++)
                    display.put(c.subtract(ix, 0, 0), lines);

                c = b.subtract(0, 0, dz);
                for(int ix = 1; ix < range && b.x-ix > a.x; ix++)
                    display.put(c.subtract(ix, 0, 0), lines);

                // y
                for(int iy = 1; iy < range && b.y-iy > a.y; iy++)
                    display.put(b.subtract(0, dy-iy, 0), lines);

                c = b.subtract(dx, 0, 0);
                for(int iy = 1; iy < range && b.y-iy > a.y; iy++)
                    display.put(c.subtract(0, iy, 0), lines);

                c = b.subtract(0, 0, dz);
                for(int iy = 1; iy < range && b.y-iy > a.y; iy++)
                    display.put(c.subtract(0, iy, 0), lines);

                // z
                for(int iz = 1; iz < range && b.z-iz > a.z; iz++)
                    display.put(b.subtract(0, 0, dz-iz), lines);

                c = b.subtract(dx, 0, 0);
                for(int iz = 1; iz < range && b.z-iz > a.z; iz++)
                    display.put(c.subtract(0, 0, iz), lines);

                c = b.subtract(0, dy, 0);
                for(int iz = 1; iz < range && b.z-iz > a.z; iz++)
                    display.put(c.subtract(0, 0, iz), lines);

                // Selected corners
                for(int ix = 1; ix < range && a.x+ix < b.x; ix++)
                    display.put(a.add(ix, 0, 0), linesA);

                for(int iy = 1; iy < range && a.y+iy < b.y; iy++)
                    display.put(a.add(0, iy, 0), linesA);

                for(int iz = 1; iz < range && a.z+iz < b.z; iz++)
                    display.put(a.add(0, 0, iz), linesA);

                for(int ix = 1; ix < range && b.x-ix > a.x; ix++)
                    display.put(b.subtract(ix, 0, 0), linesB);

                for(int iy = 1; iy < range && b.y-iy > a.y; iy++)
                    display.put(b.subtract(0, iy, 0), linesB);

                for(int iz = 1; iz < range && b.z-iz > a.z; iz++)
                    display.put(b.subtract(0, 0, iz), linesB);

                // Extension
                for(int ix = range; a.x+ix < b.x-range; ix += 5)
                    display.put(a.add(ix, 0, 0), extension);

                c = a.add(0, dy, 0);
                for(int ix = range; a.x+ix < b.x-range; ix += 5)
                    display.put(c.add(ix, 0, 0), extension);

                c = c.add(0, 0, dz);
                for(int ix = range; a.x+ix < b.x-range; ix += 5)
                    display.put(c.add(ix, 0, 0), extension);

                c = a.add(0, 0, dz);
                for(int ix = range; a.x+ix < b.x-range; ix += 5)
                    display.put(c.add(ix, 0, 0), extension);

                for(int iy = range; a.y+iy < b.y-range; iy += 5)
                    display.put(a.add(0, iy, 0), extension);

                c = a.add(0, 0, dz);
                for(int iy = range; a.y+iy < b.y-range; iy += 5)
                    display.put(c.add(0, iy, 0), extension);

                c = a.add(dx, 0, 0);
                for(int iy = range; a.y+iy < b.y-range; iy += 5)
                    display.put(c.add(0, iy, 0), extension);

                c = c.add(0, 0, dz);
                for(int iy = range; a.y+iy < b.y-range; iy += 5)
                    display.put(c.add(0, iy, 0), extension);

                for(int iz = range; a.z+iz < b.z-range; iz += 5)
                    display.put(a.add(0, 0, iz), extension);

                c = a.add(0, dy, 0);
                for(int iz = range; a.z+iz < b.z-range; iz += 5)
                    display.put(c.add(0, 0, iz), extension);

                c = c.add(dx, 0, 0);
                for(int iz = range; a.z+iz < b.z-range; iz += 5)
                    display.put(c.add(0, 0, iz), extension);

                c = a.add(dx, 0, 0);
                for(int iz = range; a.z+iz < b.z-range; iz += 5)
                    display.put(c.add(0, 0, iz), extension);

                // Corners
                display.put(a.add(dx, 0, 0), corners);
                display.put(a.add(0, dy, 0), corners);
                display.put(a.add(0, 0, dz), corners);

                display.put(b.subtract(dx, 0, 0), corners);
                display.put(b.subtract(0, dy, 0), corners);
                display.put(b.subtract(0, 0, dz), corners);

                display.put(a, cornerA);
                display.put(b, cornerB);
            }
        }
        else if(b != null)
        {
            display.put(b, cornerB);
            for(Direction direction: Direction.cardinal)
                display.put(b.add(direction), linesB);
        }

        if(last.equals(display))
            return;

        this.display = new HashMap<>(display);

        send(last);
    }

    protected abstract void send(Map<BlockPos, B> last);

}
