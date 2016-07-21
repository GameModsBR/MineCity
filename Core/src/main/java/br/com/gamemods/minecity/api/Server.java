package br.com.gamemods.minecity.api;

import java.util.Optional;

public interface Server
{
    Optional<PlayerID> getPlayerId(String name);
}
