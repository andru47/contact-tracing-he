package dissertation.backend.database;

import dissertation.backend.serialization.ContactMessage;
import dissertation.backend.serialization.UploadDistanceMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
  private static final String SQL_INSERT_QUERY = "INSERT INTO locations(latitude_cos,latitude_sin,longitude_cos,longitude_sin,user_id,timestamp,timestamp_end) VALUES(?,?,?,?,?,?,?)";
  private static Connection connection = null;

  static {
    try {
      connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/he-database?user=root&password=password");
    } catch (SQLException throwables) {
      throwables.printStackTrace();
      throw new RuntimeException("I couldn't connect to the database");
    }
  }

  public static void executeDeleteStaleStatement(String sqlStatement, Object obj) {
    try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
      statement.setString(1, obj.toString());
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static void executeDeleteOldContacts() {
    String sqlCommand = "DELETE from contacts where user_id in (select user_id from quarantined_users)";
    try (Statement statement = connection.createStatement()) {
      statement.execute(sqlCommand);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static String getTokenForUser(String userId) {
    String sqlCommand = "SELECT token from fcm_tokens where user_id = ?";
    String token = null;
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, userId);
      ResultSet rs = statement.executeQuery();
      if (rs == null) {
        return null;
      }
      rs.first();
      token = rs.getString("token");
      rs.close();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return token;
  }

  public static void updateTokenForUser(String userId, String token) {
    String sqlCommand = "INSERT into fcm_tokens(user_id, token) VALUES (?, ?) ON DUPLICATE KEY UPDATE token=VALUES(token)";

    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, userId);
      statement.setString(2, token);
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static void addNewContactDistance(String distance, String userId, String infectedLocationId, String locationId, String infectedUserId,
                                           String timestamp, String timestampEnd) {
    String sqlCommand = "INSERT INTO computed_distances(distance_ciphertext, possible_contact_user_id, infected_location_id, location_id, timestamp, timestamp_end, infected_user_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, distance);
      statement.setString(2, userId);
      statement.setString(3, infectedLocationId);
      statement.setString(4, locationId);
      statement.setInt(5, Integer.parseInt(timestamp));
      statement.setInt(6, Integer.parseInt(timestampEnd));
      statement.setString(7, infectedUserId);
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static void addNewInfectedUser(String userId, long timestamp) {
    String sqlCommand = "INSERT into quarantined_users(user_id, end) VALUES(?,?)";
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, userId);
      statement.setString(2, String.valueOf(timestamp));
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static void addNewProcessedLocationPair(String infectedLocation, String contactLocation) {
    String sqlCommand = "INSERT into processed_distances(infected_loc_id, contact_loc_id) VALUES(?, ?)";
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, infectedLocation);
      statement.setString(2, contactLocation);
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static ResultSet getResultSetFromStatement(String sqlStatement) {
    try (Statement statement = connection.createStatement()) {
      return statement.executeQuery(sqlStatement);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return null;
  }

  public static ResultSet getDistancesForUser(String userId) {
    String sqlCommand = "SELECT row_id, distance_ciphertext, location_id, infected_location_id, infected_user_id, timestamp, timestamp_end from computed_distances\n" +
        "where possible_contact_user_id = ? \n" +
        "and location_id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_location_id)\n" +
        "LIMIT 10";
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
      statement.setString(1, userId);
      return statement.executeQuery();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return null;
  }

  public static void addNewContact(ContactMessage message) {
    String sqlCommand = "INSERT INTO contacts(user_id,infected_user_id,timestamp,timestamp_end) VALUES(?, ?, ?, ?)";

    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, message.getUserId());
      statement.setString(2, message.getInfectedUserId());
      statement.setLong(3, message.getTimestamp());
      statement.setLong(4, message.getTimestampEnd());

      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static ResultSet getContacts(String userId, String infectedUserId) {
    String sqlCommand = "SELECT timestamp, timestamp_end from contacts where user_id = ? and infected_user_id = ? order by timestamp";
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, userId);
      statement.setString(2, infectedUserId);
      return statement.executeQuery();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return null;
  }

  public static List<String> getUsersThatNeedToDownloadDistances() {
    List<String> result = new ArrayList<>();
    String sqlCommand = "SELECT unique cd.possible_contact_user_id, ft.token from computed_distances as cd\n" +
        "join fcm_tokens as ft on ft.user_id = cd.possible_contact_user_id";
    try (ResultSet rs = connection.createStatement().executeQuery(sqlCommand)) {
      if (rs == null) {
        return result;
      }
      while (rs.next()) {
        result.add(rs.getString("token"));
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return result;
  }

  public static void uploadNewLocation(UploadDistanceMessage message) {
    updateEndTimestampForLastRecord(message.getId(), message.getTimestamp());
    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_QUERY)) {
      statement.setString(1, message.getLatitudeCos());
      statement.setString(2, message.getLatitudeSin());
      statement.setString(3, message.getLongitudeCos());
      statement.setString(4, message.getLongitudeSin());
      statement.setString(5, message.getId());
      statement.setString(6, message.getTimestamp());
      statement.setString(7, String.valueOf(Integer.parseInt(message.getTimestamp()) + 5));
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  private static void updateEndTimestampForLastRecord(String userId, String timestamp) {
    String sqlStatement = "UPDATE locations set timestamp_end = ? where timestamp in (SELECT max(timestamp) from locations where user_id=?) and user_id=? and ? - timestamp <= 900;";
    try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
      statement.setString(1, timestamp);
      statement.setString(2, userId);
      statement.setString(3, userId);
      statement.setInt(4, Integer.parseInt(timestamp));
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public static String getDummyElement() {
    try (Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery("SELECT distance_ciphertext from computed_distances where location_id=25")) {
      rs.first();
      return rs.getString("distance_ciphertext");
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return "";
  }

  public static String[] getElement() {
    try (Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery("SELECT latitude_cos, latitude_sin, longitude_cos, longitude_sin from locations where user_id='a0b9cce1-682e-43dc-b6b2-b5a24209ea22'")) {
      rs.first();
      return new String[]{rs.getString("latitude_cos"), rs.getString("latitude_sin"), rs.getString("longitude_cos"), rs.getString("longitude_sin")};
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return new String[]{};
  }
}