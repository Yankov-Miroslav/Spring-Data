package customOrm;


import customOrm.entity.Address;
import customOrm.entity.Department;
import customOrm.entity.Employee;
import ormFramework.core.EntityManager;
import ormFramework.core.EntityManagerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, URISyntaxException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        EntityManager entityManager = EntityManagerFactory.create(
                "mysql",
                "localhost",
                3306,
                "root",
                "1234",
                "custom_orm",
                Main.class
        );

        Employee byId = entityManager.findById(25, Employee.class);

        Address softUniAddress = entityManager.findById(1, Address.class);
        Address codexioAddress = entityManager.findById(2, Address.class);
        Department byId1 = entityManager.findById(30, Department.class);

        System.out.println(byId.getSalary());
        System.out.println(byId1);
        System.out.println(softUniAddress.getStreetNumber());
        System.out.println(codexioAddress.getStreet());

    }
}
