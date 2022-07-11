import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class Main {

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static Connection connection;

    public static void main(String[] args) throws SQLException, IOException {

        connection = getConnection();

        System.out.println("Enter exercise number:");
        int exerciseNumber = Integer.parseInt(reader.readLine());

        switch (exerciseNumber) {
            case 2 -> exerciseTwo();
            case 3 -> exerciseThree();
        }


    }

    private static void exerciseThree() throws IOException, SQLException {
        System.out.println("Enter villain id:");
        int villainId = Integer.parseInt(reader.readLine());


        try {
            String villainName = findEntityNameById(villainId);
            System.out.println("Villain: " + villainName);
        } catch (Exception e){
            System.out.println("No villain with ID " + villainId + " exists in the database.");
        }

        Set<String> allMinionsByVillainId = getAllMinionsByVillainId(villainId);

        for (String minion : allMinionsByVillainId) {
            System.out.println(minion);
        }

    }

    private static Set<String> getAllMinionsByVillainId(int villainId) throws SQLException {
        Set<String> result = new LinkedHashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT m.name, m.age FROM minions m " +
                "join minions_villains mv on m.id = mv.minion_id " +
                "where mv.villain_id = ?;");
        preparedStatement.setInt(1, villainId);

        ResultSet resultSet = preparedStatement.executeQuery();
        int counter = 0;

        while (resultSet.next()) {
            result.add(String.format("%d. %s %d", ++counter,
                    resultSet.getString("name"),
                    resultSet.getInt("age")));
        }
        return result;
    }

    private static String findEntityNameById(int entityId) throws SQLException {
        String query = String.format("SELECT name FROM %s WHERE id = ?", "villains");
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, entityId);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }


    private static void exerciseTwo() throws SQLException {

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT v.name, COUNT(DISTINCT mv.minion_id) as `m_count` FROM villains v " +
                        "JOIN minions_villains mv on v.id = mv.villain_id " +
                        "GROUP BY v.name " +
                        "HAVING `m_count` > ?;");

        preparedStatement.setInt(1, 15);

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s %d %n", resultSet.getString(1), resultSet.getInt(2));
        }

    }


    private static Connection getConnection() throws IOException, SQLException {

        System.out.println("Enter user:");
        String user = reader.readLine();
        System.out.println("Enter password:");
        String password = reader.readLine();

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        return DriverManager.getConnection("jdbc:mysql://localhost:3306/minions_db", properties);
    }
}
