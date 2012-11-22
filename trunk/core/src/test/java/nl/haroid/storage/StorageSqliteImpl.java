package nl.haroid.storage;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public final class StorageSqliteImpl implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageSqliteImpl.class);

    private Connection connection;

    public StorageSqliteImpl() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class not found.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int update(String table, Map<String, String> values, String whereClause, String[] whereArgs) {
        LOGGER.error("update() not implemented.");
        throw new NotImplementedException();
    }

    @Override
    public List<String[]> query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        LOGGER.error("query() not implemented.");
        throw new NotImplementedException();
    }

    @Override
    public String queryUniqueResult(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        try {
            String query = buildQueryString(table, columns, selection, groupBy, having, orderBy);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            if (selectionArgs != null && selectionArgs.length > 0) {
                for (int i=0; i<selectionArgs.length; i++) {
                    LOGGER.info("Prepared statement parameter #" + i + ": " + selectionArgs[i]);
                    preparedStatement.setString((i+1), selectionArgs[i]);
                }
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.first()) {
                return resultSet.getString(1);
            }
            LOGGER.info("No resultset, returning null.");
            return null;
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing queryUniqueResult().", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public long insert(String table, String nullColumnHack, Map<String, String> values) {
        LOGGER.error("insert() not implemented.");
        throw new NotImplementedException();
    }

    @Override
    public void execSQL(String sql) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing execSQL().", e);
            throw new RuntimeException(e);
        }
    }

    void open() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:");
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing open().", e);
            throw new RuntimeException(e);
        }
    }

    void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing close().", e);
            throw new RuntimeException(e);
        }
    }

    private String buildQueryString(String table, String[] columns, String where, String groupBy, String having, String orderBy) {
        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");
        if (columns != null && columns.length != 0) {
            appendColumns(query, columns);
        } else {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(table);
        appendClause(query, " WHERE ", where);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        LOGGER.info("Query: " + query);
        return query.toString();

    }

    private void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;

        for (int i = 0; i < n; i++) {
            String column = columns[i];

            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    private void appendClause(StringBuilder s, String name, String clause) {
        if (!Utils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }
}
