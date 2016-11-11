package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.StringUtil;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.economy.BalanceResult;
import br.com.gamemods.minecity.economy.OperationResult;
import br.com.gamemods.minecity.structure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlotCommand
{
    @Command(value = "plot.info", console = false, args = @Arg(name = "plot name", sticky = true, optional = true, type = Arg.Type.PLOT))
    public static Message info(CommandEvent cmd)
    {
        Plot plot;
        if(cmd.args.isEmpty())
        {
            plot = cmd.getChunk().getPlotAt(cmd.position.getBlock()).orElse(null);
            if(plot == null)
                return new Message("cmd.plot.info.not-inside-plot", "You are not inside a plot");
        }
        else
        {
            Island island = cmd.getChunk().getIsland().orElse(null);
            if(island == null)
                return new Message("cmd.plot.info.not-inside-city", "You are not inside a city");

            String name = String.join(" ", cmd.args);
            plot = island.getPlot(name).orElseGet(()-> island.getCity().getPlot(name).orElse(null));
            if(plot == null)
                return new Message("cmd.plot.info.not-found",
                        "The city ${city} does not contains a plot named ${plot}",
                        new Object[][]{
                                {"city", island.getCity().getName()},
                                {"plot", name}
                        }
                );
        }

        BlockPos spawn = plot.getSpawn();
        Shape shape = plot.getShape();
        City city = plot.getCity();
        OptionalPlayer mayor = city.owner();
        cmd.sender.send(new Message(
                "cmd.plot.info.page",
                "<msg><darkgray>---<yellow>-=[Plot: ${name}]=-</yellow>-----------</darkgray><br/>\n" +
                "<aqua>City: </aqua><white>${city}</white><br/>\n" +
                "<aqua>Location: </aqua><white>${nature-name} X:${spawn-x} Y:${spawn-y} Z:${spawn-z}</white><br/>\n" +
                "<aqua>Owner: </aqua><white>${owner}</white><br/>\n" +
                "<aqua>Size: </aqua><white>${area-squared}m², ${area-cube}m³, X:${size-x}, Y:${size-y}, Z:${size-z}</white><br/>\n" +
                "<aqua>Price: </aqua><white>${price}</white>"+
                "<br/><darkgreen>----------------------------------</darkgreen></msg>",
                new Object[][]{
                        {"name", plot.getName()},
                        {"city", city.getName()},
                        {"nature-name", spawn.world.name()},
                        {"spawn-x", spawn.x},
                        {"spawn-y", spawn.y},
                        {"spawn-z", spawn.z},
                        {"owner", plot.getOwner().map(Identity::getName).map(Message::string).orElse(
                                mayor.player() != null?
                                    new Message("cmd.plot.info.mayor", "<msg><i>${city}</i>'s mayor: ${mayor}</msg>",
                                            new Object[][]{
                                                    {"city", city.getName()},
                                                    {"mayor", mayor.getName()}
                                            }
                                    ) :
                                    new Message("cmd.plot.info.admin", "<msg><i>The server administrators</i></msg>",
                                            new Object[][]{
                                                    {"city", city.getName()},
                                                    {"admin", mayor.getName()}
                                            }
                                    )
                        )},
                        {"area-squared", shape.squareSize()},
                        {"area-cube", shape.area()},
                        {"size-x", shape.sizeX()},
                        {"size-y", shape.sizeY()},
                        {"size-z", shape.sizeZ()},
                        {"price", plot.getPrice() < 1? new Message("cmd.plot.info.not-selling", "Not for sale") : cmd.mineCity.economy.format(plot.getPrice())}
                }
        ));

        return null;
    }

    @Slow
    @Async
    @Command(value = "plot.create", console = false, args = @Arg(name = "plot name", sticky = true))
    public static CommandResult<?> create(CommandEvent cmd) throws DataSourceException
    {
        Selection selection = cmd.sender.getSelection(cmd.position.world);
        if(selection.isIncomplete())
        {
            cmd.sender.getServer().callSyncMethod(cmd.sender::giveSelectionTool);
            return new CommandResult<>(new Message("cmd.plot.create.no-selection",
                    "Select the plot area and then execute this command again."
            ), true);
        }

        String name = String.join(" ", cmd.args);
        String identityName = StringUtil.identity(name);
        if(identityName.length() < 3)
            return new CommandResult<>(new Message("cmd.plot.create.name-too-short",
                    "Please type a bigger name"
            ));

        Shape shape = selection.toShape();
        Island island = cmd.getChunk().getIsland().orElse(null);
        if(island == null)
            return new CommandResult<>(new Message("cmd.plot.create.not-claimed", "You are not inside a city"));

        if(!cmd.sender.getPlayerId().equals(island.getCity().owner()))
            return new CommandResult<>(new Message("cmd.plot.create.no-permission",
                    "You don't have permission to create plots inside ${city}",
                    new Object[]{"city", island.getCity().getName()}
            ));

        if(!shape.contains(cmd.position.getBlock()))
            return new CommandResult<>(new Message("cmd.plot.create.outside",
                    "Stand inside the plot and execute this command again."
            ));

        Optional<Plot> conflict = island.getCity().getPlot(name);
        if(conflict.isPresent())
            return new CommandResult<>(new Message("cmd.plot.create.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{
                            {"name", name}, {"conflict", conflict.get().getName()}
                    }
            ));

        Optional<ChunkPos> unclaimed = shape.chunks(selection.world)
                .filter(c ->
                        !cmd.mineCity.getOrFetchChunkUnchecked(c)
                                .flatMap(ClaimedChunk::getIsland)
                                .filter(island::equals)
                                .isPresent()
                ).findAny();

        if(unclaimed.isPresent())
            return new CommandResult<>(new Message("cmd.plot.create.overlaps.unclaimed",
                    "The selected area overlaps an unclaimed chunk located at: X: ${x} and Z: ${z}",
                    new Object[][]{
                            {"x", unclaimed.get().x}, {"z", unclaimed.get().z}
                    }
            ));

        List<Plot> overlaps = island.getPlots().stream().filter(p -> p.getShape().overlaps(shape))
                .collect(Collectors.toList());

        if(!overlaps.isEmpty())
        {
            if(overlaps.size() > 1)
                return new CommandResult<>(new Message("cmd.plot.create.overlaps.plots",
                        "The selected area overlaps ${count} plots: ${plots}",
                        new Object[][]{
                                {"count", overlaps.size()},
                                {"plots", Message.list(
                                        overlaps.stream().map(Plot::getName).sorted()
                                                .map(Message::new).toArray(Message[]::new)
                                )}
                        }
                ));
            else
                return new CommandResult<>(new Message("cmd.plot.create.overlaps.plot",
                        "The selected area overlaps the plot ${name}",
                        new Object[]{"name", overlaps.get(0).getName()}
                ));
        }

        Plot plot = island.createPlot(name, null, cmd.position.getBlock(), shape);
        selection.clear();
        return new CommandResult<>(new Message("cmd.plot.create.success",
                "The plot ${name} was created successfully",
                new Object[]{"name", plot.getName()}
        ), plot);
    }

    @Command(value = "plot.readjust", console = false, args = @Arg(name = "plot-name", optional = true, sticky = true, type = Arg.Type.PLOT))
    public static CommandResult<?> readjust(CommandEvent cmd)
    {
        Selection selection = cmd.sender.getSelection(cmd.position.world);
        if(selection.isIncomplete())
        {
            cmd.sender.getServer().callSyncMethod(cmd.sender::giveSelectionTool);
            return new CommandResult<>(new Message("cmd.plot.readjust.no-selection",
                    "Select the new area and then execute this command again."
            ), true);
        }

        City city;
        Plot plot;
        Island island;
        if(cmd.args.isEmpty())
        {
            plot = cmd.getChunk().getPlotAt(cmd.position.getBlock()).orElse(null);
            if(plot == null)
                return new CommandResult<>(new Message("cmd.plot.readjust.not-inside-plot", "You are not inside a plot"));
            island = plot.getIsland();
            city = island.getCity();
        }
        else
        {
            island = cmd.getChunk().getIsland().orElse(null);
            if(island == null)
                return new CommandResult<>(new Message("cmd.plot.readjust.not-inside-city", "You are not inside a city"));
            city = island.getCity();

            String name = String.join(" ", cmd.args);
            plot = island.searchPlot(name).orElse(null);
            if(plot == null)
                return new CommandResult<>(new Message("cmd.plot.readjust.not-found",
                        "The city ${city} does not contains a plot named ${name}",
                        new Object[][]{
                                {"city", city.getName()},
                                {"name", name}
                        }
                ));
        }

        Shape shape = selection.toShape();
        if(!cmd.sender.getPlayerId().equals(city.owner()))
            return new CommandResult<>(new Message("cmd.plot.readjust.no-city-permission",
                    "You don't have permission to readjust plots in ${city}",
                    new Object[]{"city", city.getName()}
            ));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.readjust.no-plot-permission",
                    "Plots can only be readjusted when they are owned by the city's mayor, ${plot} is owned by ${owner}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"owner", plot.owner().getName()}
                    }
            ));

        if(!shape.contains(cmd.position.getBlock()))
            return new CommandResult<>(new Message("cmd.plot.readjust.outside",
                    "Stand inside the new area and execute this command again."
            ));

        Optional<ChunkPos> unclaimed = shape.chunks(selection.world)
                .filter(c ->
                        !cmd.mineCity.getOrFetchChunkUnchecked(c)
                                .flatMap(ClaimedChunk::getIsland)
                                .filter(island::equals)
                                .isPresent()
                ).findAny();

        if(unclaimed.isPresent())
            return new CommandResult<>(new Message("cmd.plot.create.readjust.unclaimed",
                    "The selected area overlaps an unclaimed chunk located at: X: ${x} and Z: ${z}",
                    new Object[][]{
                            {"x", unclaimed.get().x}, {"z", unclaimed.get().z}
                    }
            ));

        List<Plot> overlaps = island.getPlots().stream().filter(p -> !p.equals(plot) && p.getShape().overlaps(shape))
                .collect(Collectors.toList());

        if(!overlaps.isEmpty())
        {
            if(overlaps.size() > 1)
                return new CommandResult<>(new Message("cmd.plot.readjust.overlaps.plots",
                        "The selected area overlaps ${count} plots: ${plots}",
                        new Object[][]{
                                {"count", overlaps.size()},
                                {"plots", Message.list(
                                        overlaps.stream().map(Plot::getName).sorted()
                                                .map(Message::new).toArray(Message[]::new)
                                )}
                        }
                ));
            else
                return new CommandResult<>(new Message("cmd.plot.readjust.overlaps.plot",
                        "The selected area overlaps the plot ${name}",
                        new Object[]{"name", overlaps.get(0).getName()}
                ));
        }

        String code = cmd.sender.confirm(sender -> {
            Selection sel = sender.getSelection(island.world);
            if(sel.isIncomplete() || !sel.toShape().equals(shape))
                return new CommandResult<>(new Message("cmd.plot.readjust.selection-changed",
                        "The selection has changed before the confirmation. Execute the readjust command again to receive a new code."
                ));

            BlockPos spawn = sender.getPosition().getBlock();
            if(!shape.contains(spawn))
                return new CommandResult<>(new Message("cmd.plot.readjust.outside",
                        "Stand inside the new area and execute this command again."
                ));

            plot.setShape(shape, spawn);
            selection.clear();
            return new CommandResult<>(new Message("cmd.plot.readjust.success",
                    "The plot ${name} was readjusted successfully",
                    new Object[]{"name", plot.getName()}
            ), plot);
        });

        String fallback = "${size-square}m² ${area}m³ X:${size-x}m Y:${size-y}m Z:${size-z}";
        Shape old = plot.getShape();
        //noinspection LanguageMismatch
        return new CommandResult<>(new Message("cmd.plot.readjust.confirm",
                "You are about to readjust the plot ${plot} in ${city}, the plot size will change from ${from-size} to ${to-size}." +
                "If you are sure about this then type /plot confirm ${code}",
                new Object[][]{
                        {"plot", plot.getName()},
                        {"city", city.getName()},
                        {"code", code},
                        {"from-size", new Message("cmd.plot.readjust.size", fallback, new Object[][]{
                                {"size-square", old.squareSize()},
                                {"area", old.area()},
                                {"size-x", old.sizeX()},
                                {"size-y", old.sizeY()},
                                {"size-z", old.sizeZ()}
                        })},
                        {"to-size", new Message("cmd.plot.readjust.size", fallback, new Object[][]{
                                {"size-square", shape.squareSize()},
                                {"area", shape.area()},
                                {"size-x", shape.sizeX()},
                                {"size-y", shape.sizeY()},
                                {"size-z", shape.sizeZ()}
                        })}
                }
        ), true);
    }

    @Slow
    @Async
    @Command(value = "plot.rename", console = false, args = @Arg(name = "new name", sticky = true))
    public static CommandResult<?> rename(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.rename.not-claimed", "You are not inside a plot"));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.rename.no-permission",
                    "You don't have permission to rename the plot ${plot}",
                    new Object[]{"plot", plot.getName()}
            ));

        String name = String.join(" ", cmd.args);
        String identity = StringUtil.identity(name);
        if(identity.length() < 3)
            return new CommandResult<>(new Message("cmd.plot.rename.name-too-short", "Please type a bigger name"));

        if(name.equals(plot.getName()))
            return new CommandResult<>(new Message("cmd.plot.rename.already-named",
                    "The plot is already named ${name}",
                    new Object[]{"name", name}
            ));

        Optional<Plot> conflict = plot.getCity().getPlot(identity);
        if(conflict.isPresent())
            return new CommandResult<>(new Message("cmd.plot.rename.conflict",
                    "The name ${name} conflicts with ${conflict}",
                    new Object[][]{
                            {"name", name}, {"conflict", conflict.get().getName()}
                    }
            ));

        String old = plot.getName();
        plot.setName(name);
        return new CommandResult<>(new Message("cmd.plot.rename.success",
                "The plot ${old} is now named ${name}",
                new Object[][]{
                        {"old",old}, {"new",name}
                }
        ), true);
    }

    @Command(value = "plot.return", console = false)
    public static CommandResult<?> returnPlot(CommandEvent cmd)
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.return.not-claimed", "You are not inside a plot"));

        if(!plot.getOwner().isPresent())
            return new CommandResult<>(new Message("cmd.plot.return.already", "The plot ${plot} is already owned by the mayor of the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()}, {"city", plot.getCity().getName()}
                    }));

        if(!cmd.sender.getPlayerId().equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.return.no-permission",
                    "You don't have permission to return the ownership of ${plot} to the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()}
                    }));

        String code = cmd.sender.confirm(sender -> {
            plot.setOwner(null);
            return new CommandResult<>(new Message("cmd.plot.return.success",
                    "The plot ${plot} has returned to the city ${city}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()}
                    }), true);
        });

        return new CommandResult<>(new Message("cmd.plot.return.confirm",
                "<msg>You are about to return the plot ${plot} and everything that is in it to the city ${city}, all permissions will be reset " +
                "and you'll no longer be able to control the plot unless you have the appropriate city city's permission or the mayor grants them later, " +
                "<b>you'll not be refunded by this action</b>. If you are sure about this, type /plot confirm ${code}</msg>",
                new Object[][]{
                        {"plot", plot.getName()},
                        {"city", plot.getCity().getName()},
                        {"code", code}
                }), code);
    }

    @Slow
    @Async
    @Command(value = "plot.list", console = false, args = {
            @Arg(name = "city", optional = true, sticky = true, type = Arg.Type.CITY),
            @Arg(name = "page", type = Arg.Type.NUMBER, optional = true)
    })
    public static CommandResult<?> list(CommandEvent cmd) throws DataSourceException
    {
        int page = 1;
        List<String> args = cmd.args instanceof ArrayList? cmd.args : new ArrayList<>(cmd.args);
        if(!args.isEmpty())
        {
            int index = args.size() - 1;
            String last = args.get(index);
            if(last.matches("^[0-9]+$"))
            {
                page = Integer.parseInt(last);
                args.remove(index);
            }
        }

        City city;
        if(args.isEmpty())
        {
            city = cmd.getChunk().getCity().orElse(null);
            if(city == null)
                return new CommandResult<>(new Message("cmd.plot.list.not-claimed", "You are not inside a city"));
        }
        else
        {
            String cityName = String.join(" ", args);
            city = cmd.mineCity.dataSource.getCityByName(cityName).orElse(null);
            if(city == null)
                return new CommandResult<>(new Message("cmd.plot.list.not-found", "No city were found named ${name}",
                        new Object[]{"name", cityName})
                );
        }

        List<Plot> plots = city.plots().sorted((a, b) -> a.getIdentityName().compareToIgnoreCase(b.getIdentityName()))
                .collect(Collectors.toList());

        if(plots.isEmpty())
            return new CommandResult<>(new Message(
                    "cmd.plot.list.no-plots",
                    "The city ${city} does not have plots.",
                    new Object[]{"city", city.getName()}
            ), true);

        int pages = (int) Math.ceil(plots.size() / 8.0);
        page = Math.min(pages, page);
        int index = 8 * (page-1);

        Message[] lines = new Message[2 + Math.min(8, plots.size()-index)];
        for(int i = 1; i < lines.length-1; i++, index++)
        {
            Plot plot = plots.get(index);
            lines[i] = new Message("cmd.plot.list.plot",
                    "<msg><darkgray><![CDATA[ * ]]></darkgray><white>${plot}</white> <gray>${size-x}x${size-z}x${size-y} ~${owner}</gray></msg>",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"size-x", plot.getShape().sizeX()},
                            {"size-y", plot.getShape().sizeY()},
                            {"size-z", plot.getShape().sizeZ()},
                            {"owner", plot.getOwner()
                                    .map(id-> new Message("cmd.plot.list.owner", "${name}", new Object[]{"name", id.getName()}))
                                    .orElseGet(()-> new Message("cmd.plot.list.no-owner", "<msg><darkgray>nobody</darkgray></msg>"))
                            }
                    }
            );
        }

        lines[0] = new Message("cmd.plot.list.header",
                "<msg><green>---<yellow>-=[Plots at ${city}]=-</yellow>---</green></msg>",
                new Object[]{"city", city.getName()}
        );
        lines[lines.length-1] = (pages == 1)?
                new Message("cmd.plot.list.footer.one-page",
                        "<msg><green>\n" +
                                "    Page <gold>1</gold>/<gold>1</gold>\n" +
                                "    <darkgreen>---</darkgreen>\n" +
                                "    Tip: Type <click><suggest cmd='/city go '/><hover><tooltip><gold>/city go</gold></tooltip><gold>/city go</gold></hover></click> to go to the city\n" +
                                "</green></msg>")
                : page == pages?
                new Message("cmd.plot.list.footer.last-page",
                        "<msg><green>\n" +
                                "    Page <gold>${page}</gold>/<gold>${page}</gold>\n" +
                                "    <darkgreen>---</darkgreen>\n" +
                                "    Tip: Type <click><suggest cmd='/city go '/><hover><tooltip><gold>/city go</gold></tooltip><gold>/city go</gold></hover></click> to go to the city\n" +
                                "</green></msg>",
                        new Object[][]{
                                {"page", page}
                        })
                :
                new Message("cmd.plot.list.footer.more-pages",
                        "<msg><green>\n" +
                                "    Page <gold>${page}</gold>/<gold>${total}</gold>\n" +
                                "    <darkgreen>---</darkgreen>\n" +
                                "    Next page: <hover>\n" +
                                "    <tooltip><gold>${next-page}</gold></tooltip>\n" +
                                "    <click>\n" +
                                "        <suggest cmd=\"${next-page}\"/>\n" +
                                "        <gold>${next-page}</gold>\n" +
                                "    </click></hover>\n" +
                                "</green></msg>",
                        new Object[][]{
                                {"page", page},
                                {"total", pages},
                                {"next-page", "/"+String.join(" ", cmd.path)+" "+(page + 1)}
                        }
                );

        cmd.sender.send(lines);
        return CommandResult.success();
    }

    @Slow
    @Async
    @Command(value = "plot.transfer", console = false, args = @Arg(name = "player name", type = Arg.Type.PLAYER))
    public static CommandResult<?> transfer(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.transfer.not-claimed", "You are not inside a plot"));

        PlayerID senderId = cmd.sender.getPlayerId();
        if(!senderId.equals(plot.owner()))
            return new CommandResult<>(new Message("cmd.plot.transfer.no-permission",
                    "You don't have permission to transfer the plot ${plot}",
                    new Object[]{"plot", plot.getName()}
            ));

        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.plot.transfer.player.empty",
                    "Type a player name"));

        if(cmd.args.size() > 1)
            return new CommandResult<>(new Message("cmd.plot.transfer.too-many-args",
                    "Player names does not have spaces"
        ));

        String playerName = cmd.args.get(0);
        PlayerID target = cmd.mineCity.findPlayer(playerName).orElse(null);
        if(target == null)
            return new CommandResult<>(new Message("cmd.plot.transfer.player.not-found",
                    "No player was found with name ${name}",
                    new Object[]{"name",playerName}
            ));

        if(target.equals(senderId))
        {
            if(plot.getOwner().isPresent())
                return new CommandResult<>(new Message("cmd.plot.transfer.self.already",
                        "You already own the plot ${plot}",
                        new Object[]{"plot", plot.getName()}
                ));

            plot.setOwner(senderId);
            return new CommandResult<>(new Message("cmd.plot.transfer.self.success",
                    "The plot ${plot} is now your personal plot",
                    new Object[]{"plot", plot.getName()}
            ), senderId);
        }

        if(target.equals(plot.getCity().owner()))
        {
            String code = cmd.sender.confirm(sender -> {
                plot.setOwner(target);
                return new CommandResult<>(new Message("cmd.plot.transfer.mayor.success",
                        "The plot ${plot} is now a ${target}'s personal plot.",
                        new Object[][]{
                                {"plot", plot.getName()},
                                {"target", target.getName()}
                        }
                ), true);
            });

            return new CommandResult<>(new Message("cmd.plot.transfer.mayor.confirm",
                    "<msg>You are about to transfer the plot ${plot} and everything that is in it to ${target}, the plot " +
                    "<b>will not be returned to the city</b> " +
                    "and will become a ${target}'s personal plot. You'll not be refunded and you'll not be able to " +
                    "undo this action. If you are sure about it type /plot confirm ${code}</msg>",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"target", target.getName()},
                            {"code", code}
                    }
            ), code);
        }
        else
        {
            String code = cmd.sender.confirm(sender -> {
                plot.setOwner(target);
                return new CommandResult<>(new Message("cmd.plot.transfer.player.success",
                        "The plot ${plot} is now owned by ${target}",
                        new Object[][]{
                                {"plot", plot.getName()},
                                {"target", target.getName()}
                        }
                ), true);
            });

            return new CommandResult<>(new Message("cmd.plot.transfer.player.confirm",
                    "<msg>You are about to transfer the plot ${plot} and everything that is in it to ${target}. " +
                    "You'll not be refunded and <b>you'll not be able to  undo this action</b>. " +
                    "If you are sure about it type /plot confirm ${code}</msg>",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"target", target.getName()},
                            {"code", code}
                    }
            ), code);
        }
    }

    @Command(value = "plot.delete", console = false)
    public static CommandResult<?> delete(CommandEvent cmd)
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.delete.not-claimed", "You are not inside a plot"));

        PlayerID senderId = cmd.sender.getPlayerId();
        if(!senderId.equals(plot.getCity().owner()))
            return new CommandResult<>(new Message("cmd.plot.delete.not-mayor",
                    "Only the mayor of ${city} can delete plots, you can return this plot to the city using /plot return",
                    new Object[]{"city", plot.getCity().getName()}
            ));

        Optional<PlayerID> plotOwner = plot.getOwner();
        if(plotOwner.isPresent() && !plotOwner.get().equals(senderId))
            return new CommandResult<>(new Message("cmd.plot.delete.no-permission",
                    "You don't have permission to delete the plot ${plot} because it's owned by ${owner}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"owner", plotOwner.get().getName()}
                    }
            ));

        String code = cmd.sender.confirm(sender -> {
            plot.delete();
            return new CommandResult<>(new Message("cmd.plot.delete.success",
                    "The plot ${plot} was deleted successfully",
                    new Object[]{"plot", plot.getName()}
            ), true);
        });

        return new CommandResult<>(new Message("cmd.plot.delete.confirm",
                "You are about to delete the plot ${plot} from the city ${city}, if you are sure about it type /plot confirm ${code}",
                new Object[][]{
                        {"plot", plot.getName()},
                        {"city", plot.getCity().getName()},
                        {"code", code}
                }), true
        );
    }

    @Slow
    @Async
    @Command(value = "plot.abort.sell", console = false)
    public static CommandResult<?> abortSell(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.abort.sell.not-claimed", "You are not inside a plot"));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!plot.owner().equals(playerId))
            return new CommandResult<>(new Message("cmd.plot.abort.sell.no-permission",
                    "You don't have permission abort the sale of ${plot}, only ${owner} can do that",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"owner", plot.owner().getName()}
                    }
            ));

        if(plot.getPrice() < 1.0)
            return new CommandResult<>(new Message("cmd.plot.abort.sell.not-selling",
                    "The plot ${plot} is not for sale",
                    new Object[]{"plot", plot.getName()}
            ));

        plot.setPrice(0);
        return new CommandResult<>(new Message("cmd.plot.abort.sell.success",
                "The plot ${plot} is no longer for sale",
                new Object[]{"plot", plot.getName()}
        ));
    }

    @Command(value = "plot.sell", console = false, args = @Arg(name = "price", type = Arg.Type.NUMBER))
    public static CommandResult<String> sell(CommandEvent cmd)
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.sell.not-claimed", "You are not inside a plot"));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!plot.owner().equals(playerId))
            return new CommandResult<>(new Message("cmd.plot.sell.no-permission",
                    "You don't have permission to sell the plot ${plot}, only ${owner} can do that",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"owner", plot.owner().getName()}
                    }
            ));

        if(plot.can(PermissionFlag.ENTER).isPresent() || plot.can(PermissionFlag.CLICK).isPresent())
            return new CommandResult<>(new Message("cmd.plot.sell.perms",
                    "You can't sell ${plot} because you need to allow players to get inside and click on things.",
                    new Object[]{"plot", plot.getName()}
            ));

        if(cmd.args.size() != 1)
            return new CommandResult<>(new Message("cmd.plot.sell.no-args",
                    "You need to type the price..."
            ));

        double price;
        try
        {
            price = Double.parseDouble(cmd.args.get(0).replace(',','.'));
        }
        catch(NumberFormatException e)
        {
            return new CommandResult<>(new Message("cmd.plot.sell.not-number", "The price needs to be a number, it can be fractional but can't have thousands separators."));
        }

        if(price < 1.0)
            return new CommandResult<>(new Message("cmd.plot.sell.free",
                    "The price can't be less then ${minimum}",
                    new Object[]{"minimum", cmd.mineCity.economy.format(1.0)}
            ));

        String code = cmd.sender.confirm((sender)-> {
            plot.setPrice(price);
            return new CommandResult<>(new Message("cmd.plot.sell.success",
                    "The plot ${plot} is now for sale by ${price}. Type /plot abort sell if you change your mind.",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"price", cmd.mineCity.economy.format(price)}
                    }
            ), true);
        });

        double from = plot.getPrice();
        if(from < 1)
            return new CommandResult<>(new Message("cmd.plot.sell.confirm-new-sell",
                    "You are about to sell the plot ${plot} that is not being sold and is inside the city ${city} by ${price}. If you are sure about it type /plot confirm ${code}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()},
                            {"code", code},
                            {"price", cmd.mineCity.economy.format(price)}
                    }
            ), code);
        else
            return new CommandResult<>(new Message("cmd.plot.sell.confirm-price-change",
                    "You are about to change the price of the plot ${plot} that is inside the city ${city} from ${from} to ${to}. If you are sure about it type /plot confirm ${code}",
                    new Object[][]{
                            {"plot", plot.getName()},
                            {"city", plot.getCity().getName()},
                            {"code", code},
                            {"to", cmd.mineCity.economy.format(price)},
                            {"from", cmd.mineCity.economy.format(plot.getPrice())}
                    }
            ), code);
    }

    @Slow
    @Async
    @Command(value = "plot.buy", console = false)
    public static CommandResult<?> buy(CommandEvent cmd) throws DataSourceException
    {
        Plot plot = cmd.mineCity.getPlot(cmd.position.getBlock()).orElse(null);
        if(plot == null)
            return new CommandResult<>(new Message("cmd.plot.buy.not-claimed", "You are not inside a plot"));

        if(plot.owner().equals(cmd.sender.getPlayerId()))
            return new CommandResult<>(new Message("cmd.plot.buy.own", "You can't buy your own plot"));

        double price = plot.getPrice();
        if(price < 1.0)
            return new CommandResult<>(new Message("cmd.plot.buy.not-selling", "The plot ${name} is not for sale",
                    new Object[]{"name", plot.getName()}
            ));

        PlayerID playerId = cmd.sender.getPlayerId();
        if(!cmd.mineCity.economy.has(playerId, price, cmd.position.world).result)
            return new CommandResult<>(new Message("cmd.plot.buy.economy.insufficient-funds",
                    "Insufficient funds, you need ${money} to purchase ${plot}",
                    new Object[][]{
                            {"money", cmd.mineCity.economy.format(price)},
                            {"plot", plot.getName()}
                    }
            ));

        String code = cmd.sender.confirm(sender->
        {
            BalanceResult balance = cmd.mineCity.economy.has(playerId, price, cmd.position.world);
            if(!balance.result)
                return new CommandResult<>(new Message("cmd.plot.buy.economy.insufficient-funds",
                        "Insufficient funds, you need ${money} to purchase ${plot}",
                        new Object[][]{
                                {"money", cmd.mineCity.economy.format(price)},
                                {"plot", plot.getName()}
                        }
                ));

            OperationResult charge = cmd.mineCity.economy.charge(cmd.sender, price, balance, cmd.position.world);
            if(!charge.success)
            {
                if(charge.error == null)
                    return new CommandResult<>(new Message("cmd.plot.buy.economy.charge.error-unknown",
                            "Oopss... An unknown error has occurred while processing your transaction."
                    ));
                else
                    return new CommandResult<>(new Message("cmd.plot.buy.economy.charge.error",
                            "The purchase has failed: ${error}",
                            new Object[]{"error", charge.error}
                    ));
            }

            double investment = price - charge.amount;

            PlayerID old = plot.owner().player();
            try
            {
                plot.setOwner(playerId);
            }
            catch(Throwable e)
            {
                cmd.mineCity.economy.refund(playerId, investment, balance, cmd.position.world, e);
                throw e;
            }

            Message revert = null;
            Throwable ex = null;
            if(old != null)
                try
                {
                    OperationResult give = cmd.mineCity.economy.give(old, investment, null, plot.getSpawn().world, false);
                    if(!give.success)
                    {
                        if(charge.error == null)
                            revert = new Message("cmd.plot.buy.economy.give.error-unknown",
                                    "Oopss... An unknown error has occurred while transferring the money to the old owner."
                            );
                        else
                            revert = new Message("cmd.plot.buy.economy.give.error",
                                    "The purchase has failed: ${error}",
                                    new Object[]{"error", charge.error}
                            );
                    } else
                    {
                        if(give.amount < 0)
                        {
                            OperationResult adjust = cmd.mineCity.economy.charge(cmd.sender, -give.amount, null, cmd.position.world);
                            if(adjust.success)
                                investment -= give.amount + adjust.amount;
                        }
                    }
                }
                catch(Throwable e)
                {
                    e.printStackTrace();
                    ex = e;
                    revert = new Message("cmd.plot.buy.economy.give.exception",
                            "An exception has occurred while giving the money to the old owner"
                    );
                }

            if(revert != null)
            {
                try
                {
                    if(ex != null)
                        cmd.mineCity.economy.refund(playerId, investment, null, cmd.position.world, ex);
                    else
                        cmd.mineCity.economy.refund(playerId, investment, null, cmd.position.world, true);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                return new CommandResult<>(revert);
            }


            boolean errorOnReset = false;
            for(PermissionFlag flag : PermissionFlag.values())
            {
                try
                {
                    plot.resetAll(flag);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    errorOnReset = true;
                }

                try
                {
                    if(!flag.defaultPlot)
                        plot.deny(flag);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    errorOnReset = true;
                }
            }

            if(errorOnReset)
                cmd.sender.send(CommandFunction.messageFailed(new Message(
                        "cmd.plot.buy.error-reset",
                        "An error has occurred while resetting the plot permissions, you might want to take a look at /plot check"
                )));

            try
            {
                plot.invest(investment);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return new CommandResult<>(new Message("cmd.plot.buy.success",
                    "Congratulations! The plot ${plot} is now yours.",
                    new Object[]{"plot", plot.getName()}
            ), true);
        });

        return new CommandResult<>(new Message("cmd.plot.buy.confirm",
                "You are about to purchase the plot ${plot} in ${city} by ${money}. If you are sure about it type /plot confirm ${code}",
                new Object[][]{
                        {"plot", plot.getName()},
                        {"city", plot.getCity().getName()},
                        {"money", cmd.mineCity.economy.format(price)},
                        {"code", code}
                }
        ));
    }
}
