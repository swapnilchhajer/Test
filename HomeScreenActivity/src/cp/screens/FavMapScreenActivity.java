package cp.screens;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cp.overlays.FavItemizedOverlay;

public class FavMapScreenActivity extends MapActivity{
	
	private AutoCompleteTextView favedit;
	private Button addbutt;
	private String name = "";
	private String favAddr = "";
	private MapView mapView;
	private List<Address> address;
	private MapController mapController;
	private GetSuggestionsTask getSuggestionsTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fav_map);
		
		mapView =(MapView) findViewById(R.id.favmapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController(); 
		
		addbutt = (Button) findViewById(R.id.favaddbutton);
		
		favedit = (AutoCompleteTextView) findViewById(R.id.favaddedit);
		favedit.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(favedit);
					getSuggestionsTask.execute(str);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {
			}
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
			}
	    });
		
		addbutt.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				String str = favedit.getText().toString();
				try {
					address = new Geocoder(getApplicationContext()).getFromLocationName(str, 2);
					favAddr = getFormattedAddrFromPt(address.get(0).getLatitude(),address.get(0).getLongitude());
					AlertDialog.Builder builder = new AlertDialog.Builder(FavMapScreenActivity.this);

					Log.d("favaddr", favAddr);
					
					builder.setTitle("New Favorite");
					builder.setMessage("Name this Place:");

					// Set an EditText view to get user input 
					final EditText input = new EditText(getApplicationContext());
					builder.setView(input);

					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							name = input.getText().toString();
							SharedPreferences favPref = getSharedPreferences(FavListScreenActivity.FAV_PREFS_NAME, 0);		
							SharedPreferences.Editor editor = favPref.edit();
							editor.putString(name, favAddr);
							editor.commit();
							addToMap(name, favAddr);
						}
					});

					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					    // Canceled.
					  }
					});
					
					AlertDialog alert = builder.create();
					alert.show();
					
				} catch (Exception e) {
					Log.d("favmap", e.getMessage());
				}
 			}
		});
	}
	
	public void addToMap(String nametemp, String favAddrtemp){
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		Drawable drawable = this.getResources().getDrawable(R.drawable.pin);
		FavItemizedOverlay itemizedoverlay = new FavItemizedOverlay(drawable, FavMapScreenActivity.this);
		
		GeoPoint point = new GeoPoint((int)(address.get(0).getLatitude() * 1E6),(int)(address.get(0).getLongitude() * 1E6));
		OverlayItem overlayitem = new OverlayItem(point, nametemp, favAddrtemp);
		
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		mapController.animateTo(point);
	}
	
	public String getFormattedAddrFromPt(double latdob, double londob){
		String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?address="+ latdob +","+ londob +"&sensor=false&region=us";
		JSONObject json = null;
		String inputLine;
		String encoded = "";
		String resp = "";
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
			
			resp = (((JSONObject)resultArr.get(0)).get("formatted_address").toString());
			
			return resp;
			
		} catch (Exception e){
			Log.d("getSug", e.getMessage());
		}
		return resp;
	}
	
	@Override
	protected boolean isRouteDisplayed(){
		return false;
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
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(FavMapScreenActivity.this, R.layout.list_item, result);
				mAutoCompleteTextView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
		}
	}
}
