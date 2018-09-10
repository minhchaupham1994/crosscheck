package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLJDBC {
	static final String url = "jdbc:postgresql://localhost:5432/maas_db";
	static final String user = "postgres";
	static final String password = "1278";
    
    public static ResultSet execute(String query) {
    	ResultSet rs = null;
    	
    	
    	try {
			Connection con = DriverManager.getConnection(url, user, password);
			Statement st = con.createStatement();
			rs = st.executeQuery(query);
			
    	} catch (SQLException ex) {
    		System.out.println(ex);
		    
		}
    	return rs;
    }
    
    public static List<UnmatchedTransaction> parseResultSet(ResultSet rs, String reason) {
    	List<UnmatchedTransaction> unmatchList = new ArrayList<>();
    	
    	if (rs == null) {
    		return unmatchList;
    	} 
    	
    	try {
			while(rs.next()) {
				unmatchList.add(new UnmatchedTransaction(rs.getString(1), reason));
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
    	
    	return unmatchList;
    }
   
}