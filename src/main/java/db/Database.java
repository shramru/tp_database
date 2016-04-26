package db;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vladislav on 18.03.16.
 */
public class Database {

    final BasicDataSource dataSource;

    public Database() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/db_techopark?allowMultiQueries=true");
        dataSource.setUsername("www-data");
        dataSource.setPassword("technopark");
        dataSource.setPoolPreparedStatements(true);
    }

    @Override
    @SuppressWarnings("OverlyBroadThrowsClause")
    protected void finalize() throws Throwable {
        dataSource.close();
        super.finalize();
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
                stmt.executeUpdate(update, Statement.RETURN_GENERATED_KEYS);
                int res = -1;

                try (ResultSet result = stmt.getGeneratedKeys()) {
                    if (result.next()) { res = result.getInt(1); }
                }
                return res;
            }
        }
    }

}
