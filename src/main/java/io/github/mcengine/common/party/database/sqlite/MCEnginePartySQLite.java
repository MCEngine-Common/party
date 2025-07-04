package io.github.mcengine.common.party.database.sqlite;

import io.github.mcengine.common.party.database.IMCEnginePartyDB;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

/**
 * SQLite implementation of the party system for MC Engine.
 */
public class MCEnginePartySQLite implements IMCEnginePartyDB {

    /** The plugin instance used for configuration and logging. */
    private final Plugin plugin;

    /** The persistent connection to the SQLite database. */
    private final Connection conn;

    /**
     * Constructs the SQLite handler and connects to the local SQLite database.
     *
     * @param plugin the Bukkit plugin instance
     */
    public MCEnginePartySQLite(Plugin plugin) {
        this.plugin = plugin;
        String fileName = plugin.getConfig().getString("database.sqlite.path", "party.db");

        File dbFile = new File(plugin.getDataFolder(), fileName);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        Connection tempConn = null;
        try {
            tempConn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to connect to SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        this.conn = tempConn;

        init();
    }

    @Override
    public void init() {
        String createPartyTable = """
            CREATE TABLE IF NOT EXISTS party (
                party_id INTEGER PRIMARY KEY AUTOINCREMENT,
                party_owner TEXT NOT NULL
            );
        """;

        String createPartyMemberTable = """
            CREATE TABLE IF NOT EXISTS party_member (
                party_member_id TEXT NOT NULL,
                party_id INTEGER NOT NULL,
                FOREIGN KEY (party_id) REFERENCES party(party_id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createPartyTable);
            stmt.execute(createPartyMemberTable);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to create SQLite party tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void createParty(Player player) {
        String insertParty = "INSERT INTO party (party_owner) VALUES (?)";
        String insertMember = "INSERT INTO party_member (party_member_id, party_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertParty, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int partyId = rs.getInt(1);
                try (PreparedStatement memberStmt = conn.prepareStatement(insertMember)) {
                    memberStmt.setString(1, player.getUniqueId().toString());
                    memberStmt.setInt(2, partyId);
                    memberStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to create party in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void invitePlayerToParty(String party_id, Player player) {
        String insertSql = "INSERT INTO party_member (party_member_id, party_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, Integer.parseInt(party_id));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invite player to party in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void kickPlayerFromParty(String party_id, Player player) {
        String deleteSql = "DELETE FROM party_member WHERE party_id = ? AND party_member_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to kick player from party in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void leaveParty(String party_id, Player player) {
        String checkOwnerSql = "SELECT party_owner FROM party WHERE party_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(checkOwnerSql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String ownerUuid = rs.getString("party_owner");
                if (ownerUuid.equals(player.getUniqueId().toString())) {
                    try (
                        PreparedStatement deleteMembers = conn.prepareStatement("DELETE FROM party_member WHERE party_id = ?");
                        PreparedStatement deleteParty = conn.prepareStatement("DELETE FROM party WHERE party_id = ?")
                    ) {
                        deleteMembers.setInt(1, Integer.parseInt(party_id));
                        deleteParty.setInt(1, Integer.parseInt(party_id));
                        deleteMembers.executeUpdate();
                        deleteParty.executeUpdate();
                    }
                } else {
                    kickPlayerFromParty(party_id, player);
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to leave party in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMember(String party_id, Player player) {
        String sql = "SELECT 1 FROM party_member WHERE party_id = ? AND party_member_id = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            stmt.setString(2, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException | NumberFormatException e) {
            plugin.getLogger().warning("Failed to check party membership in SQLite: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes one or more raw SQL statements.
     *
     * @param sqls an array of SQL strings to execute
     */
    @Override
    public void executeSqls(String[] sqls) {
        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqls) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to execute external SQL in SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
