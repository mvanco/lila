package eu.mvanco.lila;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends Activity {
	
	private Button okButton;
	private Button cancelButton;
	private EditText username;
	private EditText password;
	private EditText firstName;
	private EditText familyName;
	private DatePicker datePicker;
	private EditText birthPlace;
	
	public static String USERNAME = "username";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
		setTitle("Registration"); 
		
		okButton = (Button) findViewById(R.id.reg_ok_button);
		cancelButton = (Button) findViewById(R.id.reg_cancel_button);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.reg_password);
		firstName = (EditText) findViewById(R.id.reg_first_name);
		familyName = (EditText) findViewById(R.id.reg_family_name);
		birthPlace = (EditText) findViewById(R.id.reg_birthplace);
		
		datePicker = (DatePicker) findViewById(R.id.reg_datepicker);
		
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//String minutes = (datePicker. > 9) ? String.valueOf(min) : "0" + String.valueOf(min);
				

			    
			    new Thread(new Runnable() {
			        //Thread to stop network calls on the UI thread
			        public void run() {
			        	
						String year = String.valueOf(datePicker.getYear());
						String month = (datePicker.getMonth() + 1 > 9) ? String.valueOf(datePicker.getMonth() + 1) : "0" + String.valueOf(datePicker.getMonth() + 1);
						String day = (datePicker.getDayOfMonth() > 9) ? String.valueOf(datePicker.getDayOfMonth()) : "0" + String.valueOf(datePicker.getDayOfMonth());
						String date = year + "-" + month + "-" + day;
						
						
						
						
						
						
						
						StringBuilder url = new StringBuilder();
						url.append("http://mvanco.eu/index.php/action/sign_up");
						url.append("/" + username.getText().toString());
						url.append("/" + password.getText().toString());
						url.append("/" + firstName.getText().toString());
						url.append("/" + familyName.getText().toString());
						url.append("/" + date);
						url.append("/" + birthPlace.getText().toString());
						HttpGet getRequest = new HttpGet(url.toString());
						HttpClient httpclient = new DefaultHttpClient();
					    HttpResponse response = null;
			        	
						try {
							response = httpclient.execute(getRequest);
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                Intent intent = new Intent();
						
						if (response != null) {
							StatusLine statusLine = response.getStatusLine();
			                int sc = statusLine.getStatusCode();

			                if (statusLine.getStatusCode() == 201) {
				                intent.putExtra(USERNAME, username.getText().toString());
				                setResult(RESULT_OK, intent);
				                finish();
			                }
			                else {
			                	setResult(RESULT_CANCELED, intent);
			                	finish();
			                }
			                
						}
						else {
							setResult(RESULT_CANCELED, intent);
							finish();
						}
		                
			        }
			    }).start();
			}
			
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, new Intent());
				finish();
				
			}
			
		});
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}

}
