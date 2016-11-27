package tech.slideshare.collector.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class SlideDao extends AbstractDao {

    public SlideDao(Connection con) {
        super(con);
    }

    public boolean tryEnqueue(String title, String url, Date date) throws SQLException {
        String slideSql = "INSERT INTO slide (title, url) \n" +
                "SELECT \n" +
                "  ?, ? \n" +
                "WHERE \n" +
                "NOT EXISTS ( \n" +
                "  SELECT url FROM slide WHERE url = ? \n" +
                ")";
        try (PreparedStatement pstmt = con.prepareStatement(slideSql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, url);
            pstmt.setString(3, url);

            if (pstmt.executeUpdate() == 0) {
                return false;
            }
        }

        String queueSql =
                "INSERT INTO tweet_queue (slide_id, date) VALUES (LAST_INSERT_ID(), ?)";
        try (PreparedStatement pstmt = con.prepareStatement(queueSql)) {
            pstmt.setTimestamp(1, new Timestamp(date.getTime()));

            return pstmt.executeUpdate() > 0;
        }
    }
}
