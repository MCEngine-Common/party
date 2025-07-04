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
     * Invites another player to the specified party.
     *
     * @param party_id the ID of the party to which the player is being invited
     * @param player the player to be invited to the party
     */
    void invitePlayerToParty(String party_id, Player player);

    /**
     * Removes a player from the specified party.
     * This operation is typically performed by the party owner.
     *
     * @param party_id the ID of the party
     * @param player the player to be removed from the party
     */
    void kickPlayerFromParty(String party_id, Player player);

    /**
     * Removes the player from the specified party.
     * If the player is the owner, the party will be disbanded.
     * If the player is a member, they will simply leave the party.
     *
     * @param party_id the ID of the party
     * @param player the player who is leaving the party
     */
    void leaveParty(String party_id, Player player);

    /**
     * Checks if a player is a member of a specific party.
     *
     * @param party_id the ID of the party
     * @param player the player to check
     * @return true if the player is a member of the party, false otherwise
     */
    boolean isMember(String party_id, Player player);

    /**
     * Executes one or more raw SQL statements.
     *
     * @param sqls an array of SQL statements to execute
     */
    void executeSqls(String[] sqls);

    /**
     * Sets or changes the party name, if the given player is the party owner.
     *
     * @param party_id the ID of the party
     * @param player the player attempting to set the name
     * @param name the new name for the party
     * @return true if name was set successfully, false otherwise (e.g. not owner)
     */
    boolean setPartyName(String party_id, Player player, String name);
}
