package project3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import project3.DatabaseManager.CRUD;
import project3.DatabaseManager.REPORTS;
import project3.DatabaseManager.TABLE;

public class DatabaseTextInterface {
	private Scanner in;
	private int input_int;
	private boolean running;
	private DatabaseManager db;

	/**
	 * Constructor, initialises variables.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public DatabaseTextInterface() throws IOException, ClassNotFoundException {
		in = new Scanner(System.in);
		input_int = 0;
		running = true;
		db = new DatabaseManager();
	}

	/**
	 * Runs the program in a loop until user chooses to exit.
	 * 
	 * @throws SQLException
	 */
	public void run() throws SQLException {
		while (running) {
			displayMainMenu();
			getIntInput(":> ");
			processMainMenuInput();
		}
	}

	/**
	 * Handles the queries to and from the database via the DatabaseManager
	 * 
	 * @param type   CRUD query type
	 * @param table  Table to use
	 * @param params Parameters to pass
	 * @throws SQLException
	 */
	private void queryDatabaseManager(DatabaseManager.CRUD type, DatabaseManager.TABLE table, String[] params)
			throws SQLException {
		DatabaseManager.Package results = db.databaseOperation(type, table, params);
		if (!results.valid) {
			System.out.println(results.result);
			System.out.println("Please try again");
		} else {
			if (type == CRUD.READ) {
				for (String str : results.rs) {
					System.out.println(str);
				}
			}
		}
	}

	private void queryDatabaseReports(DatabaseManager.REPORTS report, String input) throws SQLException {
		DatabaseManager.Package results = db.databaseReport(report, input);
		if (!results.valid) {
			System.out.println(results.result);
			System.out.println("Please try again");
		} else {
			for (String str : results.rs) {
				System.out.println(str);
			}
		}
	}

	private void processMainMenuInput() throws SQLException {
		switch (input_int) {
		case 1:
			displaySubMenu("Student");
			processSubMenuInput(TABLE.STUDENT);
			break;
		case 2:
			displaySubMenu("Module");
			processSubMenuInput(TABLE.MODULE);
			break;
		case 3:
			displaySubMenu("Staff");
			processSubMenuInput(TABLE.STAFF);
			break;
		case 4:
			displaySubMenu("Registration");
			processSubMenuInput(TABLE.REGISTERED);
			break;
		case 5:
			displaySubMenu("Teaches");
			processSubMenuInput(TABLE.TEACHES);
			break;
		case 6:
			displayReportMenu();
			processReportMenuInput();
			break;
		case 0:
			System.out.print("Quitting, goodbye.");
			running = false;
			break;
		default:
			System.out.print("Input not recognised, try again.\n");
			break;
		}
	}

	private void processSubMenuInput(DatabaseManager.TABLE table) throws SQLException {
		boolean inSubMenu = true;

		while (inSubMenu) {
			getIntInput(":> ");
			switch (input_int) {
			case 1:
				// List
				String[] list_params = { "none" };
				queryDatabaseManager(CRUD.READ, table, list_params);
				break;
			case 2:
				// Add
				queryDatabaseManager(CRUD.CREATE, table, getAddParameters(table));
				break;
			case 3:
				// Delete
				String the_id_del = getTextInput("Enter ID :> ");
				String[] del_params = { the_id_del };
				queryDatabaseManager(CRUD.DELETE, table, del_params);
				break;
			case 4:
				// Update
				if (table == TABLE.REGISTERED || table == TABLE.TEACHES) {
					System.out.print("Input not recognised, try again.\n");
					break;
				}
				String the_id_upd = getTextInput("Enter ID :> ");
				String the_col = getTextInput("Enter column :> ");
				String the_val = getTextInput("Enter value :>");
				String[] upd_params = { the_id_upd, the_col, the_val };
				queryDatabaseManager(CRUD.UPDATE, table, upd_params);
				break;
			case 0:
				// Quit
				inSubMenu = false;
				System.out.println();
				break;
			default:
				System.out.print("Input not recognised, try again.\n");
				break;
			}
		}

	}

