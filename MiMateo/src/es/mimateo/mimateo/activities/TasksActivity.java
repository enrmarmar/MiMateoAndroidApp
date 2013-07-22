package es.mimateo.mimateo.activities;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import es.mimateo.mimateo.Api;
import es.mimateo.mimateo.CurrentUser;
import es.mimateo.mimateo.database.DBAdapter;
import es.mimateo.mimateo.database.DBAdapter.LocalBinder;
import es.mimateo.mimateo.R;
import es.mimateo.mimateo.Task;
import es.mimateo.mimateo.User;

public class TasksActivity extends Activity {

	Activity context;
	DBAdapter dbService;
	private LocalBinder mBinder;
	private ServiceConnection dbConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName classname, IBinder service) {
			mBinder = (LocalBinder) service;
			dbService = mBinder.getService();
			new AsyncLoadDB().execute();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasks);
		this.context = this;
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, dbConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tasks, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("DebugTag", "Menu item id : " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.action_new_task:
			Log.d("DebugTag", "Nueva tarea seleccionada");
			launchNewTaskActivity();
			return true;	
		}
		return false;
	}

	private void launchNewTaskActivity() {
		Intent intent = new Intent(getApplicationContext(),
				NewTaskActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, dbConnection, Context.BIND_AUTO_CREATE);
		new AsyncLoadDB().execute();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void launchTaskActivity(int task_id) {
		Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
		intent.putExtra("task_id", task_id);
		Log.d("DebugTag", "Launching TaskActivity : " + task_id);
		startActivity(intent);
	}

	public void loadTasks() {
		final Task[] tasks = dbService.getTasks();
		Log.d("DebugTag", "tasks.length : " + tasks.length);

		String[] from = new String[] { "name", "deadline" };
		int[] to = new int[] { R.id.text1, R.id.text2 };

		ArrayList<HashMap<String, String>> tasksData = new ArrayList<HashMap<String, String>>();
		if (tasks.length > 0) {
			for (Task task : tasks) {
				HashMap<String, String> taskData = new HashMap<String, String>();
				taskData.put(from[0], task.getName());
				if (task.getCompleted())
					taskData.put(from[1], "Completada");
				else
					taskData.put(from[1], task.getDeadlineAsWord());
				tasksData.add(taskData);
			}
			SimpleAdapter adapter = new SimpleAdapter(TasksActivity.this,
					tasksData, R.layout.row, from, to);
			ListView list = (ListView) findViewById(R.id.list);
			list.setAdapter(adapter);
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parentView, View arg1,
						int position, long arg3) {
					launchTaskActivity(tasks[position].getId());
				}
			});
		}
		else{
			Toast.makeText(context, "No hay tareas que mostrar",
					Toast.LENGTH_SHORT).show();
		}
		
	}

	public class AsyncLoadDB extends AsyncTask<Void, Void, Task[]> {

		ProgressDialog progressDialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setMessage("Cargando...");
			progressDialog.show();
		}

		@Override
		protected Task[] doInBackground(Void... params) {
			Task[] listTasks = null;
			HttpClient httpClient = new DefaultHttpClient();

			HttpGet get = new HttpGet(Api.url + "tasks" + "?token="
					+ CurrentUser.getToken() + "&email="
					+ CurrentUser.getEmail());
			get.setHeader("content-type", "application/json");

			HttpResponse resp;
			try {
				resp = httpClient.execute(get);
				String respStr = EntityUtils.toString(resp.getEntity());
				JSONObject respJSON = new JSONObject(respStr);

				JSONObject userObj = respJSON.getJSONObject("user");
				User.setId(userObj.getInt("id"));

				if (!respJSON.isNull("tasks")) {
					JSONArray tasksArray = respJSON.getJSONArray("tasks");

					int numberOfTasks = tasksArray.length();
					if (numberOfTasks > 0) {
						listTasks = new Task[numberOfTasks];
						for (int i = 0; i < numberOfTasks; i++) {
							JSONObject obj = tasksArray.getJSONObject(i);
							JSONObject taskObj = obj.getJSONObject("task");
							Task task = new Task();
							task.setId(taskObj.getInt("id"));
							task.setUserId(taskObj.getInt("user_id"));
							task.setName(new String(taskObj.getString("name")
									.getBytes("ISO-8859-1"), "UTF-8"));
							task.setDescription(new String(taskObj.getString(
									"description").getBytes("ISO-8859-1"),
									"UTF-8"));

							task.setDeadline(taskObj.getString("deadline"));
							Log.d("DebugTag", "Leyendo tarea : " + i);
							Boolean completed = (String.valueOf(taskObj
									.getString("completed")) == "true");
							task.setCompleted(completed);
							Log.d("DebugTag",
									"Completada2 : " + task.getCompleted());

							listTasks[i] = task;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "No se pudo conectar.",
						Toast.LENGTH_SHORT).show();
			}
			dbService.deleteTableTasks();
			dbService.insertTask(listTasks);
			return listTasks;
		}

		@Override
		protected void onPostExecute(Task[] result) {
			super.onPostExecute(result);
			loadTasks();
			progressDialog.dismiss();
		}
	}
}
