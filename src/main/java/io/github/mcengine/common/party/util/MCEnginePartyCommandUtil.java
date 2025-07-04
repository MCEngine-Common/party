package io.github.mcengine.common.party.util;

import io.github.mcengine.common.party.MCEnginePartyCommon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class containing static handler methods for /party subcommands.
 */
public final class MCEnginePartyCommandUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private MCEnginePartyCommandUtil() {}

    /**
     * Handles the /party create command.
     *
     * @param player      The player issuing the command
     * @param partyCommon The party API handler
     */
    public static void handleCreate(Player player, MCEnginePartyCommon partyCommon) {
        String partyId = partyCommon.findPlayerPartyId(player);
        if (partyId != null) {
            player.sendMessage(ChatColor.RED + "You are already in a party.");
            return;
        }
        partyCommon.createParty(player);
        player.sendMessage(ChatColor.GREEN + "Party created! You are the party owner.");
    }

    /**
     * Handles the /party invite command.
     *
     * @param player      The player issuing the invite
     * @param targetName  The name of the player to invite
     * @param partyCommon The party API handler
     */
    public static void handleInvite(Player player, String targetName, MCEnginePartyCommon partyCommon) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found or not online.");
            return;
        }

        String partyId = partyCommon.findPlayerPartyId(player);
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party. Use /party create first.");
            return;
        }

        String role = partyCommon.getPlayerPartyRole(partyId, player);
        if (!"owner".equals(role)) {
            player.sendMessage(ChatColor.RED + "Only the party owner can invite players.");
            return;
        }

        if (partyCommon.isMember(partyId, target)) {
            player.sendMessage(ChatColor.RED + "Player is already in your party.");
            return;
        }

        partyCommon.invitePlayerToParty(partyId, target);
        player.sendMessage(ChatColor.GREEN + "Invited " + target.getName() + " to the party.");
        target.sendMessage(ChatColor.YELLOW + "You have been invited to join a party by " + player.getName() + ".");
    }

    /**
     * Handles the /party kick command.
     *
     * @param player      The player issuing the command
     * @param targetName  The name of the player to kick
     * @param partyCommon The party API handler
     */
    public static void handleKick(Player player, String targetName, MCEnginePartyCommon partyCommon) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found or not online.");
            return;
        }

        String partyId = partyCommon.findPlayerPartyId(player);
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        String role = partyCommon.getPlayerPartyRole(partyId, player);
        if (!"owner".equals(role)) {
            player.sendMessage(ChatColor.RED + "Only the party owner can kick members.");
            return;
        }

        if (!partyCommon.isMember(partyId, target)) {
            player.sendMessage(ChatColor.RED + "Player is not in your party.");
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(ChatColor.RED + "You cannot kick yourself. Use /party leave to disband the party.");
            return;
        }

        partyCommon.kickPlayerFromParty(partyId, target);
        player.sendMessage(ChatColor.GREEN + "Kicked " + target.getName() + " from the party.");
        target.sendMessage(ChatColor.RED + "You have been kicked from the party by " + player.getName() + ".");
    }

    /**
     * Handles the /party leave command.
     *
     * @param player      The player issuing the command
     * @param partyCommon The party API handler
     */
    public static void handleLeave(Player player, MCEnginePartyCommon partyCommon) {
        String partyId = partyCommon.findPlayerPartyId(player);
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        String role = partyCommon.getPlayerPartyRole(partyId, player);
        partyCommon.leaveParty(partyId, player);

        if ("owner".equals(role)) {
            player.sendMessage(ChatColor.YELLOW + "You have disbanded the party.");
            // Optionally notify other members, requires tracking party members.
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have left the party.");
        }
    }
}
