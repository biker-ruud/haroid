package nl.haroid.storage;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            PreparedStatement preparedStatement = buildUpdateStatement(table, values, whereClause, whereArgs);
            int affectedRecords = preparedStatement.executeUpdate();
            preparedStatement.close();
            return affectedRecords;
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing update().", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String[]> query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        LOGGER.error("query() not implemented.");
        throw new IllegalArgumentException("query() Not implemented");
    }

    @Override
    public String queryUniqueResult(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        try {
            String query = buildSelectQueryString(table, columns, selection, groupBy, having, orderBy);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            bindArgs(preparedStatement, selectionArgs);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
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
        try {
            PreparedStatement preparedStatement = buildInsertStatement(table, nullColumnHack, values);
            int affectedRecords = preparedStatement.executeUpdate();
            preparedStatement.close();
            return affectedRecords;
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing insert().", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execSQL(String sql) {
        try {
            LOGGER.info("execSQL(): " + sql);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing execSQL().", e);
            throw new RuntimeException(e);
        }
    }

    void open() {
        if (connection == null) {
            try {
                LOGGER.info("Opening database...");
                connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            } catch (SQLException e) {
                LOGGER.error("sql exception while performing open().", e);
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.info("Database already open.");
        }
    }

    void close() {
        LOGGER.info("Not closing database, since that will throw away all relevant unit test data.");
//        try {
//            LOGGER.info("Closing database...");
//            connection.close();
//            connection=null;
//        } catch (SQLException e) {
//            LOGGER.error("sql exception while performing close().", e);
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Gets the database version.
     *
     * @return the database version
     */
    int getVersion() {
        int currentVersion = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("PRAGMA user_version;");
            if (resultSet.next()) {
                currentVersion = resultSet.getInt(1);
                LOGGER.info("Database current version: " + currentVersion);
            } else {
                LOGGER.info("Database has no version.");
            }
            resultSet.close();
            statement.close();
            return currentVersion;
        } catch (SQLException e) {
            LOGGER.error("sql exception while performing getVersion().", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the database version.
     *
     * @param version the new database version
     */
    void setVersion(int version) {
        execSQL("PRAGMA user_version = " + version);
    }

    private void bindArgs(PreparedStatement preparedStatement, String[] bindArgs) throws SQLException {
        if (bindArgs != null && bindArgs.length > 0) {
            for (int i=0; i<bindArgs.length; i++) {
                LOGGER.info("Prepared statement parameter #" + i + ": " + bindArgs[i]);
                preparedStatement.setString((i+1), bindArgs[i]);
            }
        }
    }

    private PreparedStatement buildInsertStatement(String table, String nullColumnHack, Map<String, String> values) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(" INTO ");
        sql.append(table);
        sql.append('(');

        String[] bindArgs = null;
        int size = (values != null && values.size() > 0)
                ? values.size() : 0;
        if (size > 0) {
            bindArgs = new String[size];
            int i = 0;
            for (Map.Entry<String, String> valueEntry : values.entrySet()) {
                sql.append((i > 0) ? "," : "");
                sql.append(valueEntry.getKey());
                bindArgs[i++] = valueEntry.getValue();
            }
            sql.append(')');
            sql.append(" VALUES (");
            for (i = 0; i < size; i++) {
                sql.append((i > 0) ? ",?" : "?");
            }
        } else {
            sql.append(nullColumnHack + ") VALUES (NULL");
        }
        sql.append(')');

        LOGGER.info("Query: " + sql.toString());
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        bindArgs(preparedStatement, bindArgs);
        return preparedStatement;
    }

    private PreparedStatement buildUpdateStatement(String table, Map<String, String> values, String whereClause, String[] whereArgs) throws SQLException {
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(table);
        sql.append(" SET ");

        // move all bind args to one array
        int setValuesSize = values.size();
        int bindArgsSize = (whereArgs == null) ? setValuesSize : (setValuesSize + whereArgs.length);
        String[] bindArgs = new String[bindArgsSize];
        int i = 0;
        for (Map.Entry<String, String> valueEntry : values.entrySet()) {
            sql.append((i > 0) ? "," : "");
            sql.append(valueEntry.getKey());
            bindArgs[i++] = valueEntry.getValue();
            sql.append("=?");
        }
        if (whereArgs != null) {
            for (i = setValuesSize; i < bindArgsSize; i++) {
                bindArgs[i] = whereArgs[i - setValuesSize];
            }
        }
        if (!Utils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }

        LOGGER.info("Query: " + sql.toString());
        PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        bindArgs(preparedStatement, bindArgs);
        return preparedStatement;
    }

    private String buildSelectQueryString(String table, String[] columns, String where, String groupBy, String having, String orderBy) {
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
