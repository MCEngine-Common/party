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
 * </ul>
 */
public class MCEnginePartyCommand implements CommandExecutor {

    /**
     * Reference to the party API for handling core party logic.
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
            player.sendMessage(ChatColor.YELLOW + "Party commands: /party create, /party invite <player>, /party kick <player>, /party leave");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> MCEnginePartyCommandUtil.handleCreate(player, partyCommon);
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                } else {
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
            default -> player.sendMessage(ChatColor.YELLOW + "Unknown party command. Try: /party create, /party invite <player>, /party kick <player>, /party leave");
        }
        return true;
    }
}
