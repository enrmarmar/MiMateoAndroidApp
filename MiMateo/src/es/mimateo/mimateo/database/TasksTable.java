package es.mimateo.mimateo.database;

public class TasksTable implements TasksColumns {
	public final static String TABLE_NAME = "tasks";
	public final static String[] COLS = { _ID, NAME, DESCRIPTION, DEADLINE, USER_ID, COMPLETED };
	public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + " (" 
			+ _ID + " INTEGER PRIMARY KEY, "
			+ NAME + " TEXT, " 
			+ DESCRIPTION + " TEXT, " 
			+ DEADLINE + " TEXT, "
			+ USER_ID + " INTEGER, "
			+ COMPLETED + " BOOLEAN"
			+ ");";
}
