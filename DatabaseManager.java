package project3;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Class to handle database connectivity and transaction management.
 */
public class DatabaseManager {
	/**
	 * Available actions the database accepts
	 */
	public static enum CRUD {
		CREATE, READ, UPDATE, DELETE
	}

	/**
	 * Available tables the database accepts
	 */
	public static enum TABLE {

		MODULE("module"), REGISTERED("registered"), STAFF("staff"), STUDENT("student"), TEACHES("teaches");

		private final String tableName;

		/**
		 * Constructor, set the tablename value
		 * 
		 * @param tableName
		 */
		private TABLE(String tableName) {
			this.tableName = tableName;
		}

		/**
		 * Returns the table name
		 * 
		 * @return table name as string
		 */
		public String getValue() {
			return this.tableName;
		}
	}

	/**
	 * Available reports from the database
	 */
	public static enum REPORTS {
		MODULES_TAUGHT_BY, STUDENTS_MODULE, STAFF_MODULE_STUDENT, STAFF_TEACH_MULTIPLE
	}

	/**
	 * Query return package. Contains a boolean (validity) and a String (result)
	 */
	class Package {
		public boolean valid;
		public String result;
		public ArrayList<String> rs;

		/**
		 * Constructor
		 * 
		 * @param _valid  True if executed successfully
		 * @param _result Message to pass.
		 */
		public Package(boolean _valid, String _result) {
			valid = _valid;
			result = _result;
		}

		public void setRS(ArrayList<String> _rs) {
			rs = _rs;
		}
	}

	private Connection conn;
	private static String url;
	private static String username;
	private static String password;

	/**
	 * Loads database properties.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public DatabaseManager() throws IOException, ClassNotFoundException {
		InputStream stream = DatabaseManager.class.getResourceAsStream("/database.properties");
		Properties props = new Properties();
		props.load(stream);

		String driver = props.getProperty("jdbc.driver");
		url = props.getProperty("jdbc.url");
		username = props.getProperty("jdbc.username");
		if (username == null)
			username = "";
		password = props.getProperty("jdbc.password");
		if (password == null)
			password = "";
		if (driver != null)
			Class.forName(driver);
	}

	/**
	 * Public interface to interact with the database. Executes correct methods
	 * based of passed parameters.
	 * 
	 * @param type   CRUD query type
	 * @param table  TABLE table to use
	 * @param params Query parameters
	 * @return Package with results or errors
	 * @throws SQLException
	 */
	public Package databaseOperation(DatabaseManager.CRUD type, DatabaseManager.TABLE table, String[] params)
			throws SQLException {
		switch (type) {
		case CREATE:
			return create_record(table.getValue(), params);
		case READ:
			return list_records("SELECT * FROM " + table.getValue(), null);
		case UPDATE:
			return update_record(table.getValue(), params);
		case DELETE:
			return delete_record(table.getValue(), params[0]);
		default:
			return new Package(false, "Error, type unknown");
		}
	}

	/**
	 * Handles report retrieval.
	 * 
	 * @param report
	 * @param input
	 * @return
	 * @throws SQLException
	 */
	public Package databaseReport(DatabaseManager.REPORTS report, String input) throws SQLException {
		String query = "";
		switch (report) {
		case MODULES_TAUGHT_BY:
			query = "SELECT module.module_Id, module.module_name FROM teaches, module WHERE teaches.staff_Id = ? AND teaches.module_Id = module.module_Id";
			return list_records(query, input);
		case STUDENTS_MODULE:
			query = "SELECT student.student_Id, student.student_name FROM registered, student WHERE registered.module_Id = ? AND registered.student_Id = student.student_Id";
			return list_records(query, input);
		case STAFF_MODULE_STUDENT:
			query = "SELECT DISTINCT staff.staff_Id, staff.staff_name, teaches.module_Id FROM staff, teaches WHERE teaches.module_Id = (SELECT registered.module_Id FROM registered WHERE registered.student_Id = ?)";
			return list_records(query, input);
		case STAFF_TEACH_MULTIPLE:
			query = "SELECT DISTINCT staff.staff_Id, staff.staff_name FROM staff INNER JOIN teaches ON staff.staff_Id = teaches.staff_Id GROUP BY staff.staff_Id HAVING COUNT(staff.staff_name) > 1";
			return list_records(query, null);
		default:
			return new Package(false, "Error, report unknown");
		}
	}

