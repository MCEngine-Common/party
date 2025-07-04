package io.github.mcengine.common.party.database.mysql;

import io.github.mcengine.common.party.database.IMCEnginePartyDB;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.*;

/**
 * MySQL implementation of the party system for MC Engine.
 */
public class MCEnginePartyMySQL implements IMCEnginePartyDB {

    /**
     * The plugin instance used for configuration and logging.
     */
    private final Plugin plugin;

    /**
     * The persistent connection to the MySQL database.
     */
    private final Connection conn;

    /**
     * Constructs the MySQL handler and connects to the database.
     *
     * @param plugin the Bukkit plugin instance
     */
    public MCEnginePartyMySQL(Plugin plugin) {
        this.plugin = plugin;

        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        String port = plugin.getConfig().getString("database.mysql.port", "3306");
        String dbName = plugin.getConfig().getString("database.mysql.name", "mcengine");
        String user = plugin.getConfig().getString("database.mysql.user", "root");
        String pass = plugin.getConfig().getString("database.mysql.password", "");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&autoReconnect=true";

        Connection tempConn = null;
        try {
            tempConn = DriverManager.getConnection(jdbcUrl, user, pass);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to connect to MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        this.conn = tempConn;

        init();
    }

    /**
     * Initializes the MySQL tables for the party system if they do not already exist.
     * This method must be called before any other database operations are performed.
     */
    @Override
    public void init() {
        String createPartyTable = """
            CREATE TABLE IF NOT EXISTS party (
                party_id INT AUTO_INCREMENT PRIMARY KEY,
                party_owner_id VARCHAR(36) NOT NULL,
                party_name VARCHAR(255) DEFAULT NULL
            );
        """;

        String createPartyMemberTable = """
            CREATE TABLE IF NOT EXISTS party_member (
                party_member_id VARCHAR(36) NOT NULL,
                party_id INT NOT NULL,
                FOREIGN KEY (party_id) REFERENCES party(party_id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createPartyTable);
            stmt.execute(createPartyMemberTable);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to create party tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a new party with the specified player as the owner and member.
     * Inserts the player as both the owner in the party table and as a member in the party_member table.
     *
     * @param player the player who will be the owner of the new party
     */
    @Override
    public void createParty(Player player) {
        String insertParty = "INSERT INTO party (party_owner_id) VALUES (?)";
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
            plugin.getLogger().warning("Failed to create party: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Invites a player to an existing party by adding them as a member in the party_member table.
     *
     * @param party_id the ID of the party to which the player is being invited
     * @param player the player to be invited to the party
     */
    @Override
    public void invitePlayerToParty(String party_id, Player player) {
        String insertSql = "INSERT INTO party_member (party_member_id, party_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, Integer.parseInt(party_id));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to invite player to party: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kicks a player from the specified party by removing their record from the party_member table.
     *
     * @param party_id the ID of the party
     * @param player the player to be removed from the party
     */
    @Override
    public void kickPlayerFromParty(String party_id, Player player) {
        String deleteSql = "DELETE FROM party_member WHERE party_id = ? AND party_member_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to kick player from party: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Removes the player from the specified party.
     * If the player is the owner, the entire party and its members will be deleted.
     * If the player is a regular member, only their party_member record is removed.
     *
     * @param party_id the ID of the party
     * @param player the player who is leaving the party
     */
    @Override
    public void leaveParty(String party_id, Player player) {
        String checkOwnerSql = "SELECT party_owner_id FROM party WHERE party_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(checkOwnerSql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String ownerUuid = rs.getString("party_owner_id");
                if (ownerUuid.equals(player.getUniqueId().toString())) {
                    // Owner: delete party and members
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
                    // Member: remove from party
                    kickPlayerFromParty(party_id, player);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to process leaveParty: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the specified player is a member of the specified party.
     *
     * @param party_id the ID of the party
     * @param player the player to check
     * @return true if the player is a member of the party, false otherwise
     */
    @Override
    public boolean isMember(String party_id, Player player) {
        String sql = "SELECT 1 FROM party_member WHERE party_id = ? AND party_member_id = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(party_id));
            stmt.setString(2, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException | NumberFormatException e) {
            plugin.getLogger().warning("Failed to check party membership: " + e.getMessage());
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
            plugin.getLogger().warning("Failed to execute external SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the party name if the given player is the owner of the party.
     *
     * @param party_id the ID of the party
     * @param player the player attempting to set the name
     * @param name the new name for the party
     * @return true if the name was set, false otherwise
     */
    @Override
    public boolean setPartyName(String party_id, Player player, String name) {
        String checkOwnerSql = "SELECT party_owner_id FROM party WHERE party_id = ?";
        String updateNameSql = "UPDATE party SET party_name = ? WHERE party_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkOwnerSql)) {
            checkStmt.setInt(1, Integer.parseInt(party_id));
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String ownerUuid = rs.getString("party_owner_id");
                if (ownerUuid.equals(player.getUniqueId().toString())) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateNameSql)) {
                        updateStmt.setString(1, name);
                        updateStmt.setInt(2, Integer.parseInt(party_id));
                        updateStmt.executeUpdate();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to set party name: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the role of the specified player in the party.
     * Returns "owner" if the player is the owner, "member" if they are a member, or null if not found.
     *
     * @param party_id the ID of the party
     * @param player the player whose role is to be checked
     * @return "owner", "member", or null
     */
    @Override
    public String getPlayerPartyRole(String party_id, Player player) {
        String uuid = player.getUniqueId().toString();
        String checkOwnerSql = "SELECT party_owner_id FROM party WHERE party_id = ?";
        String checkMemberSql = "SELECT 1 FROM party_member WHERE party_id = ? AND party_member_id = ? LIMIT 1";

        try (PreparedStatement ownerStmt = conn.prepareStatement(checkOwnerSql)) {
            ownerStmt.setInt(1, Integer.parseInt(party_id));
            ResultSet ownerRs = ownerStmt.executeQuery();
            if (ownerRs.next()) {
                if (uuid.equals(ownerRs.getString("party_owner_id"))) {
                    return "owner";
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check party owner: " + e.getMessage());
            e.printStackTrace();
        }

        try (PreparedStatement memberStmt = conn.prepareStatement(checkMemberSql)) {
            memberStmt.setInt(1, Integer.parseInt(party_id));
            memberStmt.setString(2, uuid);
            ResultSet memberRs = memberStmt.executeQuery();
            if (memberRs.next()) {
                return "member";
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check party member: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Finds the party ID that the specified player belongs to.
     * Returns the party ID as a string if the player is in a party, or null if not found.
     *
     * @param player the player to look up
     * @return party ID if found, or null
     */
    @Override
    public String findPlayerPartyId(Player player) {
        String uuid = player.getUniqueId().toString();
        // Check if player is a party owner
        String sqlOwner = "SELECT party_id FROM party WHERE party_owner_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlOwner)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return String.valueOf(rs.getInt("party_id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check party ownership: " + e.getMessage());
            e.printStackTrace();
        }

        // Check if player is a party member
        String sqlMember = "SELECT party_id FROM party_member WHERE party_member_id = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sqlMember)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return String.valueOf(rs.getInt("party_id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check party membership: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
