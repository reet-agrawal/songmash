package edu.brown.cs.songmash.filereading;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLReader to read sqlite files.
 */
public class SQLReader {

    private Connection conn;

    /**
     * Constructor: Initializes SQLReader to read from database specified by
     * filepath.
     *
     * @param filepath
     *          filepath to sqlite file
     * @throws SQLException
     *           if some sql error were to occur
     * @throws ClassNotFoundException
     *           if class is not found
     */
    public SQLReader(String filepath)
            throws ClassNotFoundException, SQLException {
        setup(filepath);
    }

    private void setup(String filepath)
            throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String urlToDB = "jdbc:sqlite:" + filepath;
        conn = DriverManager.getConnection(urlToDB);
        Statement stat = conn.createStatement();
        stat.executeUpdate("PRAGMA foreign_keys = ON;");
        stat.close();

    }

    /**
     * @param query
     *          sql query to be sent to database
     * @param filler
     *          ? in the query that must be placed with objects in the filler
     * @return List of String corresponding to the output of the query
     * @throws SQLException
     *           when sql issues occur
     */
    public List<String> query(String query, List<Object> filler)
            throws SQLException {
        PreparedStatement prep;
        prep = conn.prepareStatement(query);

        for (int i = 1; i <= filler.size(); i++) {
            prep.setObject(i, filler.get(i - 1));
        }

        ResultSet rs = prep.executeQuery();
        List<String> toReturn = new ArrayList<>();
        while (rs.next()) {
            toReturn.add(rs.getString(1));
        }
        rs.close();
        prep.close();

        return toReturn;
    }

    /**
     * @param query
     *          sql query to be sent to database
     * @param filler
     *          ? in the query that must be placed with objects in the filler
     * @param numColumn
     *          number of columns being pulled from database
     * @return List of String corresponding to the output of the query
     * @throws SQLException
     *           when sql issues occur
     */
    public List<String> query(String query, List<Object> filler, int numColumn)
            throws SQLException {
        PreparedStatement prep;
        prep = conn.prepareStatement(query);

        for (int i = 1; i <= filler.size(); i++) {
            prep.setObject(i, filler.get(i - 1));
        }

        ResultSet rs = prep.executeQuery();
        List<String> toReturn = new ArrayList<>();
        while (rs.next()) {
            for (int i = 1; i <= numColumn; i++) {
                toReturn.add(rs.getString(i));
            }
        }
        rs.close();
        prep.close();

        return toReturn;
    }

}
