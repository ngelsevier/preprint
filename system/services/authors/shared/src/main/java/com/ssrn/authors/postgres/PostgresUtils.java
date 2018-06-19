package com.ssrn.authors.postgres;

import org.postgresql.util.PSQLState;

import java.sql.SQLException;

public class PostgresUtils {
    public static boolean databaseObjectInUseCaused(SQLException e) {
        return PSQLState.OBJECT_IN_USE.getState().equals(e.getSQLState());
    }
}
