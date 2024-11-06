package org.lightvideo;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlHelp {
    private static final String TAG = "MysqlHelp";

    private static final String CLS = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://101.43.35.186:3306/light_video?characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PWD = "mysql-root-makehan312";

    // 创建表 user（如果不存在）
    public static void createUserTableIfNotExists() {
        try {
            Class.forName(CLS);
            Connection conn = DriverManager.getConnection(URL, USER, PWD);
            String sql = "CREATE TABLE IF NOT EXISTS user (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "video_title varchar(255) NOT NULL," +
                    "duration LONG NOT NULL" +
                    ")";

            Statement st = conn.createStatement();
            st.execute(sql);
            Log.d(TAG, "Table 'user' created or already exists.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception", e); // 输出异常信息到 LogCat
        }
    }

    // 写入一行信息
    public static boolean insertUserInfo(String video_title, long duration) {
        try {
            Class.forName(CLS);
            Connection conn = DriverManager.getConnection(URL, USER, PWD);
            String sql = "INSERT INTO user (video_title, duration) VALUES (?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, video_title);
            pst.setLong(2, duration);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                Log.d(TAG, "Inserted user info successfully.");
                return true;
            } else {
                Log.e(TAG, "Failed to insert user info.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception", e); // 输出异常信息到 LogCat
            return false;
        }
    }
}