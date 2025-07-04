package io.github.mcengine.common.party;

import io.github.mcengine.common.party.database.IMCEnginePartyDB;
import io.github.mcengine.common.party.database.mysql.MCEnginePartyMySQL;
import io.github.mcengine.common.party.database.sqlite.MCEnginePartySQLite;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Common logic handler for the MCEngine Party plugin.
 * Handles database backend initialization and provides access to party data methods.
 */
public class MCEnginePartyCommon {

    /**
     * Singleton instance of the Party Common manager.
     */
    private static MCEnginePartyCommon instance;

    /**
     * The database implementation used for party data.
     */
    private final IMCEnginePartyDB db;

    /**
     * The Bukkit plugin instance.
     */
    private final Plugin plugin;

    /**
     * Constructs a new Party Common handler.
     * Initializes the appropriate database backend based on plugin config.
     *
     * Supported database types (config key: {@code database.type}):
     * <ul>
     *     <li>{@code sqlite}</li>
     *     <li>{@code mysql}</li>
     * </ul>
     *
     * @param plugin the Bukkit plugin instance
     */
    public MCEnginePartyCommon(Plugin plugin) {
        instance = this;
        this.plugin = plugin;

        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        switch (dbType) {
            case "sqlite" -> this.db = new MCEnginePartySQLite(plugin);
            case "mysql" -> this.db = new MCEnginePartyMySQL(plugin);
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    /**
     * Returns the singleton instance of the party common handler.
     *
     * @return {@link MCEnginePartyCommon} global instance
     */
    public static MCEnginePartyCommon getApi() {
        return instance;
    }

    /**
     * Gets the associated plugin instance.
     *
     * @return Bukkit plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Creates a new party and assigns the given player as the owner.
     *
     * @param player the player who will be the owner of the new party
     */
    public void createParty(Player player) {
        db.createParty(player);
    }

    /**
     * Invites another player to the specified party.
     *
     * @param partyId the ID of the party to which the player is being invited
     * @param player the player to be invited to the party
     */
    public void invitePlayerToParty(String partyId, Player player) {
        db.invitePlayerToParty(partyId, player);
    }

    /**
     * Removes a player from the specified party.
     * This operation is typically performed by the party owner.
     *
     * @param partyId the ID of the party
     * @param player the player to be removed from the party
     */
    public void kickPlayerFromParty(String partyId, Player player) {
        db.kickPlayerFromParty(partyId, player);
    }

    /**
     * Removes the player from the specified party.
     * If the player is the owner, the party will be disbanded.
     * If the player is a member, they will simply leave the party.
     *
     * @param partyId the ID of the party
     * @param player the player who is leaving the party
     */
    public void leaveParty(String partyId, Player player) {
        db.leaveParty(partyId, player);
    }

    /**
     * Checks if a player is a member of a specific party.
     *
     * @param partyId the ID of the party
     * @param player the player to check
     * @return true if the player is a member of the party, false otherwise
     */
    public boolean isMember(String partyId, Player player) {
        return db.isMember(partyId, player);
    }

    /**
     * Executes one or more raw SQL statements directly against the database.
     *
     * @param sqls an array of SQL statements to execute
     */
    public void executeSqls(String[] sqls) {
        db.executeSqls(sqls);
    }

    /**
     * Gets the role of the specified player in the party.
     * Returns "owner" if the player is the owner, "member" if they are a member, or null if not found.
     *
     * @param partyId the ID of the party
     * @param player the player whose role is to be checked
     * @return "owner", "member", or null
     */
    public String getPlayerPartyRole(String partyId, Player player) {
        return db.getPlayerPartyRole(partyId, player);
    }

    /**
     * Finds the party ID the player belongs to, or null if not in a party.
     *
     * @param player The player to check
     * @return The party ID if found, or null
     */
    public String findPlayerPartyId(Player player) {
        return db.findPlayerPartyId(player);
    }

    /**
     * Sets the name of the specified party, if the player is the owner.
     *
     * @param partyId The ID of the party
     * @param player  The player attempting to set the party name
     * @param name    The new party name to set
     * @return true if the party name was set, false otherwise
     */
    public boolean setPartyName(String partyId, Player player, String name) {
        return db.setPartyName(partyId, player, name);
    }

    /**
     * Finds the party ID that the specified player belongs to by player name.
     *
     * @param playerName The name of the player to look up
     * @return The party ID if found, or null
     */
    public String findPlayerPartyId(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            return db.findPlayerPartyId(player);
        }
        return null;
    }
}
