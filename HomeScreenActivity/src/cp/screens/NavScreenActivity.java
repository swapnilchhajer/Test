package cp.screens;

import java.io.BufferedReader;     
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List; 

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cp.helpers.Polyline;
import cp.overlays.CustomItemizedOverlay;
import cp.overlays.PathOverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NavScreenActivity extends MapActivity {
	
	private AutoCompleteTextView editFrom,editTo;
	private Button goButton;
	private Button routeButton; //swapnil.c
	private Button navigateButton;
	private TextView tvgoogtime;
	private TextView tvcptime;
	static final int DATYTIME_ACTIVITY = 2; 
	static final boolean DEPARTURE = true;
	static final boolean ARRIVAL = false;
	private boolean deporarr = DEPARTURE;
	private static final int SELECTDAYTIME_ID = Menu.FIRST;
	public static final CharSequence[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	private Location myLoc;
	private GeoPoint fromPt, toPt;
	private MapView mapView;
	private List<Overlay> mapOverlays;
	private CustomItemizedOverlay itemizedoverlay;
	private Drawable drawable;
	private CustomItemizedOverlay itemizedoverlay1;
	private Drawable endmarker;
	private MapController mapController;
	private ArrayList<GeoPoint> googlePointsArr;
	private ArrayList<GeoPoint> cpPointsArr;
	private String googtime = "N/A";
	private String cptime= "N/A";
	private int mHour;
    private int mMinute;
	private int mDay;
	private GetSuggestionsTask getSuggestionsTask;
	private boolean isVisible = true;
	private LocationManager locManager;
	private LocationListener locListenerGPS;
	private LocationListener locListenerNW;
	private String serverURL="http://128.125.163.86/TDSP_Servlet/TDSPQuerySuper6?start=";
	
	private class HandleURL extends AsyncTask<URL, String, String>{ //swapnil.c
		@Override
		protected String doInBackground(URL... url) {
			// TODO Auto-generated method stub
			String encoded = "";
			try{
				String inputLine;
				//swapnil.c : start
				URLConnection conn = url[0].openConnection();
				BufferedReader inReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while((inputLine = inReader.readLine()) != null){
					encoded = encoded.concat(inputLine);
				}
				return encoded;
			}
			catch(Exception e){
				Log.d("getSug", e.getMessage());
			}
			return encoded;
		}
	}
	
	private class HandleHTTP extends AsyncTask<String, String, String>{ //swapnil.c

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			String ptListStr="";			
		    HttpClient httpclient = new DefaultHttpClient(); 
	        HttpGet request = new HttpGet(url[0]);  
	        ResponseHandler<String> handler = new BasicResponseHandler();
	        try{
	        	ptListStr = httpclient.execute(request, handler);
	        	return ptListStr;
	        }
	        catch (Exception e){
	        	e.getMessage();
	        	Log.d("arrivalexec", e.getMessage());
	        } finally {
	        	httpclient.getConnectionManager().shutdown();  
	        }
			return ptListStr;
		}
	}
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_screen);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController(); 
        mapController.animateTo(new GeoPoint(Double.valueOf(34.0522 * 1E6).intValue(),Double.valueOf(-118.2428 * 1E6).intValue()));
        mapController.setZoom(11);
       
        		
        editFrom = (AutoCompleteTextView) findViewById(R.id.editfrom);
        editTo = (AutoCompleteTextView) findViewById(R.id.editto);
        mapView.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
        		if(isVisible){
        			isVisible = false;
        			editFrom.setVisibility(View.GONE);
        		} else {
        			isVisible = true;
        			editFrom.setVisibility(View.VISIBLE);
        		}	
			}
        });
        tvgoogtime = (TextView) findViewById(R.id.tvgoogtime);
        tvcptime = (TextView) findViewById(R.id.tvcptime);
        goButton = (Button) findViewById(R.id.gobutton);
        routeButton = (Button) findViewById(R.id.routebutton); //swapnil.c
        navigateButton = (Button) findViewById(R.id.navigatebutton);
        
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		myLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);						//set My Location to last known location till it gets updated to current location
		locListenerGPS = new CustomLocListenerGPS();
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locListenerGPS);
        locListenerNW = new CustomLocListenerNW();
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locListenerNW);
        
        Calendar cal = Calendar.getInstance();
		mDay = cal.get(Calendar.DAY_OF_WEEK)-1; 
		mHour = cal.get(Calendar.HOUR_OF_DAY);
    	mMinute = cal.get(Calendar.MINUTE);
        
        //check if activity called through ToHome,ToWork, Recent 
        if(editTo.getText().toString().equals("")){
        	Bundle extras = getIntent().getExtras();
        	if(extras!=null){
        		editTo.setText(extras.getString("to"));
        		//long timelong = System.currentTimeMillis();
        		cal = Calendar.getInstance();
        		mDay = cal.get(Calendar.DAY_OF_WEEK)-1;
        		int hour = cal.get(Calendar.HOUR_OF_DAY);
        		int minute = cal.get(Calendar.MINUTE);
        		int time = getCpTime(hour, minute);
        		drawPath(time);
        	}	
        }
        
        //auto-complete code for from
        editFrom.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(editFrom);
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
        
        //auto-complete code for to
        editTo.addTextChangedListener(new TextWatcher(){
	    	public void afterTextChanged(Editable e) {
				String str = e.toString();
				try {
					if (getSuggestionsTask != null) {
						getSuggestionsTask.cancel(true);
					}

					getSuggestionsTask = new GetSuggestionsTask(editTo);
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
        
        goButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
        		hideKeyboard();
        		int time = getCpTime(mHour, mMinute);
				drawPath(time);
        	}        	
        });
        
        routeButton.setOnClickListener(new OnClickListener(){ //swapnil.c
        	public void onClick(View arg0) {
            	int time = getCpTime(mHour, mMinute);
            	if(fromPt!=null && toPt!=null){
            		//
//    			String url = "http://128.125.163.86/TDSP_Servlet/TDSPQueryListNewFibo12345turnWithUturnWithCoord?start="
//    					+ fromPt.getLatitudeE6()/1E6 + "," + fromPt.getLongitudeE6()/1E6 
//    					+ "&end=" +toPt.getLatitudeE6()/1E6 + "," + toPt.getLongitudeE6()/1E6 
//    					+ "&time=" + time+"&update=False&"
//    					+ "day=Monday";
//    					//+ "day=" + days[mDay];
//	        		String url = "http://geodb.usc.edu:8080/TDSP_Servlet/TDSPQueryListNewFibo1234turnWithUturnWithCoord?start=" 
//	    					+ fromPt.getLatitudeE6()/1E6 + "," + fromPt.getLongitudeE6()/1E6 
//	    					+ "&end=" +toPt.getLatitudeE6()/1E6 + "," + toPt.getLongitudeE6()/1E6 
//	    					+ "&time=" + time+"&update=False&" 
//	    					+ "day=" + days[mDay];
    			String url = serverURL
    					+ fromPt.getLatitudeE6()/1E6 + "," + fromPt.getLongitudeE6()/1E6 
    					+ "&end=" +toPt.getLatitudeE6()/1E6 + "," + toPt.getLongitudeE6()/1E6 
    					+ "&time=" + time+"&update=False1&"
    					+ "day=" + days[mDay];
    					//+ "day=Monday";
    					//+ "day=" + days[mDay];    			
	        		//here goes code redirects to list.java file
					Intent i = new Intent(getApplicationContext(), RouteScreenActivity.class); //swapnil.c
					i.putExtra("url", url);
					startActivity(i);
	            }
        	}        	
        });
        
        navigateButton.setOnClickListener(new OnClickListener(){ //swapnil.c
        	public void onClick(View arg0) {
            	int time = getCpTime(mHour, mMinute);
        		String fromStr = editFrom.getText().toString();
        		String toStr = editTo.getText().toString();
				if(fromPt!=null && toPt!=null)
				{ 
//    			String url = "http://128.125.163.86/TDSP_Servlet/TDSPQueryListNewFibo12345turnWithUturnWithCoord?start="
				String url = serverURL	
    					+ fromPt.getLatitudeE6()/1E6 + "," + fromPt.getLongitudeE6()/1E6 
    					+ "&end=" +toPt.getLatitudeE6()/1E6 + "," + toPt.getLongitudeE6()/1E6 
    					+ "&time=" + time+"&update=False1&"
    					+ "day=" + days[mDay];
    					//+ "day=Monday";
    					//+ "day=" + days[mDay];       		
//	        		String url = "http://geodb.usc.edu:8080/TDSP_Servlet/TDSPQueryListNewFibo1234turnWithUturnWithCoord?start=" 
//	    					+ fromPt.getLatitudeE6()/1E6 + "," + fromPt.getLongitudeE6()/1E6 
//	    					+ "&end=" +toPt.getLatitudeE6()/1E6 + "," + toPt.getLongitudeE6()/1E6 
//	    					+ "&time=" + time+"&update=False&" 
//	    					+ "day=" + days[mDay];
	
	            	//here goes code redirects to list.java file
					Intent i = new Intent(getApplicationContext(), GpsNavScreenActivity.class); //swapnil.c
					i.putExtra("url", url);
					i.putExtra("fromPtLat", fromPt.getLatitudeE6()/1E6);
					i.putExtra("fromPtLong", fromPt.getLongitudeE6()/1E6);
					i.putExtra("toPtLat", toPt.getLatitudeE6()/1E6);
					i.putExtra("toPtLong", toPt.getLongitudeE6()/1E6);
					i.putExtra("time", time);
					i.putExtra("day", days[mDay]);
					i.putExtra("fromStr", fromStr);
					i.putExtra("toStr", toStr);
					
					startActivity(i);
				}
        	}        	
        });        

    }
    
	@Override
	public void onDestroy(){
		super.onDestroy();
		locManager.removeUpdates(locListenerGPS);
		locManager.removeUpdates(locListenerNW);
	}
	
	public void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editFrom.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(editTo.getWindowToken(), 0);
	}
	
	public String getFormattedAddrFromPt(String latnlon){
		String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?address="+ latnlon +"&sensor=false&region=us";
		JSONObject json = null;
		String encoded = "";
		String resp = "";
		try{
			URL url = new URL(urlStr); //swapnil.c
			encoded = (new HandleURL().doInBackground(url));
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
	
    public void drawPath(int time){ 
    	//Check if from address is MyLocation
		String fromStr = editFrom.getText().toString();
		String toStr = editTo.getText().toString();
		if(fromStr.length() == 0 || fromStr == ""){
			buildAlertMessageNoAddr("Enter From Address.", editFrom);
			return;
		}
		if(toStr.length() == 0 || toStr == ""){
			buildAlertMessageNoAddr("Enter To Address.", editTo);
			return;
		} 
		
		if(fromStr.equalsIgnoreCase("My Location")){
			fromPt = new GeoPoint(Double.valueOf( myLoc.getLatitude() * 1E6).intValue(), Double.valueOf(myLoc.getLongitude() *1E6).intValue() );
		} else {
			fromPt = getPointFromAddr(fromStr);		//User entered from address
		}
		
		toPt = getPointFromAddr(toStr);		
		if(fromPt == null){
			buildAlertMessageNoAddr("From address invalid.", editFrom);
			return;
		}	
		if(toPt == null){
			buildAlertMessageNoAddr("To address invalid.", editTo);
			return;
		}
		
		mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
        drawable = getApplicationContext().getResources().getDrawable(R.drawable.pin);
        endmarker = getApplicationContext().getResources().getDrawable(R.drawable.marker);
        
		itemizedoverlay = new CustomItemizedOverlay(drawable, NavScreenActivity.this);
		itemizedoverlay1 = new CustomItemizedOverlay(endmarker, NavScreenActivity.this);
		String fromtemp = null;
		if(fromStr.equalsIgnoreCase("My Location")){
			fromtemp = getFormattedAddrFromPt(fromPt.getLatitudeE6()/1E6 +","+fromPt.getLongitudeE6()/1E6);
		} else {
			fromtemp = fromStr;
		}
		OverlayItem overlayfrom = new OverlayItem(fromPt, "From Address: ", fromtemp);
		OverlayItem overlayto = new OverlayItem(toPt, "To Address: ", toStr);
		itemizedoverlay.addOverlay(overlayfrom);
		itemizedoverlay1.addOverlay(overlayto);

		mapOverlays.add(itemizedoverlay);
		mapOverlays.add(itemizedoverlay1);
		
		//call google webservice
        googlePointsArr = getPointsFromGoogle(fromPt, toPt);
                   
		//add all google points to path overlay as two-point pairs
		PathOverlay googlePath = new PathOverlay(googlePointsArr, Color.BLUE);
		mapOverlays.add(googlePath);

		//call clearpath web service
		cpPointsArr = getPointsFromClearPath(fromPt, toPt,time, deporarr);
		
		
		//add all clearpath points to path overlay as two-point pairs
		PathOverlay clearPath = new PathOverlay(cpPointsArr, Color.GREEN);
		mapOverlays.add(clearPath);

		//mapController.animateTo(fromPt);
		mapController.setCenter(new GeoPoint((fromPt.getLatitudeE6()+toPt.getLatitudeE6())/2, (fromPt.getLongitudeE6()+toPt.getLongitudeE6())/2));
		//for auto zoom scaling //swapnil.c
		int latSpan = (int)Math.round(Math.abs((fromPt.getLatitudeE6()-toPt.getLatitudeE6())));
		int lonSpan = (int)Math.round(Math.abs((fromPt.getLongitudeE6()-toPt.getLongitudeE6())));
		if(latSpan>10000 && lonSpan>10000){
			latSpan += 100000;
			lonSpan += 100000;
		}
		mapController.zoomToSpan(latSpan, lonSpan);
		
		
		//add travel time for google
		tvgoogtime.setText("Google Travel Time: " + googtime);
		
		//add travel time for CP
		tvcptime.setText("ClearPath Travel Time: " + cptime + " mins");
		
		//add to and from to recents
		addToRecent(toStr);
		if(!fromStr.equalsIgnoreCase("My Location")){
			addToRecent(fromStr);
		}	
    }
    
    public void drawPath(int time, boolean deporarr){
    	//Check if from address is MyLocation
		String fromStr = editFrom.getText().toString();
		String toStr = editTo.getText().toString();
		if(fromStr.length() == 0 || fromStr == ""){
			buildAlertMessageNoAddr("Enter From Address.", editFrom);
			return;
		}
		if(toStr.length() == 0 || toStr == ""){
			buildAlertMessageNoAddr("Enter To Address.", editTo);
			return;
		} 
		
		if(fromStr.equalsIgnoreCase("My Location")){
			fromPt = new GeoPoint(Double.valueOf( myLoc.getLatitude() * 1E6).intValue(), Double.valueOf(myLoc.getLongitude() *1E6).intValue() );
		} else {
			fromPt = getPointFromAddr(fromStr);		//User entered from address
		}
		
		toPt = getPointFromAddr(toStr);		
		if(fromPt == null){
			buildAlertMessageNoAddr("From address invalid.", editFrom);
			return;
		}	
		if(toPt == null){
			buildAlertMessageNoAddr("To address invalid.", editTo);
			return;
		}
		
		mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
        drawable = getApplicationContext().getResources().getDrawable(R.drawable.pin);
        endmarker = getApplicationContext().getResources().getDrawable(R.drawable.marker);
        
		itemizedoverlay = new CustomItemizedOverlay(drawable, NavScreenActivity.this);
		itemizedoverlay1 = new CustomItemizedOverlay(endmarker, NavScreenActivity.this);
		String fromtemp = null;
		if(fromStr.equalsIgnoreCase("My Location")){
			fromtemp = getFormattedAddrFromPt(fromPt.getLatitudeE6()/1E6 +","+fromPt.getLongitudeE6()/1E6);
		} else {
			fromtemp = fromStr;
		}
		OverlayItem overlayfrom = new OverlayItem(fromPt, "From Address: ", fromtemp);
		OverlayItem overlayto = new OverlayItem(toPt, "To Address: ", toStr);
		itemizedoverlay.addOverlay(overlayfrom);
		itemizedoverlay1.addOverlay(overlayto);

		mapOverlays.add(itemizedoverlay);
		mapOverlays.add(itemizedoverlay1);
		
		if(deporarr == DEPARTURE){
			//call google webservice
			googlePointsArr = getPointsFromGoogle(fromPt, toPt);
                   
			//add all google points to path overlay as two-point pairs
			PathOverlay googlePath = new PathOverlay(googlePointsArr, Color.BLUE);
			mapOverlays.add(googlePath);
			//add travel time for google
			tvgoogtime.setText("Google Travel Time: " + googtime);
		} else if (deporarr == ARRIVAL){
			tvgoogtime.setText("");
		}

		//call clearpath web service
		cpPointsArr = getPointsFromClearPath(fromPt, toPt,time, deporarr);
		
		//add all clearpath points to path overlay as two-point pairs
		PathOverlay clearPath = new PathOverlay(cpPointsArr, Color.GREEN);
		mapOverlays.add(clearPath);
		
		if(deporarr == DEPARTURE){
			//add travel time for CP
			tvcptime.setText("CleatPath Travel Time: " + cptime + " mins");
		} else if (deporarr == ARRIVAL){
			tvcptime.setText(getNormalTime(time));
		}

		mapController.animateTo(fromPt);
		
		//add to and from to recents
		addToRecent(toStr);
		if(!fromStr.equalsIgnoreCase("My Location")){
			addToRecent(fromStr);
		}	
    }
    
    public void addToRecent(String recent){
    	SharedPreferences spRec = getSharedPreferences(RecentScreenActivity.REC_PREFS_NAME, 0);
    	SharedPreferences.Editor editor = spRec.edit();
    	editor.putLong(recent, System.currentTimeMillis());
    	editor.commit();
    }
    
    public int getCpTime(int hour, int minute){
    	int time = -1;
    	if(hour < 6)
    		return 0;
    	
    	time = ((hour-6) * 4) + (minute /15); 
    	if(time > 56)
    		time = 56;
    	return time;
    }
    
    public String getNormalTime(int slot){
    	String str = "Latest Departure Time: ";
    	int min = (slot % 4) * 15;
    	int hr = (slot / 4) + 6;
    	
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.HOUR_OF_DAY, hr);
    	c.set(Calendar.MINUTE, min);
    	c.add(Calendar.MINUTE, -1 * Integer.parseInt(cptime));
    	
    	str = str + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
    	return str;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
         menu.add(0, SELECTDAYTIME_ID, 0, R.string.menu_daytime);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {    	
        case SELECTDAYTIME_ID:
        	selectDayTime();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<GeoPoint> getPointsFromClearPath(GeoPoint fromPoint, GeoPoint toPoint, int time, boolean timeopt){
    	ArrayList<GeoPoint> ret = new ArrayList<GeoPoint>();
    	Log.d("cpDayTime", "Day:" + mDay + "  Time:" + time);
    	String url = null;
    	if(timeopt == DEPARTURE){
//    		url = "http://geodb.usc.edu:8080/TDSP_Servlet/TDSPQueryListNewFibo1234turnWithUturnWithCoord?start=" 
//    					+ fromPoint.getLatitudeE6()/1E6 + "," + fromPoint.getLongitudeE6()/1E6 
//    					+ "&end=" +toPoint.getLatitudeE6()/1E6 + "," + toPoint.getLongitudeE6()/1E6 
//    					+ "&time=" + time+"&update=False&" 
//    					+ "day=" + days[mDay];
			//url = "http://128.125.163.86/TDSP_Servlet/TDSPQueryListNewFibo12345turnWithUturnWithCoord?start="
					url = serverURL
					+ fromPoint.getLatitudeE6()/1E6 + "," + fromPoint.getLongitudeE6()/1E6 
					+ "&end=" +toPoint.getLatitudeE6()/1E6 + "," + toPoint.getLongitudeE6()/1E6 
					+ "&time=" + time+"&update=False1&"
					+ "day=" + days[mDay];
					//+ "day=Monday";
					//+ "day=" + days[mDay];
    	} else if (timeopt == ARRIVAL) {
//    		url = "http://geodb.usc.edu:8080/TDSP_Servlet_Arrival/TDSPQueryListNewFibo1234turnWithUturnWithCoord?start="
//    				+ fromPoint.getLatitudeE6()/1E6 + "," + fromPoint.getLongitudeE6()/1E6
//    				+ "&end="+toPoint.getLatitudeE6()/1E6 + "," + toPoint.getLongitudeE6()/1E6
//    				+"&time=" + time+"&update=False&" 
//    				+ "day=" + days[mDay];
			//url = "http://128.125.163.86/TDSP_Servlet_Arrival/TDSPQueryListNewFibo12345turnWithUturnWithCoord?start="
			url = serverURL
					+ fromPoint.getLatitudeE6()/1E6 + "," + fromPoint.getLongitudeE6()/1E6 
					+ "&end=" +toPoint.getLatitudeE6()/1E6 + "," + toPoint.getLongitudeE6()/1E6 
					+ "&time=" + time+"&update=False1&"
					+ "day=" + days[mDay];
					//+ "day=Monday";
					//+ "day=" + days[mDay];			
    	}    	
    	String ptListStr;
    	String ptListStrRetrieved;
    	
        try{
        	ptListStrRetrieved = (new HandleHTTP().doInBackground(url));
           
            //get lat,lon for all points from clear path
        	ptListStr = (ptListStrRetrieved.split("@"))[0];
            String[] points = ptListStr.split(";");
            Double latdob, londob;
            int length = points.length;
            for(int i = 0; i < length-1; i++){
            	latdob = Double.parseDouble((points[i].split(","))[0]);
            	londob = Double.parseDouble((points[i].split(","))[1]);
            	ret.add(new GeoPoint(Double.valueOf(latdob * 1E6).intValue(),Double.valueOf(londob * 1E6).intValue()));
            }
            String cptimestr = points[length-1].split("-")[0];
            double t = Math.round(Double.parseDouble(cptimestr));
            cptime  = String.valueOf((int) t);
         } catch (Exception e){
        	e.getMessage();
        	Log.d("arrivalexec", e.getMessage());
        }
        
    	return ret;
    }
   
    public ArrayList<GeoPoint> getPointsFromGoogle(GeoPoint fromPoint, GeoPoint toPoint){
    	ArrayList<GeoPoint> ret = new ArrayList<GeoPoint>();
    	String strUrl = "http://maps.googleapis.com/maps/api/directions/json?origin="+
    						fromPoint.getLatitudeE6()/1E6+","+fromPoint.getLongitudeE6()/1E6+"&destination="+
    						toPoint.getLatitudeE6()/1E6+","+toPoint.getLongitudeE6()/1E6+"&sensor=false";
		
		JSONObject json = null;
		try{
			//swapnil.c
			String encoded="";
			URL url = new URL(strUrl);
			encoded = (new HandleURL().doInBackground(url)); //swapnil.c			
			JSONTokener jt = new JSONTokener(encoded);
			json = new JSONObject(jt);
	
			JSONArray routesArr =(JSONArray) json.get("routes");
			JSONObject j1 = (JSONObject) routesArr.get(0);

			//get travel time
			JSONArray legsArr = (JSONArray) j1.get("legs");
			JSONObject j3 = (JSONObject) legsArr.get(0);
			googtime = ((JSONObject) j3.get("duration")).get("text").toString();
			
			//route polyline decoding
			String polyline = ((JSONObject)j1.get("overview_polyline")).get("points").toString();
			ArrayList<GeoPoint> poly = new ArrayList<GeoPoint>();
			poly = (ArrayList<GeoPoint>) Polyline.decodePolyline(polyline);
			
			ret = poly;
			return ret;
		} catch (Exception e) {
			//String exp = e.getMessage();
			Log.d("gcatch", e.getMessage());
		}
    	return ret;
    }
    
    public void buildAlertMessageNoAddr(String message, final AutoCompleteTextView edittext) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
               .setCancelable(false)
               .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       edittext.requestFocus();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    
    public GeoPoint getPointFromAddr(String strAddr){
    	GeoPoint p = null;
    	
    	Geocoder coder = new Geocoder(this);
    	List<Address> address;
    	
    	try{
    		address = coder.getFromLocationName(strAddr, 5);
    		if(address == null || address.size() == 0){
    			return null;
    		}
    		
    		Address loc = address.get(0);
    		loc.getLatitude();
    		loc.getLongitude();
    		
    		p = new GeoPoint( (int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
    	
    	} catch (IOException e){
    		Log.d("geocoding", e.getMessage());
    	}
    	return p;
    }
   
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void selectDayTime(){
		Intent i = new Intent(getApplicationContext(), DayTimeSelectActivity.class);
		startActivityForResult(i,DATYTIME_ACTIVITY);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_CANCELED){
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Departure Time:");
			dialog.setMessage(pad(mHour)+":"+ pad(mMinute) +", " +days[mDay]);
			dialog.show();
		} else {
			Bundle bund = data.getExtras();
			int time = getCpTime(bund.getInt("hr"), bund.getInt("min")); 
			mDay = bund.getInt("day");
			deporarr = bund.getBoolean("deporarrival");
			drawPath(time,deporarr);
		}
	}
	
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
	public class CustomLocListenerGPS implements LocationListener {
    	public void onLocationChanged(Location loc){
    		myLoc = new Location(loc);
    	}
    	
    	public void onProviderDisabled(String provider){
    		Toast.makeText(getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT).show();
    	}
    	
    	public void onProviderEnabled(String provider){
    		Toast.makeText(getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT);
    	}
    	
    	public void onStatusChanged(String provider, int status, Bundle extras){
    		
    	}
    }

	public class CustomLocListenerNW implements LocationListener {
    	public void onLocationChanged(Location loc){
    		myLoc = new Location(loc);
    	}
    	
    	public void onProviderDisabled(String provider){
    		Toast.makeText(getApplicationContext(), "Wi-Fi Disabled", Toast.LENGTH_SHORT).show();
    	}
    	
    	public void onProviderEnabled(String provider){
    		Toast.makeText(getApplicationContext(), "Wi-Fi Enabled", Toast.LENGTH_SHORT);
    	}
    	
    	public void onStatusChanged(String provider, int status, Bundle extras){
    		
    	}
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
				encoded = (new HandleURL().doInBackground(url)); //swapnil.c
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
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(NavScreenActivity.this, R.layout.list_item, result);
				mAutoCompleteTextView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
		}
	}

}