package cp.screens;

import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity; 
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
public class HomeScreenActivity extends Activity {
	
	private ImageButton bDrive;
	private ImageButton bFav;
	private ImageButton bHome;
	private ImageButton bWork;
	private ImageButton bRecent;
	private String home = "";
	private String work = "";
	private static final int SET_HOME = Menu.FIRST;
	private static final int SET_WORK = Menu.FIRST+1;
	public static final String PREFS_NAME = "CPPrefsFile";
	private static final String HOMEADDR = "homeAddr";
	private static final String WORKADDR = "workAddr";
	private GetSuggestionsTask getSuggestionsTask;
	private LocationManager locmanager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		
		bDrive = (ImageButton) findViewById(R.id.imButtonDrive);
		bDrive.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
				startActivity(i);
			}
		});
		
		bHome = (ImageButton) findViewById(R.id.imButtonHome);
		bHome.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(home.equals("") || home.equals("nohome")){		//home not set
					setHome();						
				} else {
					drawRoute(home);		//home location found, just fire intent
				}
				
			}
		});
		
		bWork = (ImageButton) findViewById(R.id.imButtonWork );
		bWork.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				if(work.equals("") || work.equals("nowork")){		//work not set
					setWork();						
				} else {							//work location found, just fire intent
					drawRoute(work);		
				}
			}
		});
		
		bRecent = (ImageButton) findViewById(R.id.imButtonRecent);
		bRecent.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), RecentScreenActivity.class);
				startActivity(i);
			}
		});
		
		bFav = (ImageButton) findViewById(R.id.imageButtonFav);
		bFav.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), FavListScreenActivity.class);
				startActivity(i);
			}
		});
		
		locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
		if ( !locmanager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			buildAlertMessageNoProvider("GPS");
		}
		if (!locmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			buildAlertMessageNoProvider("Network location provider");
		}
		
		SharedPreferences hwAddr = getSharedPreferences(PREFS_NAME, 0);		
		home = hwAddr.getString(HOMEADDR, "nohome");
		work = hwAddr.getString(WORKADDR, "nowork");
		
		//swapnil.c
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	      }
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, SET_HOME, 0, R.string.set_home);
        menu.add(0, SET_WORK , 0, R.string.set_work);
        return result;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case SET_HOME:
        	changeHome();
        	return true;
            
        case SET_WORK:
        	changeWork();
            return true;    
        }
    	
        return super.onOptionsItemSelected(item);
    }
	
	private void buildAlertMessageNoProvider(String provider) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(provider + " is disabled. Do you want to enable it?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, final int id) {
                       startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog,final int id) {
                        dialog.cancel();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
	
	public void setHome(){ 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Home Not Found");
		builder.setMessage("Home Is At:");

		// Set an EditText view to get user input 
		final AutoCompleteTextView input = new AutoCompleteTextView(this);
		input.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(input);
					getSuggestionsTask.execute(str);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
	    	
	    });
		builder.setView(input);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				home = input.getText().toString();
				SharedPreferences homePref = getSharedPreferences(PREFS_NAME, 0);		
				SharedPreferences.Editor editor = homePref.edit();
				editor.putString(HOMEADDR, home);
				editor.commit();
				Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
				i.putExtra("to", home);
				startActivity(i);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void setWork(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Work Not Found");
		builder.setMessage("Work Is At:");

		// Set an EditText view to get user input 
		final AutoCompleteTextView input = new AutoCompleteTextView(this);
		input.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(input);
					getSuggestionsTask.execute(str);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
	    	
	    });
		builder.setView(input);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				work = input.getText().toString();
				SharedPreferences workPref = getSharedPreferences(PREFS_NAME, 0);	
				SharedPreferences.Editor editor = workPref.edit();
				editor.putString(WORKADDR, work);
				editor.commit();
				Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
				i.putExtra("to", work);
				startActivity(i);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void changeWork(){  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Change Work Address");
		builder.setMessage("Work Is At:");

		// Set an EditText view to get user input 
		final AutoCompleteTextView input = new AutoCompleteTextView(this);
		input.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(input);
					getSuggestionsTask.execute(str);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
	    	
	    });
		builder.setView(input);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				work = input.getText().toString();
				SharedPreferences workPref = getSharedPreferences(PREFS_NAME, 0);		
				SharedPreferences.Editor editor = workPref.edit();
				editor.putString(WORKADDR, work);
				editor.commit();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public class DriveOnClickListener implements OnClickListener {
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
			startActivity(i);
		}
	}
	
	public void changeHome(){ 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Change Home Address");
		builder.setMessage("Home Is At:");

		// Set an EditText view to get user input 
		final AutoCompleteTextView input = new AutoCompleteTextView(this);
		input.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(input);
					getSuggestionsTask.execute(str);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
	    	
	    });
		builder.setView(input);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				home = input.getText().toString();
				SharedPreferences homePref = getSharedPreferences(PREFS_NAME, 0);		
				SharedPreferences.Editor editor = homePref.edit();
				editor.putString(HOMEADDR, home);
				editor.commit();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void drawRoute(String toaddr){
		Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
		i.putExtra("to", toaddr);
		startActivity(i);
	}
	
	private class GetSuggestionsTask extends AsyncTask<String, Void, String[] > { 
		String[] dummy = new String[] {};

		AutoCompleteTextView mAutoCompleteTextView;

		public GetSuggestionsTask(AutoCompleteTextView autoCompleteTextView) {
			mAutoCompleteTextView = autoCompleteTextView;
		}

		@Override
		protected String[] doInBackground(String... args) {
			String inputStr = args[0].replace(" ", "+");
			String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?address="+ inputStr 
									+",USA&sensor=false&bounds=34,-119|35-117";
			JSONObject json = null;
			String inputLine;
			String encoded = "";
			List<String> resp = new ArrayList<String>();
			try{
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				BufferedReader inReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while((inputLine = inReader.readLine()) != null){
					encoded = encoded.concat(inputLine);
				}
				JSONTokener jt = new JSONTokener(encoded);
				json = new JSONObject(jt);
				JSONArray resultArr = (JSONArray) json.get("results");
				int len = (5 < resultArr.length()) ? 5 : resultArr.length(); 
				for (int i = 0; i < len; i++) {
					resp.add(((JSONObject)resultArr.get(i)).get("formatted_address").toString());
				}
				
			} catch (Exception e){
				Log.w("getSug", e.getClass().getName());
			}
			return resp.toArray(dummy);
		}

		@Override
		protected void onPostExecute(String[] result) {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenActivity.this, R.layout.list_item, result);
				mAutoCompleteTextView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
		}
	}

	
}

	