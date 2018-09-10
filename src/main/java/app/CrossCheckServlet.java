package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;

@MultipartConfig(maxFileSize = 10*1024*1024,maxRequestSize = 20*1024*1024,fileSizeThreshold = 5*1024*1024)
public class CrossCheckServlet extends HttpServlet {
	private final String REASON_REF_ID_NOT_FOUND = "Transaction reference id is not found";
	private final String REASON_REF_AMOUNT_NOT_MATCHED = "Transaction amount is not matched";
	private final String UPLOAD_PATH = "resource" + File.separator + "upload";
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Crosscheck Servlet</h1>");
        response.getWriter().println(PostgreSQLJDBC.execute(""));
        response.getWriter().println("session=" + request.getSession(true).getId());
        
	}
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String applicationPath = request.getServletContext().getRealPath("");
    	String uploadFilePath = applicationPath + "\\" + UPLOAD_PATH;    	
    	String fileName = null;
    	Set<String> files = new HashSet<>();
    	List<UnmatchedTransaction> unmatchedTransactions;
    	
        
    	// Wait for all files to be completely uploaded
    	
    	for (Part part : request.getParts()) {
			if (part != null && part.getSize() > 0) {
				fileName = part.getSubmittedFileName();
								
				files.add(uploadFilePath + "\\" + fileName);
				part.write(uploadFilePath + File.separator + fileName);
				
			}
		}
    	    	
    	unmatchedTransactions = getUnmatchedTransactions(new ArrayList<>(files)); 
    	response.getWriter().println(new JSONArray(unmatchedTransactions)); 
    }
    
    private List<UnmatchedTransaction> getUnmatchedTransactions(List<String> files) {
    	List<UnmatchedTransaction> unmatchedTransactions = new ArrayList<>();
    	
    	for (String file: files) {
    		unmatchedTransactions.addAll(crossCheck(file));
    	}
    	
    	return unmatchedTransactions;
    }
    
    private List<UnmatchedTransaction> crossCheck(String filePath) {
    	List<UnmatchedTransaction> unmatchedTransaction;
    	ResultSet queryTransactions;
    	Map<String, String> settlements = extractSettlements(filePath);
    	
    	if (settlements.size() == 0) {
    		return new ArrayList();
    	}
    	    	
    	//case 1: Reference IDs are not in our DB
    	StringBuilder query = new StringBuilder("SELECT * FROM transactions WHERE payment_order_reference IS NOT IN ('");
    	    	
    	query.append(String.join("', '"));
    	queryTransactions = PostgreSQLJDBC.execute((query.toString()));
    	unmatchedTransaction = PostgreSQLJDBC.parseResultSet(queryTransactions, REASON_REF_ID_NOT_FOUND);
    	
    	//case 2: Transaction amounts are not matched
    	query = new StringBuilder(buildCrossCheckQuery(settlements));
    	queryTransactions = PostgreSQLJDBC.execute((query.toString()));
    	unmatchedTransaction.addAll(PostgreSQLJDBC.parseResultSet(queryTransactions, REASON_REF_AMOUNT_NOT_MATCHED));
    	
    	return unmatchedTransaction;
    }
    
    private Map<String, String> extractSettlements(String filePath) {
    	Map<String, String> settlements = new HashMap<>();
    	
    	try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
	    	CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
            .withHeader("Company Account", "Merchant Account", "Psp Reference", "Merchant Reference", "Payment Method", "Creation Date", "TimeZone", "Type", "Modification Reference", "Gross Currency", "Gross Debit (GC)", "Gross Credit (GC)", "Exchange Rate", "Net Currency", "Net Debit (NC)", "Net Credit (NC)", "Commission (NC)", "Markup (NC)", "Scheme Fees (NC)", "Interchange (NC)", "Payment Method Variant", "Modification Merchant Reference", "Batch Number", "Reserved4", "Reserved5", "Reserved6", "Reserved7", "Reserved8", "Reserved9", "Reserved10")
            .withIgnoreHeaderCase()
            .withTrim());) {
    		
    		for (CSVRecord csvRecord : csvParser) {
    			settlements.put(csvRecord.get("Merchant Reference"), csvRecord.get("Gross Credit (GC)"));
    		}
    	} catch (IOException e) {
			System.out.println(e);
		}    	
    	
    	return settlements;
			
    }
    
    private String buildCrossCheckQuery(Map<String, String> settlements) {
    	if (settlements.size() == 0) {
    		return "";
    	}
    	
    	StringBuilder query = new StringBuilder("SELECT payment_order_reference FROM transactions WHERE ");    	
    	List<String> whereClauses = settlements.keySet()
    			.stream()
                .map(key -> String.format("(payment_order_reference = '%s' AND amount != %s)", key, settlements.get(key)))
                .collect(Collectors.toList());
    	
    	return query.append(String.join(" OR ", whereClauses)).toString();
    	
    }
}