	/**
	 * Handles deletion of a record
	 * 
	 * @param table Table name
	 * @param param Row id to delete
	 * @return Query results or errors
	 * @throws SQLException
	 */
	private Package delete_record(String table, String param) throws SQLException {
		conn = DriverManager.getConnection(url, username, password);

		if (TABLE.REGISTERED.getValue().equals(table)) {
			table = "student";
		} else if (TABLE.TEACHES.getValue().equals(table)) {
			table = "staff";
		}

		PreparedStatement pstat = conn.prepareStatement("DELETE FROM " + table + " WHERE " + table + "_id = ?");
		Package pkg;

		pstat.setString(1, param);

		try {
			pstat.execute();
			pkg = new Package(true, "Update Successful");

		} catch (SQLException e) {
			return new Package(false, e.getMessage());
		} finally {
			pstat.close();
			conn.close();
		}
		return pkg;
	}

	/**
	 * Handles update of a record
	 * 
	 * @param table  Table name
	 * @param values Id, Column and Value to use
	 * @return Query results or errors
	 * @throws SQLException
	 */
	private Package update_record(String table, String[] values) throws SQLException {
		if (values.length < 3) {
			return new Package(false, "Too Few Parameters");
		}

		String id_name = table;
		if (TABLE.REGISTERED.getValue().equals(table)) {
			id_name = "student";
		} else if (TABLE.TEACHES.getValue().equals(table)) {
			id_name = "staff";
		}

		conn = DriverManager.getConnection(url, username, password);
		PreparedStatement pstat = conn
				.prepareStatement("UPDATE " + table + " SET " + values[1] + " = ? WHERE " + id_name + "_id = ?");
		Package pkg;

		if (values[1].equals("credits")) {
			pstat.setInt(1, Integer.parseInt(values[2]));
		} else {
			pstat.setString(1, values[2]);
		}
		pstat.setString(2, values[0]);

		try {
			pstat.execute();
			pkg = new Package(true, "Update Successful");

		} catch (SQLException e) {
			return new Package(false, e.getMessage());
		} finally {
			pstat.close();
			conn.close();
		}
		return pkg;
	}

	/**
	 * Handles creation of a new record.
	 * 
	 * @param table  Table name
	 * @param values Values to use
	 * @return Query results or errors
	 * @throws SQLException
	 */
	private Package create_record(String table, String[] values) throws SQLException {
		conn = DriverManager.getConnection(url, username, password);
		PreparedStatement pstat;
		Package pkg;

		if (TABLE.REGISTERED.getValue().equals(table) || TABLE.TEACHES.getValue().equals(table)) {
			pstat = conn.prepareStatement("INSERT INTO " + table + " VALUES (?, ?)");
			for (int i = 0; i < values.length; ++i) {
				pstat.setString(i + 1, values[i]);
			}
		} else {
			pstat = conn.prepareStatement("INSERT INTO " + table + " VALUES (?, ?, ?)");
			for (int i = 0; i < values.length; ++i) {
				if (table.equals("module") && i == 2) {
					pstat.setInt(3, Integer.parseInt(values[i]));
				} else {
					pstat.setString(i + 1, values[i]);
				}
			}
		}

		try {
			pstat.execute();
			pkg = new Package(true, "Update Successful");

		} catch (SQLException e) {
			return new Package(false, e.getMessage());
		} finally {
			pstat.close();
			conn.close();
		}
		return pkg;
	}

	/**
	 * Lists all records for a table
	 * 
	 * @param table Table name
	 * @return Query results or errors
	 * @throws SQLException
	 */
	private Package list_records(String query, String param) throws SQLException {
		conn = DriverManager.getConnection(url, username, password);
		PreparedStatement pstat = conn.prepareStatement(query);
		Package pkg;

		if (param != null) {
			pstat.setString(1, param);
		}

		try {
			ResultSet rs = pstat.executeQuery();
			if (!rs.isBeforeFirst()) {
				// No Data
				pkg = new Package(false, "No Data");
			} else {
				pkg = new Package(true, "Success");

				ArrayList<String> passingThis = new ArrayList<String>();
				String cols = "";
				String sep = "";

				ResultSetMetaData md = rs.getMetaData();

				for (int i = 1; i <= md.getColumnCount(); i++) {
					cols += String.format("%" + 30 + "s", md.getColumnLabel(i));
					cols += " ";
					sep += String.format("%0" + 30 + "d", 0).replace('0', '*');
					sep += " ";
				}
				passingThis.add(cols);
				passingThis.add(sep);

				while (rs.next()) {
					String row = "";
					for (int i = 1; i <= md.getColumnCount(); i++) {
						row += String.format("%" + 30 + "s", rs.getString(i));
						row += " ";
					}
					passingThis.add(row);
				}

				pkg.setRS(passingThis);
			}

		} catch (SQLException e) {
			return new Package(false, e.getMessage());
		} finally {
			pstat.close();
			conn.close();
		}
		return pkg;
	}
}
