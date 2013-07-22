package es.mimateo.mimateo.activities;

import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import es.mimateo.mimateo.Api;
import es.mimateo.mimateo.CurrentUser;
import es.mimateo.mimateo.database.DBAdapter;
import es.mimateo.mimateo.database.DBAdapter.LocalBinder;
import es.mimateo.mimateo.R;
import es.mimateo.mimateo.Task;

public class EditTaskActivity extends Activity {
	static int task_id;
	static Task task;
	Activity context;
	private DatePicker datepickerDeadline;
	private int year;
	private int month;
	private int day;
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
			setCurrentDateOnView();
			EditText editTextName = (EditText) findViewById(R.id.editTextName);
			EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);
			editTextName.setText(task.getName());
			editTextDescription.setText(task.getDescription());
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);
		this.context = this;
		datepickerDeadline = (DatePicker) findViewById(R.id.datepickerDeadline);
		datepickerDeadline.setCalendarViewShown(false);
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, dbConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_task, menu);
		return true;
	}

	public void setCurrentDateOnView() {
		datepickerDeadline = (DatePicker) findViewById(R.id.datepickerDeadline);
		datepickerDeadline.setCalendarViewShown(false);
		Calendar c = Calendar.getInstance();
		year = task.getDeadlineYear();
		month = task.getDeadlineMonth(); // Months start at 0
		day = task.getDeadlineDay();

		datepickerDeadline.init(year, month, day, null);
		datepickerDeadline.setMinDate(c.getTimeInMillis() - 1000);
	}

	public void SaveTask(View v) {
		EditText editTextName = (EditText) findViewById(R.id.editTextName);
		EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);

		task.setName(editTextName.getText().toString());
		task.setDescription(editTextDescription.getText().toString());

		datepickerDeadline = (DatePicker) findViewById(R.id.datepickerDeadline);
		year = datepickerDeadline.getYear();
		month = datepickerDeadline.getMonth();
		day = datepickerDeadline.getDayOfMonth();
		Log.d("DebugTag", "year : " + year + ", month: " + month + ", day: "
				+ day);
		String formattedDate = Integer.toString(year) + "-"
				+ Integer.toString(month) + "-" + Integer.toString(day);
		Log.d("DebugTag", "Formatted Date : " + formattedDate);
		task.setDeadline(formattedDate);

		if (task.getName().equals("")) {
			Toast.makeText(getApplicationContext(),
					"El nombre de la tarea es obligatorio", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		new AsyncUpdateTask().execute();
	}

	public class AsyncUpdateTask extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Void... params) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost post = new HttpPost(Api.url + "task/update");
				post.setHeader("content-type", "application/json");

				JSONObject data = new JSONObject();
				data.put("email", CurrentUser.getEmail());
				data.put("token", Api.token);
				JSONObject JSONTask = new JSONObject();

				JSONTask.put("id", task.getId());
				JSONTask.put("name", task.getName());
				JSONTask.put("description", task.getDescription());
				JSONTask.put("deadline", task.getDeadline());
				data.put("task", JSONTask);

				StringEntity entity;
				entity = new StringEntity(data.toString(), "UTF-8");

				Log.d("DebugTag", "JSON : " + data);
				post.setEntity(entity);

				HttpResponse resp = httpClient.execute(post);
				String respStr = EntityUtils.toString(resp.getEntity());
				JSONObject respJSON = new JSONObject(respStr);
				return respJSON;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject respJSON) {
			super.onPostExecute(respJSON);
			try {
				if (respJSON.isNull("status")) {
					Toast.makeText(context, "Error de conexión",
							Toast.LENGTH_SHORT).show();
				} else {
					JSONObject statusObj = respJSON.getJSONObject("status");
					if (statusObj.isNull("error")) {
						String info_message = statusObj.getString("info");
						Toast.makeText(context, info_message,
								Toast.LENGTH_SHORT).show();
						setResult(RESULT_OK, null);
						context.finish();

					} else {
						String error_message = statusObj.getString("error");
						if (!statusObj.isNull("task")) {
							JSONObject taskObj = respJSON.getJSONObject("task");
							JSONObject errorM = taskObj.getJSONObject("error");
							Log.d("DebugTag", "Error" + errorM);
							Toast.makeText(context, errorM.toString(),
									Toast.LENGTH_SHORT).show();
						}
						Toast.makeText(context, error_message,
								Toast.LENGTH_SHORT).show();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
