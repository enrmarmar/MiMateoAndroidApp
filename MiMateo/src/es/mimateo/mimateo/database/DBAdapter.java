package es.mimateo.mimateo.database;

import es.mimateo.mimateo.Task;
import es.mimateo.mimateo.User;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DBAdapter extends Service {
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public DBAdapter getService() {
			return DBAdapter.this;
		}
	}

	@Override
	public void onCreate() {
		dbHelper = new DBHelper(this);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			db = dbHelper.getReadableDatabase();
		}
	}

	@Override
	public void onDestroy() {
		db.close();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public Task[] getTasks() {
		String[] args = new String[] { String.valueOf(User.getId()) };

		Cursor cursor = db.query(TasksTable.TABLE_NAME, new String[] {
				TasksTable._ID, TasksTable.USER_ID, TasksTable.NAME,
				TasksTable.DESCRIPTION, TasksTable.COMPLETED,
				TasksTable.DEADLINE }, TasksTable.USER_ID + "=?", args, null,
				null, TasksTable._ID);

		int id_column_index = cursor.getColumnIndex(TasksColumns._ID);
		int user_id_column_index = cursor.getColumnIndex(TasksColumns.USER_ID);
		int name_column_index = cursor.getColumnIndex(TasksColumns.NAME);
		int description_column_index = cursor
				.getColumnIndex(TasksColumns.DESCRIPTION);
		int completed_column_index = cursor
				.getColumnIndex(TasksColumns.COMPLETED);
		int deadline_column_index = cursor
				.getColumnIndex(TasksColumns.DEADLINE);
		int numberOfTasks = cursor.getCount();

		Task[] tasks = new Task[numberOfTasks];
		Log.d("DebugTag", "Number of tasks: " + numberOfTasks);
		cursor.moveToFirst();

		for (int i = 0; i < numberOfTasks; i++) {
			Log.d("DebugTag", "i : " + i);
			int id = cursor.getInt(id_column_index);
			int user_id = cursor.getInt(user_id_column_index);
			String name = cursor.getString(name_column_index);
			String description = cursor.getString(description_column_index);
			String deadline = cursor.getString(deadline_column_index);
			Boolean completed = (cursor.getInt(completed_column_index) != 0);
			Task task = new Task(id, name, user_id, deadline, description,
					completed);
			tasks[i] = task;
			cursor.moveToNext();
		}

		return tasks;

	}

	public Task getTask(int id) {
		String[] args = new String[] { String.valueOf(id) };

		Cursor cursor = db.query(TasksTable.TABLE_NAME,
				new String[] { TasksTable._ID, TasksTable.NAME,
						TasksTable.DESCRIPTION, TasksTable.COMPLETED,
						TasksTable.USER_ID, TasksTable.DEADLINE },
				TasksTable._ID + "=?", args, null, null, TasksTable._ID);
		int user_id_column_index = cursor.getColumnIndex(TasksColumns.USER_ID);
		int name_column_index = cursor.getColumnIndex(TasksColumns.NAME);
		int description_column_index = cursor
				.getColumnIndex(TasksColumns.DESCRIPTION);
		int completed_column_index = cursor
				.getColumnIndex(TasksColumns.COMPLETED);
		int deadline_column_index = cursor
				.getColumnIndex(TasksColumns.DEADLINE);

		cursor.moveToFirst();
		Log.d("DebugTag", "Cursor : " + cursor.toString());

		int user_id = cursor.getInt(user_id_column_index);
		String name = cursor.getString(name_column_index);
		String description = cursor.getString(description_column_index);
		String deadline = cursor.getString(deadline_column_index);
		Boolean completed = (cursor.getInt(completed_column_index) != 0);

		return new Task(id, name, user_id, deadline, description, completed);
	}

	public boolean insertTask(Task task) {
		ContentValues newValues = new ContentValues();
		newValues.put(TasksTable._ID, task.getId());
		newValues.put(TasksColumns.USER_ID, task.getUserId());
		newValues.put(TasksColumns.NAME, task.getName());
		newValues.put(TasksColumns.DESCRIPTION, task.getDescription());
		newValues.put(TasksColumns.DEADLINE, task.getDeadline());
		newValues.put(TasksColumns.COMPLETED, task.getCompleted());
		long i = 0;
		try {
			i = db.insertWithOnConflict(TasksTable.TABLE_NAME, null, newValues,
					SQLiteDatabase.CONFLICT_REPLACE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return i > 0;
	}

	public boolean insertTask(Task[] listTasks) {
		if (listTasks != null) {
			for (Task task : listTasks) {
				Log.d("DebugTag", "Inserted task : " + task.getName());
				if (!insertTask(task))
					return false;
			}
		}
		return true;
	}
	
	public boolean deleteTask(int task_id){
		String[] args = new String[] { String.valueOf(task_id) };
		db.delete(TasksTable.TABLE_NAME, TasksTable._ID + "=?", args);
		return true;
	}
	
	public boolean deleteTableTasks(){
		db.delete(TasksTable.TABLE_NAME, null, null);
		return true;
	}
}