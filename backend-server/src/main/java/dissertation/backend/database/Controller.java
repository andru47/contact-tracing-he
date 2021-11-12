package dissertation.backend.database;

import dissertation.backend.serialization.UploadDistanceMessage;

import java.sql.*;

public class Controller {
  private static Connection connection = null;
  private static final String SQL_INSERT_QUERY = "INSERT INTO locations(timestamp,latitude_cos,latitude_sin,longitude_cos,longitude_sin,user_id) VALUES(?,?,?,?,?,?)";

  static {
    try {
      connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/he-database?user=root&password=password");
    } catch (SQLException throwables) {
      throwables.printStackTrace();
      throw new RuntimeException("I couldn't connect to the database");
    }
  }

  public void uploadNewLocation(UploadDistanceMessage message) {
    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_QUERY)) {
      statement.setString(1, message.getTimestamp());
      statement.setString(2, message.getLatitudeCos());
      statement.setString(3, message.getLatitudeSin());
      statement.setString(4, message.getLongitudeCos());
      statement.setString(5, message.getLongitudeSin());
      statement.setString(6, message.getId());
      statement.executeUpdate();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  public String[] getElement() {
    try (Statement statement = connection.createStatement()) {
      ResultSet rs = statement.executeQuery("SELECT latitude_cos, latitude_sin, longitude_cos, longitude_sin from locations where user_id='a0b9cce1-682e-43dc-b6b2-b5a24209ea22'");
      rs.first();
      return new String[]{rs.getString("latitude_cos"), rs.getString("latitude_sin"), rs.getString("longitude_cos"), rs.getString("longitude_sin")};
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return new String[]{};
  }
}
