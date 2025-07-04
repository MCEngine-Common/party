package io.github.mcengine.common.party.listener;

import io.github.mcengine.common.party.MCEnginePartyCommon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player-related party events in the MC Engine Party plugin.
 * <p>
 * When a player leaves the server, if they are a member of a party, 
 * they will automatically leave the party.
 */
public class MCEnginePartyListener implements Listener {

    /**
     * Reference to the common party logic handler.
     */
    private final MCEnginePartyCommon partyCommon;

    /**
     * Constructs the listener with the party logic handler.
     *
     * @param partyCommon The party logic API instance.
     */
    public MCEnginePartyListener(MCEnginePartyCommon partyCommon) {
        this.partyCommon = partyCommon;
    }

    /**
     * Handles the PlayerQuitEvent. If the player is a member of a party,
     * this will remove the player from the party automatically.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String partyId = partyCommon.findPlayerPartyId(player);
        if (partyId != null) {
            partyCommon.leaveParty(partyId, player);
        }
    }
}
