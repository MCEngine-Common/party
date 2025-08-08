package io.github.mcengine.common.party.command;

import io.github.mcengine.common.party.MCEnginePartyCommon;
import io.github.mcengine.common.party.util.MCEnginePartyCommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for the /party command and its subcommands.
 * <p>
 * Supported subcommands:
 * <ul>
 *     <li>/party create</li>
 *     <li>/party invite &lt;player&gt;</li>
 *     <li>/party kick &lt;player&gt;</li>
 *     <li>/party leave</li>
 *     <li>/party set name &lt;name&gt;</li>
 *     <li>/party find &lt;player&gt; (requires permission "mcengine.party.find")</li>
 * </ul>
 */
public class MCEnginePartyCommand implements CommandExecutor {

    /**
     * Reference to the party API for handling core party logic, including
     * configured party size limits and current member counts.
     */
    private final MCEnginePartyCommon partyCommon;

    /**
     * Constructs a new party command executor.
     *
     * @param partyCommon the shared party logic handler
     */
    public MCEnginePartyCommand(MCEnginePartyCommon partyCommon) {
        this.partyCommon = partyCommon;
    }

    /**
     * Handles the /party command and its subcommands.
     *
     * @param sender The command sender
     * @param command The command
     * @param label The command label used
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by players.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Party commands: /party create, /party invite <player>, /party kick <player>, /party leave, /party set name <name>, /party find <player>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> MCEnginePartyCommandUtil.handleCreate(player, partyCommon);
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                } else {
                    // Enforce party size limit before attempting to invite.
                    // Limit is read from config via MCEnginePartyCommon#getPartyLimit().
                    // A limit of 0 means unlimited size.
                    int limit = partyCommon.getPartyLimit();
                    if (limit > 0) {
                        String inviterPartyId = partyCommon.findPlayerPartyId(player);
                        if (inviterPartyId != null) {
                            int count = partyCommon.getPartyCount(inviterPartyId);
                            if (count >= limit) {
                                player.sendMessage(ChatColor.RED + "Your party is full (" + count + "/" + limit + ").");
                                return true;
                            }
                        }
                    }
                    MCEnginePartyCommandUtil.handleInvite(player, args[1], partyCommon);
                }
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party kick <player>");
                } else {
                    MCEnginePartyCommandUtil.handleKick(player, args[1], partyCommon);
                }
            }
            case "leave" -> MCEnginePartyCommandUtil.handleLeave(player, partyCommon);
            case "set" -> {
                if (args.length >= 3 && args[1].equalsIgnoreCase("name")) {
                    String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                    MCEnginePartyCommandUtil.handleSetName(player, name, partyCommon);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /party set name <name>");
                }
            }
            case "find" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party find <player>");
                } else {
                    MCEnginePartyCommandUtil.handleFind(player, args[1], partyCommon);
                }
            }
            default -> player.sendMessage(ChatColor.YELLOW + "Unknown party command. Try: /party create, /party invite <player>, /party kick <player>, /party leave, /party set name <name>, /party find <player>");
        }
        return true;
    }
}
