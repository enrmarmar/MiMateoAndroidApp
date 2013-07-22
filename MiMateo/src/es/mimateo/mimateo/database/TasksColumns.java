package es.mimateo.mimateo.database;

import android.provider.BaseColumns;

public interface TasksColumns extends BaseColumns {
	public final static String NAME = "name";
	public final static String DESCRIPTION = "description";
	public final static String DEADLINE = "deadline";
	public final static String USER_ID = "user_id";
	public final static String COMPLETED = "completed";
}
