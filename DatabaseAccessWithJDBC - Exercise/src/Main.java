import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

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
            case 4 -> exerciseFour();
            case 5 -> exerciseFive();
            case 7 -> exerciseSeven();
        }


    }

    private static void exerciseSeven() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM minions;");
        List<String> names = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            names.add(resultSet.getString(1));

        }
        int count = 0;
        for (int i = 0; i < names.size() / 2; i++) {
            System.out.println(names.get(i));
            System.out.println(names.get(names.size()-i -1));
        }
    }

    private static void exerciseFive() throws IOException, SQLException {
        System.out.println("Enter country name:");
        String countryName = reader.readLine();

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE towns SET name = UPPER(name) WHERE country = ?");
        preparedStatement.setString(1, countryName);
        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows == 0 ){
            System.out.println("No town names were affected.");
            return;
        }

        System.out.printf("%d town names were affected.%n", affectedRows);

        PreparedStatement preparedStatementTowns = connection.prepareStatement("SELECT name FROM towns WHERE country = ?");

        preparedStatementTowns.setString(1,countryName);
        ResultSet resultSet = preparedStatementTowns.executeQuery();

        while (resultSet.next()){
            System.out.println(resultSet.getString("name"));
        }

    }

    private static void exerciseFour() throws IOException, SQLException {
        System.out.println("Enter minion information:");
        String[] minionInformation = reader.readLine().split("\\s+");
        String minionName = minionInformation[1];
        int minionAge = Integer.parseInt(minionInformation[2]);
        String minionCity = minionInformation[3];
        String[] villain = reader.readLine().split("\\s+");
        String villainName = villain[1];

        if (!minionCityExists(minionCity)) {
            addMinionCityToDB(minionCity);
        }
        if (!villainExists(villainName)){
            addVillain(villainName);
        }

        addMinionToDB(minionName, minionAge, minionCity);

        addMinionToBeServantOfTheVillain(minionName, villainName);


    }

    private static void addMinionToBeServantOfTheVillain(String minionName, String villainName) throws SQLException {
        PreparedStatement findMinionId = connection.prepareStatement("SELECT id FROM minions WHERE name = ?;");
        findMinionId.setString(1, minionName);
        ResultSet resultSet = findMinionId.executeQuery();
        resultSet.next();
        int minionId = resultSet.getInt(1);
        PreparedStatement findVillainId = connection.prepareStatement("SELECT id FROM villains WHERE name = ?;");
        findVillainId.setString(1, villainName);
        ResultSet resultSet1 = findVillainId.executeQuery();
        resultSet1.next();
        int villainId = resultSet1.getInt(1);

        PreparedStatement preparedStatement = connection.prepareStatement("insert into minions_villains (minion_id, villain_id) values ( ?, ?);");
        preparedStatement.setInt(1, minionId);
        preparedStatement.setInt(2, villainId);
        preparedStatement.executeUpdate();

        System.out.println("Successfully added " + minionName +" to be minion of " + villainName);
    }

    private static void addMinionToDB(String minionName, int minionAge, String minionCity) throws SQLException {
        PreparedStatement minionCityId = connection.prepareStatement("SELECT id FROM towns WHERE name = ?;");
        minionCityId.setString(1, minionCity);
        ResultSet resultSet = minionCityId.executeQuery();
        resultSet.next();
        int cityId = resultSet.getInt(1);
        PreparedStatement preparedStatement = connection.prepareStatement("insert into minions ( name, age, town_id) values ( ?, ?, ?);");
        preparedStatement.setString(1, minionName);
        preparedStatement.setInt(2, minionAge);
        preparedStatement.setInt(3, cityId);
        preparedStatement.executeUpdate();

    }

    private static void addVillain(String villainName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into villains (name, evilness_factor) values (?, ?);");
        preparedStatement.setString(1, villainName);
        preparedStatement.setString(2, "evil");
        preparedStatement.executeUpdate();
        System.out.println("Villain " + villainName + " was added to the database.");
    }

    private static boolean villainExists(String villainName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM villains WHERE villains.name = ?;");
        preparedStatement.setString(1, villainName );

        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }

    private static void addMinionCityToDB(String minionCity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("insert into towns (name) values (?);");
        preparedStatement.setString(1, minionCity);
        preparedStatement.executeUpdate();
        System.out.println("Town " + minionCity + " was added to the database.");
    }

    private static boolean minionCityExists(String minionCity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM towns WHERE towns.name = ?;");
        preparedStatement.setString(1, minionCity );

        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next();
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

//        System.out.println("Enter user:");
//        String user = reader.readLine();
//        System.out.println("Enter password:");
//        String password = reader.readLine();

        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "1234");

        return DriverManager.getConnection("jdbc:mysql://localhost:3306/minions_db", properties);
    }
}
