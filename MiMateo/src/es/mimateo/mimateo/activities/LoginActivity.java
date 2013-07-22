package es.mimateo.mimateo.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import es.mimateo.mimateo.Api;
import es.mimateo.mimateo.CurrentUser;
import es.mimateo.mimateo.database.DBAdapter;
import es.mimateo.mimateo.database.DBAdapter.LocalBinder;
import es.mimateo.mimateo.R;
import es.mimateo.mimateo.User;

public class LoginActivity extends Activity {	
	Activity context;
	
	DBAdapter dbService;
	private LocalBinder mBinder;
	private ServiceConnection dbConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName classname, IBinder service) {
			mBinder = (LocalBinder) service;
			dbService = mBinder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		this.context = this;
		
		Intent intent = new Intent(this, DBAdapter.class);
		bindService(intent, dbConnection, Context.BIND_AUTO_CREATE);

		Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
		String email = User.getEmail(getApplicationContext());
		buttonLogin.setText(email);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void launchTasksActivity() {
		Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
		startActivity(intent);
	}

	public void login(View v) {
		String email = User.getEmail(getApplicationContext());
		CurrentUser.setEmail(email);
		CurrentUser.setToken(Api.token);
		launchTasksActivity();
	}

	public void loginAs1(View v) {
		CurrentUser.setEmail("ana@prueba.es");
		CurrentUser.setToken(Api.token);
		launchTasksActivity();
	}

	public void loginAs2(View v) {
		CurrentUser.setEmail("pedro@prueba.es");
		CurrentUser.setToken(Api.token);
		launchTasksActivity();
	}

	public void loginAs3(View v) {
		CurrentUser.setEmail("ramon@prueba.es");
		CurrentUser.setToken(Api.token);
		launchTasksActivity();
	}

	public void loginAs4(View v) {
		CurrentUser.setEmail("isabel@prueba.es");
		CurrentUser.setToken(Api.token);
		launchTasksActivity();
	}
}
