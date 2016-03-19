package db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vladislav on 19.03.16.
 */
public interface ResultHandler {
    void handle(ResultSet result) throws SQLException;
}