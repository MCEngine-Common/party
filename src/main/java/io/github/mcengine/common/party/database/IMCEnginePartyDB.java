package io.github.mcengine.common.party.database;

import org.bukkit.entity.Player;

/**
 * Interface defining the operations for managing party-related data in the MC Engine plugin.
 */
public interface IMCEnginePartyDB {

    /**
     * Initializes the party database system.
     * This method should be called before any other methods to ensure the database is ready for operations.
     */
    void init();

    /**
     * Creates a new party and assigns the given player as the owner.
     *
     * @param player the player who will be the owner of the new party
     */
    void createParty(Player player);

    /**
     * Invites another player to the party of which the caller is a member or owner.
     *
     * @param player the player to be invited to the party
     */
    void invitePlayerToParty(Player player);

    /**
     * Removes a player from the party.
     * This operation is typically performed by the party owner.
     *
     * @param player the player to be removed from the party
     */
    void kickPlayerFromParty(Player player);

    /**
     * Removes the player from their current party.
     * If the player is the owner, the party will be disbanded.
     * If the player is a member, they will simply leave the party.
     *
     * @param player the player who is leaving the party
     */
    void leaveParty(Player player);

    /**
     * Checks if a player is a member of a specific party.
     *
     * @param partyId the ID of the party
     * @param player the player to check
     * @return true if the player is a member of the party
     */
    boolean isMember(String partyId, Player player);
}
