package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;

/**
 * Exists because SQLException in java1.4 does not allow exception wrapping.
 * 
 * @author crotwell Created on Mar 18, 2005
 */
public class WrappedSQLException extends SQLException {

    public WrappedSQLException(String message) {
        super(message);
    }
    
    public WrappedSQLException(String message, Throwable wrapped) {
        this(message);
        initCause(wrapped);
    }
    
}