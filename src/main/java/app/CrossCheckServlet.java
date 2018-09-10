package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

public class CrossCheckServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Crosscheck Servlet</h1>");
        response.getWriter().println(PostgreSQLJDBC.execute(""));
        response.getWriter().println("session=" + request.getSession(true).getId());
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		String applicationPath = request.getServletContext().getRealPath("");
		String uploadFilePath = "resources/uploads";
		File uploadFolder = new File(uploadFilePath);
		
		PrintWriter writer = response.getWriter();
		
		for (Part part : request.getParts()) {
			if (part != null && part.getSize() > 0) {
				String fileName = part.getSubmittedFileName();
				String contentType = part.getContentType();	
				String query = "";
								
				part.write(uploadFilePath + File.separator + fileName);
				query = process(uploadFilePath + File.separator + fileName);
				
				ResultSet rs = PostgreSQLJDBC.execute(query);
				List<String> unmatchedIds = new ArrayList<>();
				
				try {
					while (rs.next()) {
						unmatchedIds.add(rs.getString(1));						
					}	
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		}	              
    }
    
    private String process(String file) {
    	String line = "";
		String separator = ",";
		Map refIdList = new HashMap<String, BigDecimal>();		
		String query = "SELECT PAYMENT_ORDER_REFERENCE FROM TRANSACTIONS WHERE ";
		List<String> whereClauses = new ArrayList<>();
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				
			while ((line = br.readLine()) != null) {
				String[] data = line.split(separator);
				refIdList.put(data[3], new BigDecimal(data[11]));
				whereClauses.add(String.format("(PAYMENT_ORDER_REFERENCE = '%s' AND AMOUNT != %s)", data[3], data[11]));
			}
		
		} catch (Exception e) {	
	
		} 
		
		if (whereClauses.size() > 0) {
			return query + String.join(" OR ", whereClauses);
		} else {
			return "";
		}
			
    }
}