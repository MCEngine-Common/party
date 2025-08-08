package io.github.mcengine.common.party.command;

import io.github.mcengine.common.party.MCEnginePartyCommon;
import io.github.mcengine.common.party.util.MCEnginePartyCommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for the <code>/party default</code> command and its subcommands.
 * <p>
 * The command structure has changed from <code>/party &lt;subcommand&gt;</code> to
 * <code>/party default &lt;subcommand&gt;</code>.
 * <p>
 * Supported subcommands:
 * <ul>
 *     <li>/party default create</li>
 *     <li>/party default invite &lt;player&gt;</li>
 *     <li>/party default kick &lt;player&gt;</li>
 *     <li>/party default leave</li>
 *     <li>/party default set name &lt;name&gt;</li>
 *     <li>/party default find &lt;player&gt;</li>
 * </ul>
 */
public class MCEnginePartyCommand implements CommandExecutor {

    /**
     * Reference to the party API for handling core party logic, including
     * configured party size limits and current member counts.
     */
    private final MCEnginePartyCommon partyCommon;

    /**
     * Required first argument that selects this command group (i.e., the subcommand namespace).
     * The new syntax is <code>/party default ...</code>.
     */
    private static final String MAIN_SUBCOMMAND = "default";

    /**
     * Canonical usage lines (without the leading "Usage:" label) for help output.
     */
    private static final String[] USAGE_LINES = new String[]{
            "/party default create",
            "/party default invite <player>",
            "/party default kick <player>",
            "/party default leave",
            "/party default set name <name>",
            "/party default find <player>"
    };

    /**
     * Constructs a new party command executor.
     *
     * @param partyCommon the shared party logic handler
     */
    public MCEnginePartyCommand(MCEnginePartyCommon partyCommon) {
        this.partyCommon = partyCommon;
    }

    /**
     * Handles the <code>/party default</code> command and its subcommands.
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

        // Must be at least: /party default
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        // Enforce the new "/party default ..." structure
        if (!MAIN_SUBCOMMAND.equalsIgnoreCase(args[0])) {
            sendUsage(player);
            return true;
        }

        // Must have an actual subcommand after "default"
        if (args.length == 1) {
            sendUsage(player);
            return true;
        }

        // Subcommand is now at args[1]
        switch (args[1].toLowerCase()) {
            case "create" -> MCEnginePartyCommandUtil.handleCreate(player, partyCommon);

            case "invite" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /party default invite <player>");
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
                    MCEnginePartyCommandUtil.handleInvite(player, args[2], partyCommon);
                }
            }

            case "kick" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /party default kick <player>");
                } else {
                    MCEnginePartyCommandUtil.handleKick(player, args[2], partyCommon);
                }
            }

            case "leave" -> MCEnginePartyCommandUtil.handleLeave(player, partyCommon);

            case "set" -> {
                if (args.length >= 4 && args[2].equalsIgnoreCase("name")) {
                    String name = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                    MCEnginePartyCommandUtil.handleSetName(player, name, partyCommon);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /party default set name <name>");
                }
            }

            case "find" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /party default find <player>");
                } else {
                    MCEnginePartyCommandUtil.handleFind(player, args[2], partyCommon);
                }
            }

            default -> sendUsage(player);
        }
        return true;
    }

    /**
     * Sends standardized usage/help lines to the player.
     *
     * @param player the recipient
     */
    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage:");
        for (String line : USAGE_LINES) {
            player.sendMessage(ChatColor.GRAY + line);
        }
    }
}