	/**
	 * Process input from report menu
	 * 
	 * @throws SQLException
	 */
	private void processReportMenuInput() throws SQLException {
		boolean inReportMenu = true;

		while (inReportMenu) {
			getIntInput(":> ");
			String report_input;
			switch (input_int) {
			case 1:
				report_input = getTextInput("Enter staff id :> ");
				queryDatabaseReports(REPORTS.MODULES_TAUGHT_BY, report_input);
				break;
			case 2:
				report_input = getTextInput("Enter module id :> ");
				queryDatabaseReports(REPORTS.STUDENTS_MODULE, report_input);
				break;
			case 3:
				report_input = getTextInput("Enter student id :> ");
				queryDatabaseReports(REPORTS.STAFF_MODULE_STUDENT, report_input);
				break;
			case 4:
				queryDatabaseReports(REPORTS.STAFF_TEACH_MULTIPLE, null);
				break;
			case 0:
				// Quit
				inReportMenu = false;
				System.out.println();
				break;
			default:
				System.out.print("Input not recognised, try again.\n");
				break;
			}
		}

	}

	/**
	 * Requests user input in the form of a string
	 * 
	 * @param message Message to prompt user with
	 * @return String entered
	 */
	private String getTextInput(String message) {
		String input_str = "";
		System.out.print(message);
		input_str = in.nextLine();
		return input_str;
	}

	/**
	 * Requests user input in the form of an integer
	 * 
	 * @param message Message to prompt user with
	 * @return Integer entered
	 */
	private int getIntInput(String message) {
		input_int = -1;
		System.out.print(message);
		input_int = in.nextInt();
		in.nextLine();
		return input_int;
	}

	/**
	 * Displays the main menu
	 */
	private void displayMainMenu() {
		System.out.println("Main Menu\n" + "***************************\n" + "1. Students\n" + "2. Modules\n"
				+ "3. Staff\n" + "4. Registrations\n" + "5. Teaches\n" + "6: Reports\n" + "0. Quit\n");
	}

	/**
	 * Displays the sub menu list.
	 * 
	 * @param name Name to display in items.
	 */
	private void displaySubMenu(String name) {
		if (name.equals("Registration") || name.equals("Teaches")) {
			System.out.println("\n" + name + " Menu\n" + "***************************\n" + "1. List " + name + "s\n"
					+ "2. Add " + name + "\n" + "3. Remove " + name + "\n" + "0. Back\n");
		} else {
			System.out.println("\n" + name + " Menu\n" + "***************************\n" + "1. List " + name + "s\n"
					+ "2. Add " + name + "\n" + "3. Remove " + name + "\n" + "4. Update " + name + "\n" + "0. Back\n");
		}
	}

	/**
	 * Displays the reports sub menu
	 */
	public void displayReportMenu() {
		System.out.println("\nReport Menu\n" + "***************************\n" + "1. Modules taught by\n"
				+ "2. Students registered on\n" + "3. Staff who teach student\n" + "4. Staff who teach more than\n"
				+ "0. Back\n");
	}

	/**
	 * Handles getting the parameters for the add SQL function.
	 * 
	 * @param table Table to use
	 * @return String array of parameters
	 */
	private String[] getAddParameters(DatabaseManager.TABLE table) {
		if (table == TABLE.REGISTERED) {
			String aname = "student";
			String bname = "module";

			String a = getTextInput("Enter " + aname + " ID :> ");
			String b = getTextInput("Enter " + bname + " ID :> ");

			String[] add_params = { a, b };
			return add_params;
		} else if (table == TABLE.TEACHES) {
			String aname = "staff";
			String bname = "module";

			String a = getTextInput("Enter " + aname + " ID :> ");
			String b = getTextInput("Enter " + bname + " ID :> ");

			String[] add_params = { a, b };
			return add_params;
		} else {
			String qname = ((table == TABLE.STUDENT) ? "degree scheme" : (table == TABLE.MODULE) ? "credits" : "grade");
			String id = getTextInput("Enter ID :> ");
			String name = getTextInput("Enter name :> ");
			String other = getTextInput("Enter " + qname + " :> ");
			String[] add_params = { id, name, other };
			return add_params;
		}
	}
}
