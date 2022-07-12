package ormFramework.core;

import ormFramework.annotation.Column;
import ormFramework.annotation.Entity;
import ormFramework.annotation.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;


public class EntityManagerImpl implements EntityManager {

    private final Connection connection;

    public EntityManagerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T> T findById(int id, Class<T> type) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String tableName = type.getAnnotation(Entity.class).tableName();
        String idColumnName = Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow()
                .getName();
        PreparedStatement preparedStatement =
                this.connection.prepareStatement("SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ? ");
        preparedStatement.setInt(1, id);

        T entity = (T) type.getConstructors()[0].newInstance();

        ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()){
            return null;
        }

        for (Field field : type.getDeclaredFields()){

            if (field.isAnnotationPresent(Column.class)){
                Column columnInfo = field.getAnnotation(Column.class);
                String setterName = "set" + ((field.getName().charAt(0))+"").toUpperCase() + field.getName().substring(1);
                if (field.getType().equals(String.class)){
                    String s = resultSet.getString(columnInfo.name());
                    type.getMethod(setterName, String.class).invoke(entity, s);
                } else {
                    int s = resultSet.getInt(columnInfo.name());
                    type.getMethod(setterName, field.getType()).invoke(entity, s);
                }
            }  else if (field.isAnnotationPresent(Id.class)){
                String setterName = "set" + ((field.getName().charAt(0))+"").toUpperCase() + field.getName().substring(1);
                type.getMethod(setterName, int.class).invoke(entity, id);

            }
        }

        return entity;
    }


}
