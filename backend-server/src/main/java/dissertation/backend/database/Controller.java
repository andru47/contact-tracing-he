package dissertation.backend.database;

import dissertation.backend.CiphertextWrapper;
import dissertation.backend.config.Config;
import dissertation.backend.config.EncryptionType;
import dissertation.backend.serialization.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
  private static final String SQL_INSERT_QUERY = "INSERT INTO locations(latitude_cos,latitude_sin,longitude_cos,longitude_sin,altitude,user_id,timestamp,timestamp_end) VALUES(?,?,?,?,?,?,?,?)";
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

  public static void addNewContactDistance(CiphertextWrapper distance, CiphertextWrapper altitudeDifference, String userId, String infectedLocationId, String locationId, String infectedUserId,
                                           String timestamp, String timestampEnd) {
    String sqlCommand = "INSERT INTO computed_distances(distance_ciphertext1, altitude_difference1, possible_contact_user_id, infected_location_id, location_id, timestamp, timestamp_end, infected_user_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    if (Config.getEncryptionType() != EncryptionType.SINGLE) {
      sqlCommand = "INSERT INTO computed_distances(distance_ciphertext1, altitude_difference1, possible_contact_user_id, infected_location_id, location_id, timestamp, timestamp_end, infected_user_id, distance_ciphertext2, altitude_difference2) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
      statement.setString(1, new String(distance.getComputedCiphertext1()));
      statement.setString(2, new String(altitudeDifference.getComputedCiphertext1()));
      statement.setString(3, userId);
      statement.setString(4, infectedLocationId);
      statement.setString(5, locationId);
      statement.setInt(6, Integer.parseInt(timestamp));
      statement.setInt(7, Integer.parseInt(timestampEnd));
      statement.setString(8, infectedUserId);
      if (Config.getEncryptionType() == EncryptionType.SMKHE_MK) {
        statement.setString(9, "");
        statement.setString(10, "");
      } else if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
        statement.setString(9, new String(distance.getComputedCiphertext2()));
        statement.setString(10, new String(altitudeDifference.getComputedCiphertext2()));
      }
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

  public static ResultSet getDistancesForUser(String userId, boolean partial) {
    String sqlCommand = "SELECT row_id, distance_ciphertext1, location_id, infected_location_id, infected_user_id, timestamp, timestamp_end, altitude_difference1 from computed_distances\n" +
        "where possible_contact_user_id = ? \n" +
        "and location_id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_location_id)\n" +
        "LIMIT 10";
    if (!partial && Config.getEncryptionType() != EncryptionType.SINGLE) {
      sqlCommand = "SELECT row_id, partial_distance, distance_ciphertext1, altitude_difference1, location_id, infected_location_id, infected_user_id, timestamp, timestamp_end, partial_altitude_difference from computed_distances\n" +
          "where possible_contact_user_id = ? \n" +
          "and location_id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_location_id)\n" +
          "LIMIT 10";
    } else if (partial) {
      if (Config.getEncryptionType() == EncryptionType.LATTIGO_MK) {
        sqlCommand = "SELECT row_id, partial_distance, distance_ciphertext2, altitude_difference2, location_id, infected_location_id, infected_user_id, timestamp, timestamp_end, partial_altitude_difference, downloaded from computed_distances\n" +
            "where infected_user_id = ? and downloaded=0 \n" +
            "and location_id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_location_id)\n" +
            "LIMIT 10";
      } else {
        sqlCommand = "SELECT row_id, partial_distance, distance_ciphertext1, altitude_difference1, location_id, infected_location_id, infected_user_id, timestamp, timestamp_end, partial_altitude_difference, downloaded from computed_distances\n" +
            "where infected_user_id = ? and downloaded=0 \n" +
            "and location_id NOT IN(SELECT contact_loc_id from processed_distances where infected_loc_id = infected_location_id)\n" +
            "LIMIT 10";
      }
    }
    try (PreparedStatement statement = connection.prepareStatement(sqlCommand, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
      statement.setString(1, userId);
      return statement.executeQuery();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

    return null;
  }

  public static void addNewLocationHistory(LocationHistoryMessage[] givenLocations) {
    try (Statement statement = connection.createStatement()) {
      for (int index = 0; index < givenLocations.length; ++index) {
        System.out.println(givenLocations[index].getLongitude());
        System.out.println(givenLocations[index].getLatitude());
        String sqlCommand = "INSERT INTO location_history(location, timestamp) VALUES(ST_GeomFromText('POINT(%.16f %.16f)'), %d)";
        System.out.println(String.format(sqlCommand, givenLocations[index].getLongitude(), givenLocations[index].getLatitude(), givenLocations[index].getLocationTimestamp()));
        statement.addBatch(String.format(sqlCommand, givenLocations[index].getLongitude(), givenLocations[index].getLatitude(), givenLocations[index].getLocationTimestamp()));
      }
      statement.executeBatch();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static int getNumberOfNearbyLocations(double latitude, double longitude) {
    System.out.println(longitude);
    System.out.println(latitude);
    String sqlSetCommand = String.format("SET @currentPoint = ST_GeomFromText('POINT(%.16f %.16f)')", longitude, latitude);
    String sqlCommand = "SELECT COUNT(*) from location_history\n" +
                        "where ST_Distance_Sphere(location, @currentPoint) <= 105";
    try (Statement statement = connection.createStatement()) {
      statement.execute(sqlSetCommand);
      try (ResultSet resultSet = statement.executeQuery(sqlCommand)) {
        resultSet.next();
        return resultSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
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

  private static List<String> getListFromSqlResult(String sqlCommand) {
    List<String> result = new ArrayList<>();
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

  public static List<String> getUsersThatNeedToDownloadDistances() {
    String sqlCommand;

    if (Config.getEncryptionType() != EncryptionType.SINGLE) {
      sqlCommand = "SELECT unique cd.possible_contact_user_id, ft.token from computed_distances as cd\n" +
          "join fcm_tokens as ft on ft.user_id = cd.possible_contact_user_id\n" +
          "where cd.partial_altitude_difference is not null";
    } else {
      sqlCommand = "SELECT unique cd.possible_contact_user_id, ft.token from computed_distances as cd\n" +
          "join fcm_tokens as ft on ft.user_id = cd.possible_contact_user_id";
    }

    return getListFromSqlResult(sqlCommand);
  }

  public static List<String> getInfectedUsersThatNeedToHalfDecrypt() {
    if (Config.getEncryptionType() == EncryptionType.SINGLE) {
      return new ArrayList<>();
    }

    String sqlCommand = "SELECT unique cd.infected_user_id, ft.token from computed_distances as cd\n" +
        "join fcm_tokens as ft on ft.user_id = cd.infected_user_id\n" +
        "where cd.partial_altitude_difference is null and cd.downloaded=false";

    return getListFromSqlResult(sqlCommand);
  }

  public static void uploadNewLocation(UploadDistanceMessage message) {
    updateEndTimestampForLastRecord(message.getId(), message.getTimestamp());
    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_QUERY)) {
      statement.setString(1, message.getLatitudeCos());
      statement.setString(2, message.getLatitudeSin());
      statement.setString(3, message.getLongitudeCos());
      statement.setString(4, message.getLongitudeSin());
      statement.setString(5, message.getAltitude());
      statement.setString(6, message.getId());
      statement.setString(7, message.getTimestamp());
      statement.setString(8, String.valueOf(Integer.parseInt(message.getTimestamp()) + 5));
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

  public static void addNewKeys(NewKeysMessage message) {
    String statement = "INSERT INTO `keys`(user_id, pub_key, relin_key) VALUES(?, ?, ?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
      preparedStatement.setString(1, message.getUserId());
      preparedStatement.setString(2, message.getPubKey());
      preparedStatement.setString(3, message.getRelinKey());
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static List<String> getKeysForUser(String userId) {
    String statement = "SELECT pub_key, relin_key from `keys` WHERE user_id=?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
      preparedStatement.setString(1, userId);
      try (ResultSet rs = preparedStatement.executeQuery()) {
        rs.first();
        return Arrays.asList(rs.getString("pub_key"), rs.getString("relin_key"));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void addNewPartial(NewPartialMessage message) {
    String statement = "UPDATE computed_distances set partial_distance = ?, partial_altitude_difference = ? where row_id=?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
      preparedStatement.setString(1, message.getPartialDistance());
      preparedStatement.setString(2, message.getPartialAltitudeDifference());
      preparedStatement.setInt(3, Integer.parseInt(message.getRowId()));
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
