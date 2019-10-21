package project3;

import java.io.IOException;
import java.sql.SQLException;


public class DatabaseMain {

	/**
	 * Creates and runs a DatabaseTextInterface
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) 
			throws IOException, SQLException, ClassNotFoundException
	{
		DatabaseTextInterface dbti = new DatabaseTextInterface();
		dbti.run();
	}
}
