package db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vladislav on 18.03.16.
 */
public class Database {

    final ComboPooledDataSource dataSource;

    @SuppressWarnings("MagicNumber")
    public Database() throws PropertyVetoException {
        dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/db_techopark?allowMultiQueries=true&useSSL=false");
        dataSource.setUser("www-data");
        dataSource.setPassword("technopark");

        dataSource.setInitialPoolSize(15);
        dataSource.setMinPoolSize(15);
        dataSource.setAcquireIncrement(5);
        dataSource.setMaxPoolSize(150);
    }

    public void execQuery(String query, ResultHandler handler) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(query);
                try (ResultSet result = stmt.getResultSet()) {
                    handler.handle(result);
                }
            }
        }
    }

    public <T> T execQuery(String query, TResultHandler<T> handler) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(query);
                try (ResultSet result = stmt.getResultSet()) {
                    return handler.handle(result);
                }
            }
        }
    }

    public int execUpdate(String update) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                int res = stmt.executeUpdate(update, Statement.RETURN_GENERATED_KEYS);

                try (ResultSet result = stmt.getGeneratedKeys()) {
                    if (result.next()) { res = result.getInt(1); }
                }
                return res;
            }
        }
    }

}
