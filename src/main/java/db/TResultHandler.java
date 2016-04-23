package db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vladislav on 21.04.16.
 */
public interface TResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}