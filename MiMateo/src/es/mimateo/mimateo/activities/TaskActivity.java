package es.mimateo.mimateo.activities;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import es.mimateo.mimateo.Api;
import es.mimateo.mimateo.CurrentUser;
import es.mimateo.mimateo.R;
import es.mimateo.mimateo.Task;
import es.mimateo.mimateo.database.DBAdapter;
import es.mimateo.mimateo.database.DBAdapter.LocalBinder;

public class TaskActivity extends Activity {
	private static final int REQUEST_EXIT = 0;
	static int task_id;
	static Task task;
	Activity context;

	DBAdapter dbService;
	private LocalBinder mBinder;
	private ServiceConnection dbConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName classname, IBinder service) {
			mBinder = (LocalBinder) service;
			dbService = mBinder.getService();

			Intent intent = getIntent();
			task_id = intent.getIntExtra("task_id", 0);
			Log.d("DebugTag", "Getting Task : " + task_id);
			task = dbService.getTask(task_id);

			TextView textViewTaskName = (TextView) findViewById(R.id.textViewTaskName);
			TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
			TextView textViewDeadline = (TextView) findViewById(R.id.textViewDeadline);

			textViewTaskName.setText(task.getName());
			if (!task.getDescription().equals("null"))
				textViewDescription.setText(task.getDescription());
			else
				textViewDescription.setText("");
			if (task.getCompleted()) {
				textViewDeadline.setText("Completada");
			} else {
				textViewDeadline.setText(task.getDeadlineAsWord());
			}
			
			Button buttonComplete = (Button) findViewById(R.id.buttonComplete);
			Button buttonPostpone = (Button) findViewById(R.id.buttonPostpone);
			
			if (task.getCompleted())
			{
				ViewGroup vg = (ViewGroup)(buttonComplete.getParent());
				vg.removeView(buttonComplete);
			}
			
			if (!task.isDeadlineMissed() || task.getCompleted())
			{
				ViewGroup vg = (ViewGroup)(buttonPostpone.getParent());
				vg.removeView(buttonPostpone);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task);
		this.context = this;
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, dbConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit_task:
			launchEditTaskActivity();
			return true;
		case R.id.action_delete_task:
			deleteTask();
			return true;
		}
		return false;
	}

	private void launchEditTaskActivity() {
		Intent intent = new Intent(getApplicationContext(),
				EditTaskActivity.class);
		intent.putExtra("task_id", task_id);
		startActivityForResult(intent, REQUEST_EXIT);
	}

	// Returns to TasksActivity after successfully updating a task
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EXIT) {
			if (resultCode == RESULT_OK) {
				this.finish();
			}
		}
	}

	public void deleteTask() {
		new AsyncDeleteTask().execute();
		Log.d("DebugTag", "Tarea eliminada");
		Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	public class AsyncDeleteTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setMessage("Eliminando...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();

			HttpGet get = new HttpGet(Api.url + "task/delete" + "?token="
					+ CurrentUser.getToken() + "&email="
					+ CurrentUser.getEmail() + "&id=" + task_id);
			get.setHeader("content-type", "application/json");
			try {
				HttpResponse resp = httpClient.execute(get);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "No se pudo conectar.",
						Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void completeTask(View v) {
		new AsyncCompleteTask().execute();
		Log.d("DebugTag", "Tarea completada");
		Toast.makeText(context, "Tarea marcada como completada", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	public class AsyncCompleteTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setMessage("Completando...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();

			HttpGet get = new HttpGet(Api.url + "task/complete" + "?token="
					+ CurrentUser.getToken() + "&email="
					+ CurrentUser.getEmail() + "&id=" + task_id);
			get.setHeader("content-type", "application/json");
			try {
				HttpResponse resp = httpClient.execute(get);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "No se pudo conectar.",
						Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void postponeTask(View v) {
		new AsyncPostponeTask().execute();
		Log.d("DebugTag", "Tarea pospuesta");
		Toast.makeText(context, "Tarea pospuesta hasta mañana", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	public class AsyncPostponeTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setMessage("Posponiendo...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();

			HttpGet get = new HttpGet(Api.url + "task/postpone" + "?token="
					+ CurrentUser.getToken() + "&email="
					+ CurrentUser.getEmail() + "&id=" + task_id);
			get.setHeader("content-type", "application/json");
			try {
				HttpResponse resp = httpClient.execute(get);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "No se pudo conectar.",
						Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
