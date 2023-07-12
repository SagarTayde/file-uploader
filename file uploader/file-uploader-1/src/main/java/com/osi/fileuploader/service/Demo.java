package com.osi.fileuploader.service;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class Demo {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public void fileUploader(MultipartFile file) throws SQLException, IOException {
	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream());
	         Connection conn = jdbcTemplate.getDataSource().getConnection()) {

	        int numberOfSheets = workbook.getNumberOfSheets();

	        for (int sheetIndex = 0; sheetIndex < numberOfSheets; sheetIndex++) {
	            Sheet sheet = workbook.getSheetAt(sheetIndex);

	            List<String> names = new ArrayList<>();
	            List<Boolean> isDateColumn = new ArrayList<>(); // Track if column is a date or not

	            for (int columnNumber = sheet.getRow(0).getFirstCellNum(); columnNumber < sheet.getRow(0)
	                    .getLastCellNum(); columnNumber++) {
	                String columnName = sheet.getRow(0).getCell(columnNumber).getStringCellValue();
	                names.add(columnName);

	                // Check if column name contains "date" (case-insensitive)
	                isDateColumn.add(columnName.toLowerCase().contains("date"));
	            }

	            String tableName = file.getOriginalFilename() + sheet.getSheetName().toString();
	            tableName = tableName.replaceAll("[^\\w\\s]", "").replace(" ", "").toLowerCase();

	            if (tableExists(tableName)) {
	                // Drop existing table
	                String dropQuery = "DROP TABLE " + tableName;
	                jdbcTemplate.execute(dropQuery);
	                System.out.println("Table " + tableName + " already exists. Dropped existing table.");
	            }

	            StringBuilder sb = new StringBuilder("CREATE TABLE " + tableName + " (");

	            for (int i = 0; i < names.size(); i++) {
	                String name = names.get(i);
	                sb.append(name.replaceAll("[^\\w\\s]", "").replace(" ", "").toLowerCase());
	                if (isDateColumn.get(i)) {
	                    sb.append(" DATE,");
	                } else {
	                    sb.append(" VARCHAR(255),");
	                }
	            }

	            String createQuery = sb.substring(0, sb.length() - 1) + ");";
	            jdbcTemplate.execute(createQuery);

	            PreparedStatement statement = conn.prepareStatement(generateInsertQuery(tableName, names));

	            for (int rowNumber = 1; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
	                for (int columnNumber = 0; columnNumber < names.size(); columnNumber++) {
	                    if (sheet.getRow(rowNumber).getCell(columnNumber).getCellType() == CellType.STRING) {
	                        statement.setString(columnNumber + 1,
	                                sheet.getRow(rowNumber).getCell(columnNumber).getStringCellValue());
	                    } else if (sheet.getRow(rowNumber).getCell(columnNumber).getCellType() == CellType.NUMERIC) {
	                        if (isDateColumn.get(columnNumber)) {
	                            Date date = sheet.getRow(rowNumber).getCell(columnNumber).getDateCellValue();
	                            if (date != null) {
	                                statement.setDate(columnNumber + 1, new java.sql.Date(date.getTime()));
	                            } else {
	                                statement.setNull(columnNumber + 1, java.sql.Types.DATE);
	                            }
	                        } else {
	                            double numericValue = sheet.getRow(rowNumber).getCell(columnNumber).getNumericCellValue();
	                            statement.setString(columnNumber + 1, String.valueOf(numericValue));
	                        }
	                    }
	                }
	                statement.addBatch();
	            }

	            statement.executeBatch();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.out.println("Failed to upload the file.");
	    }
	}

	private boolean tableExists(String tableName) {
	    String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'file_uploader' AND table_name = ?";
	    int count = jdbcTemplate.queryForObject(query, new Object[]{tableName}, Integer.class);
	    return count > 0;
	}

	private String generateInsertQuery(String tableName, List<String> columnNames) {
	    StringBuilder sb1 = new StringBuilder("INSERT INTO " + tableName + " (");
	    StringBuilder sb2 = new StringBuilder("VALUES (");

	    for (String name : columnNames) {
	        sb1.append(name.replaceAll("[^\\w\\s]", "").replace(" ", "").toLowerCase()).append(",");
	        sb2.append("?,");
	    }

	    return sb1.substring(0, sb1.length() - 1) + ") " + sb2.substring(0, sb2.length() - 1) + ");";
	}
}