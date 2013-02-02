package cp.screens;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import cp.overlays.CustomItemizedOverlay;
import cp.overlays.PathOverlay;

public class GpsNavScreenActivity extends MapActivity {

	private MapView mapView;
	private MapController mapController;
	private Location interLocation1, interLocation2, interLocationUpdate1, interLocationUpdate2, streetLocation1, streetLocation2;
	private double fromPtLat, fromPtLong, toPtLat, toPtLong, arrowPtLat, arrowPtLong; 
	private int time;
	private String url;
	private GeoPoint fromGeoPt;
	private GeoPoint toGeoPt;
	private String toStr;
	private GeoPoint arrowGeoPt, arrowGeoPtNext, currentGeoPt, turnGeoPt, nextTurnGeoPt;
	private GeoPoint arrowGeoPtOld;
	private List<Overlay> mapOverlays;
	private Drawable arrow;
	private Bitmap original; //making a copy of arrow to original for rotation: swapnil.c : 10272012
	private CustomItemizedOverlay itemizedoverlay2;
	private ArrayList<GeoPoint> cpPointsArr, cpPointsArrModified; //swapnil.c
	private int countTurnsCrossed;
	private TextView tvstreetname;
	private TextView tvmiles;
	private ImageView nextTurnImage;
	private String fromStr;
	private CharSequence day;
	private ArrayList<GeoPoint> googlePointsArrStart, googlePointsArrEnd;
	private String cptime; //swapnil.c : clearpath travel time
	private int firstLocationFix=0;
	
	private Projection projection; //swapnil.c
	private Bitmap arrowBitmap;
	
	String ptListStr, timeStr;
	String streetListStr;
	String directionInfo[];
	String direction, nextTurnDirection, nextStreetName, nextMiles;
	private GeoPoint nextGeoPt;
	double nextLatitude, nextLongitude;
	String latitudeTurn, longitudeTurn;
	Double longitude, latitude;
	GeoPoint turnPt;
	Drawable markerDirection;
	CustomItemizedOverlay[] itemizedoverlayDirection = new CustomItemizedOverlay[20];
	OverlayItem[] overlayDirection = new OverlayItem[20];
	int j=0; //noTurns
	double slope=0.0f;
	
	//turn related data
	int startIndex = 0;
	int turnIndex = 0, nextTurnIndex = 0;
	int isAtTurn = 0;
	int startIndexStreet = 0;
	int isNextStreet = 0;
	
	float distanceBetweenPointsMtr=0;
	
	
	private class HandleHTTP extends AsyncTask<String, String, String> { //swapnil.c
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
	
	private class mylocationlistener implements LocationListener{ //swapnil.c

		private NavScreenActivity nsa;
		public mylocationlistener(){
			nsa = new NavScreenActivity();
			interLocationUpdate1 = new Location("interLocationUpdate1");
			interLocationUpdate2 = new Location("interLocationUpdate2");
			//arrowGeoPt = new GeoPoint(Double.valueOf(arrowPtLat * 1E6).intValue(),Double.valueOf(arrowPtLong * 1E6).intValue());
			//cpPointsArr = nsa.getPointsFromClearPath(fromGeoPt, toGeoPt, time, true);
            //arrowGeoPt = cpPointsArr.get(0);
            //arrowGeoPtNext = cpPointsArr.get(1); //swapnil.c
		}
		
	public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
	        if (location != null) {
	        	arrowPtLat = location.getLatitude();
	        	arrowPtLong = location.getLongitude();
	        	if(firstLocationFix>100)
	        		firstLocationFix=5; //not first location fix its 2nd one or after that only
	        	firstLocationFix++;
	        		        	
	            Log.d("LOCATION CHANGED", location.getLatitude() + "");
	            Log.d("LOCATION CHANGED", location.getLongitude() + "");
	            /*Toast.makeText(getApplicationContext(),
	                location.getLatitude() + "" + location.getLongitude(),
	                Toast.LENGTH_LONG).show();*/
	            

//				Point startPoint1 = new Point();
//				Point endPoint1 = new Point();
	            
	            //swapnil.c
	            arrowGeoPt = new GeoPoint(Double.valueOf(arrowPtLat * 1E6).intValue(),Double.valueOf(arrowPtLong * 1E6).intValue());
	            currentGeoPt = new GeoPoint(Double.valueOf(arrowPtLat * 1E6).intValue(),Double.valueOf(arrowPtLong * 1E6).intValue());
//	        	if(arrowGeoPt!=null) //swapnil.c: this shd work
//	        	{
//	        		//swapnil.c:testing
//	        		//mapController.animateTo(arrowGeoPt); //swapnil.c : 10272012
//	        	}
	        	
	    		if(arrowGeoPt!=null){
	    				    			
//	    			OverlayItem overlaycurrent = new OverlayItem(arrowGeoPt, "Current: ", "curloc");
//    				itemizedoverlay2.emptyOverlays();
    				//itemizedoverlay2.addOverlay(overlaycurrent);
    				//mapOverlays.add(itemizedoverlay2);
    				
    				
    				//call cp service
	    			if(firstLocationFix==1){
	    				cpPointsArr = nsa.getPointsFromClearPath(arrowGeoPt, toGeoPt, time, true);
	    			}
	    			arrowGeoPt = cpPointsArr.get(0);
	    			arrowGeoPtNext = cpPointsArr.get(1);         
		            
		            
//		            OverlayItem overlaycurrent2 = new OverlayItem(arrowGeoPt, "Current: ", "curloc");
//		            if(mapOverlays.indexOf(itemizedoverlay2)>0)
//		            	mapOverlays.remove(mapOverlays.indexOf(itemizedoverlay2));
	    			
//	    			if(arrowGeoPt!=null && arrowGeoPtNext!=null){
//						projection.toPixels(arrowGeoPt, startPoint1);
//						projection.toPixels(arrowGeoPtNext, endPoint1);
//						if(endPoint1.x == startPoint1.x){
//							slope = 0;
//						}
//						else{
//							slope = Math.atan(((double)(endPoint1.y-startPoint1.y)*1.0)/((double)(endPoint1.x-startPoint1.x)*1.0));
//							slope = 90+ slope*180.0/Math.PI;
//						}
//						rotateItem((float)slope);
//	    			}
	    			
//    				itemizedoverlay2.addOverlay(overlaycurrent2);
//    				mapOverlays.add(itemizedoverlay2);
    				mapView.invalidate();
    				mapController.animateTo(currentGeoPt);
	    		}	
	        }
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stubs
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	class MyOverlay extends Overlay{
		private GeoPoint gpback, gpforward, gpcenter;
		
		public MyOverlay(){	
			BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inPurgeable = true;
			arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.arrow1, options2);
		}
		public void draw(Canvas canvas, MapView mapv, boolean shadow){
				 super.draw(canvas, mapv, shadow);
				 
				 projection = mapView.getProjection();
				 
				 Point startPoint = new Point();
				 Point endPoint = new Point();
				 Point currentPoint = new Point();
				 float radius=0;
				 
					streetListStr = ptListStr.split("@")[1];
					directionInfo = streetListStr.split(",");
					int length = directionInfo.length;
					
					//tvmiles.setText("slope:"+slope);
					//rotateItem((float)slope);

					if(currentGeoPt != null) {
						projection.toPixels(currentGeoPt, currentPoint);					
						canvas.drawBitmap(arrowBitmap, currentPoint.x-40, currentPoint.y-40 , null);
						
						/*+ swapnil.c : code for finding nextTurnDistance +*/
						longitudeTurn = directionInfo[startIndexStreet+2].toString();
						latitudeTurn = directionInfo[startIndexStreet+3].toString();
						longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
						latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
						turnGeoPt =  new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
						
						longitudeTurn = directionInfo[startIndexStreet+5+2].toString();
						latitudeTurn = directionInfo[startIndexStreet+5+3].toString();
						longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
						latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
						nextTurnGeoPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
						
						turnIndex = cpPointsArr.indexOf(turnGeoPt);
						nextTurnIndex = cpPointsArr.indexOf(nextTurnGeoPt);
		    			
						startIndex = turnIndex;
						if(turnIndex != -1)
							arrowGeoPt = cpPointsArr.get(turnIndex);
						if(nextTurnIndex != -1)
							arrowGeoPtNext = cpPointsArr.get(nextTurnIndex); //swapnil.c
		    			
			            if(turnIndex!=-1){
							interLocationUpdate1.setLatitude((double)(currentGeoPt.getLatitudeE6())*1E-6);
							interLocationUpdate1.setLongitude((double)(currentGeoPt.getLongitudeE6())*1E-6);
				            
							interLocationUpdate2.setLatitude((double)(turnGeoPt.getLatitudeE6())*1E-6);
							interLocationUpdate2.setLongitude((double)(turnGeoPt.getLongitudeE6())*1E-6);
		
							//distance to next turn in mtr
							distanceBetweenPointsMtr = (interLocationUpdate1.distanceTo(interLocationUpdate2));
							
							if(distanceBetweenPointsMtr < 30.0f && isAtTurn == 0 && nextTurnIndex!=-1){
								//no need to call again the cpPointsArr, instead increment pointer by one
			    				//cpPointsArr = nsa.getPointsFromClearPath(currentGeoPt, toGeoPt, time, true);
								isAtTurn = 1;
								startIndex = nextTurnIndex;
								startIndexStreet += 5;
								
								longitudeTurn = directionInfo[startIndexStreet+2].toString();
								latitudeTurn = directionInfo[startIndexStreet+3].toString();
								longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
								latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
								turnGeoPt =  new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
								
								longitudeTurn = directionInfo[startIndexStreet+5+2].toString();
								latitudeTurn = directionInfo[startIndexStreet+5+3].toString();
								longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
								latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
								nextTurnGeoPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
								
								turnIndex = cpPointsArr.indexOf(turnGeoPt);
								nextTurnIndex = cpPointsArr.indexOf(nextTurnGeoPt);
								
								if(turnIndex != -1)
									arrowGeoPt = cpPointsArr.get(turnIndex);
								if(nextTurnIndex != -1)
									arrowGeoPtNext = cpPointsArr.get(nextTurnIndex); //swapnil.c
					            
								interLocationUpdate2.setLatitude((double)(arrowGeoPt.getLatitudeE6())*1E-6);
								interLocationUpdate2.setLongitude((double)(arrowGeoPt.getLongitudeE6())*1E-6);
		
								//distance to next turn in mtr
								distanceBetweenPointsMtr = (interLocationUpdate1.distanceTo(interLocationUpdate2));
							}
							else{
								isAtTurn = 0;
							}
			            }
			            /*- swapnil.c : code for finding nextTurnDistance -*/
						
						nextTurnDirection = direction = directionInfo[startIndexStreet];
						nextStreetName = directionInfo[startIndexStreet+1];
						longitudeTurn = directionInfo[startIndexStreet+2].toString();
						latitudeTurn = directionInfo[startIndexStreet+3].toString();
						longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
						latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
						turnPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());

	
//						streetLocation1.setLatitude((double)(currentGeoPt.getLatitudeE6())*1E-6);
//						streetLocation1.setLongitude((double)(currentGeoPt.getLongitudeE6())*1E-6);
//						streetLocation2.setLatitude((double)latitude);
//						streetLocation2.setLongitude((double)longitude);		
//											
//						if(streetLocation1.distanceTo(streetLocation2)<30.0f && isNextStreet == 0){
//							startIndexStreet+=5; //hsve to skip 5 values
//							isNextStreet = 1;
//							nextTurnDirection = direction = directionInfo[startIndexStreet];
//							nextStreetName = directionInfo[startIndexStreet+1];
//							longitudeTurn = directionInfo[startIndexStreet+2].toString();
//							latitudeTurn = directionInfo[startIndexStreet+3].toString();
//							longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
//							latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
//							turnPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
//						}
//						else{
//							isNextStreet = 0;
//						}
					}
					//for(int i = countTurnsCrossed*5; i < length; i+=5){
//					for(int i = 0; i < length; i+=5){						
//						direction = directionInfo[i];
//						longitudeTurn = directionInfo[i+2].toString();
//						latitudeTurn = directionInfo[i+3].toString();
//						
//						longitude = new Double(Double.valueOf(longitudeTurn).doubleValue());
//						latitude = new Double(Double.valueOf(latitudeTurn).doubleValue());
//						turnPt = new GeoPoint(Double.valueOf(latitude * 1E6).intValue(),Double.valueOf(longitude * 1E6).intValue());
//// 
////						if(arrowGeoPt != null && arrowGeoPtNext != null){
////							projection.toPixels(arrowGeoPt, startPoint);
////							projection.toPixels(arrowGeoPtNext, endPoint);
////							projection.toPixels(currentGeoPt, currentPoint);
////							slope = Math.atan(((double)(endPoint.y-startPoint.y)*1.0)/((double)(endPoint.x-startPoint.x)*1.0));
////							slope = 90+ slope*180.0/Math.PI;
////							radius = (float) Math.sqrt(Math.pow((float)(endPoint.y-startPoint.y), 2)+Math.pow((float)(endPoint.x-startPoint.x), 2));
////							
//							 //circle around initial point
//							//if(firstLocationFix<5){
////								Paint circlePaint = new Paint();
////						        circlePaint.setAntiAlias(true);
////						        circlePaint.setStrokeWidth(3.0f);
////						        circlePaint.setColor(Color.rgb(27, 131, 216));
////						        circlePaint.setStyle(Style.STROKE);
////						        canvas.drawCircle((float) currentPoint.x, (float) currentPoint.y, radius, circlePaint);
////	
////						        
////						        Paint fillCirclePaint = new Paint();
////						        fillCirclePaint.setAntiAlias(true);
////						        fillCirclePaint.setColor(Color.argb(5, 27, 131, 216));
////						        canvas.drawCircle((float) currentPoint.x, (float) currentPoint.y, radius, fillCirclePaint);
//							//}
//					        
////					        Paint myLocationCircle = new Paint();
////					        myLocationCircle.setAntiAlias(true);
////					        myLocationCircle.setColor(Color.rgb(43, 20, 248));
////					        canvas.drawCircle((float) startPoint.x, (float) startPoint.y, 20, myLocationCircle);
////					        canvas.drawCircle((float) startPoint.x, (float) startPoint.y, 20, circlePaint);
//							
//							
//							//rotation of mapView : swapnil.c : 10272012
////							mapView.setPivotX((float)startPoint.x);
////							mapView.setPivotY((float)startPoint.y);
////							mapView.setRotation((float)slope);
//							//canvas.rotate((float)slope,startPoint.x,startPoint.y);
//						}
//						
////						if(arrowGeoPt != null && arrowGeoPt.equals(turnPt)) { //draw remaining turns only
////							countTurnsCrossed += 1; //increment turn count
////							i+=5;
////						}
//
////						if(i == countTurnsCrossed*5 )
////						{
//							nextTurnDirection = directionInfo[i];
//							nextStreetName = directionInfo[i+1];
//							nextLatitude = latitude;
//							nextLongitude = longitude;
//							nextGeoPt = turnPt;
//							nextMiles = directionInfo[i+4];
////						}
//					}	
					
					//new logic for nextTurnStreet and nextTurnDirection
					
					//for calculating remaining miles to next turn
					float distanceNextTurn = 0, distanceTotal=0; //distance in miles
//					int nextTurnFound=0;
					if(arrowGeoPt!=null && cpPointsArr != null && startIndex != -1) {
						//int startIndex = cpPointsArr.indexOf(arrowGeoPt);
						for(int i=startIndex; (i+1)<(cpPointsArr.size()); i++){
							interLocation1 = new Location("inter1");
							interLocation2 = new Location("inter2");
							interLocation1.setLatitude((double)((cpPointsArr.get(i)).getLatitudeE6())*1E-6);
							interLocation1.setLongitude((double)((cpPointsArr.get(i)).getLongitudeE6())*1E-6);
	
							interLocation2.setLatitude((double)((cpPointsArr.get(i+1)).getLatitudeE6())*1E-6);
							interLocation2.setLongitude((double)((cpPointsArr.get(i+1)).getLongitudeE6())*1E-6);
	
								distanceTotal += (interLocation1.distanceTo(interLocation2))/1609.34;
//							if(nextTurnFound==0)
//								//distanceNextTurn += (interLocation1.distanceTo(interLocation2))/1609.34;
//							if((cpPointsArr.get(i)).equals(nextGeoPt))
//								nextTurnFound=1;
						}
						distanceNextTurn = (float) (distanceBetweenPointsMtr/1609.34);
						//distanceTotal += distanceBetweenPointsMtr/1609.34;
						distanceTotal += distanceNextTurn;
					}
					
					if(nextStreetName != null && nextTurnDirection!=null){
						//sets streetname
						tvstreetname.setText(nextStreetName + "            Clearpath time: " + cptime +" mins");
						//sets miles
						//tvmiles.setText(nextMiles + " miles");
						//if((Float.toString(distanceNextTurn)).length() > 0)
							//tvmiles.setText("            Distance  Total:"+ (Float.toString(distanceTotal)) + " miles       Next Turn: " + (Float.toString(distanceNextTurn)) + " miles");
							  tvmiles.setText("            Distance  Total:"+ new DecimalFormat("###.##").format(distanceTotal) + " miles       Next Turn: " + new DecimalFormat("###.##").format(distanceNextTurn) + " miles");
	
						BitmapFactory.Options options = new BitmapFactory.Options();
			            options.inPurgeable = true;
						//sets turn image
		                if((nextTurnDirection.compareTo("F")== 0) || (nextTurnDirection.compareTo("N")== 0) || (nextTurnDirection.compareTo("E")== 0) || (nextTurnDirection.compareTo("W")== 0))
		                	nextTurnImage.setImageResource(R.drawable.straight);
		                	//canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.straight, options), 0, 0 , null);             
		                else if(nextTurnDirection.compareTo("L")==0)
		                	nextTurnImage.setImageResource(R.drawable.left_turn);
		                else if(nextTurnDirection.compareTo("R")==0)
		                	nextTurnImage.setImageResource(R.drawable.right_turn);
		                else if(nextTurnDirection.compareTo("SL")== 0)
		                	nextTurnImage.setImageResource(R.drawable.sleft_turn);
		                else if(nextTurnDirection.compareTo("SR")== 0)
		                	nextTurnImage.setImageResource(R.drawable.sright_turn);
		                else if(nextTurnDirection.compareTo("M")== 0)
		                	nextTurnImage.setImageResource(R.drawable.merge);
		                else if(nextTurnDirection.contains("M-VLR"))
		                	nextTurnImage.setImageResource(R.drawable.left_ramp);
		                else if(nextTurnDirection.contains("EXIT"))
		                	nextTurnImage.setImageResource(R.drawable.exit);
		                else
		                	nextTurnImage.setImageResource(R.drawable.straight);
		                	//canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.straight, options), 0, 0 , null);
					}
	                //swapnil.c
	                //if(arrowGeoPt != null) {
		            //    Point arrowGeoPtProj = new Point();
		            //    projection.toPixels(arrowGeoPt, arrowGeoPtProj);
		            //    canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.arrow, options), arrowGeoPtProj.x, arrowGeoPtProj.y , null);
	                //}
				     //end of draw function
		}
	}
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//set content layout from gps_nav_screen.xml file (/res/layout)
		setContentView(R.layout.gps_nav_screen);
		
		//get location update for the very first time
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    LocationListener ll = new mylocationlistener();
	    //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, ll); //100 millis and 10 mtr
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 5, ll); //update frequently 0 millis and 0 mtr
	    
	    streetLocation1 = new Location("street location current");
	    streetLocation2 = new Location("street next turn");
	    
	    //Get the current location in start-up //swapnil.c
	    /*arrowGeoPt = new GeoPoint((int)(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()*1000000),
	     (int)(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()*1000000));
	    
	    if(arrowGeoPt!=null)
	    	mapController.animateTo(arrowGeoPt);*/
	    
	    countTurnsCrossed=0; //in starting no turns crossed
	    
	    tvstreetname = (TextView) findViewById(R.id.textView1);
	    tvmiles = (TextView) findViewById(R.id.textView2);
	    nextTurnImage = (ImageView) findViewById(R.id.nextTurnImageView);
	    
    	Intent intent = getIntent();
    	url = intent.getStringExtra("url");
    	fromPtLat = intent.getDoubleExtra("fromPtLat", 0);
    	fromPtLong = intent.getDoubleExtra("fromPtLong", 0);
    	toPtLat = intent.getDoubleExtra("toPtLat", 0);
    	toPtLong = intent.getDoubleExtra("toPtLong", 0); 
    	time = intent.getIntExtra("time", 0);
    	day = intent.getCharSequenceExtra("day");
    	fromStr = intent.getStringExtra("fromStr");
    	toStr = intent.getStringExtra("toStr");
    	
		mapView = (MapView) findViewById(R.id.gpsMapView);
    	mapView.setBuiltInZoomControls(true);
    	mapOverlays = mapView.getOverlays();    	
        mapController = mapView.getController();
        mapController.setZoom(20);	//swapnil.c
        
     	
    	fromGeoPt = new GeoPoint(Double.valueOf(fromPtLat * 1E6).intValue(),Double.valueOf(fromPtLong * 1E6).intValue());
    	toGeoPt = new GeoPoint(Double.valueOf(toPtLat * 1E6).intValue(),Double.valueOf(toPtLong * 1E6).intValue());
    	
        Drawable endmarker = getApplicationContext().getResources().getDrawable(R.drawable.marker);
        CustomItemizedOverlay itemizedoverlay1 = new CustomItemizedOverlay(endmarker, GpsNavScreenActivity.this);
		    
        //draw path with turn arrows
		cpPointsArr = new NavScreenActivity().getPointsFromClearPath(fromGeoPt, toGeoPt, time, true);
		ptListStr = (new HandleHTTP().doInBackground(url));
		
		
        //get lat,lon for all points from clear path
    	String latlonStr = (ptListStr.split("@"))[0];
        String[] points = latlonStr.split(";");
        int length = points.length;
        String cptimestr = points[length-1].split("-")[0];
        double t = Math.round(Double.parseDouble(cptimestr));
        cptime  = String.valueOf((int) t);

		
		streetListStr = ptListStr.split("@")[1];
		directionInfo = streetListStr.split(",");

//		PathOverlay withTurnArrow = new PathOverlay(cpPointsArrModified, Color.GREEN, directionInfo, Color.MAGENTA);
		cpPointsArrModified = new ArrayList<GeoPoint>();
		
		if(cpPointsArr.size()>3){ //one point taken from start and one from end
			googlePointsArrStart = new NavScreenActivity().getPointsFromGoogle(fromGeoPt, cpPointsArr.get(1)); 
			googlePointsArrEnd = new NavScreenActivity().getPointsFromGoogle(cpPointsArr.get((cpPointsArr.size())-2), toGeoPt);
		}
		else{
			googlePointsArrStart = new ArrayList<GeoPoint>();
			googlePointsArrEnd = new ArrayList<GeoPoint>();
			googlePointsArrStart.add(fromGeoPt);
			googlePointsArrEnd.add(toGeoPt);
		}
			//add all google points to path overlay as two-point pairs
			//PathOverlay googlePathStart = new PathOverlay(googlePointsArrStart, Color.GREEN);
			
//		if(cpPointsArr.size()>2){
//			for(int i=0; i<googlePointsArrStart.size()+cpPointsArr.size()+googlePointsArrEnd.size()-2;i++){
//				if(i<googlePointsArrStart.size())
//					cpPointsArrModified.add(googlePointsArrStart.get(i)); //googleArrPointsStart
//				else if(i>=googlePointsArrStart.size() && i<googlePointsArrStart.size()+cpPointsArr.size()-2)
//					cpPointsArrModified.add(cpPointsArr.get(i+1-googlePointsArrStart.size())); //cpPointsArr
//				else if(i>=googlePointsArrStart.size()+cpPointsArr.size()-2 && i<=googlePointsArrStart.size()+cpPointsArr.size()-2+googlePointsArrEnd.size())
//					cpPointsArrModified.add(googlePointsArrEnd.get(i-googlePointsArrStart.size()-cpPointsArr.size()+2)); //googleArrPointsEnd
//			}
//			
//			PathOverlay cpPointsArrModifiedPath = new PathOverlay(cpPointsArrModified, Color.GREEN, directionInfo, Color.BLUE);
//			mapOverlays.add(cpPointsArrModifiedPath);
//		}
		
		if(cpPointsArr.size()>3){
			for(int i=0; i<googlePointsArrStart.size();i++){
				cpPointsArrModified.add(googlePointsArrStart.get(i));
			}
			for(int i=1; i<cpPointsArr.size()-1;i++){
				cpPointsArrModified.add(cpPointsArr.get(i));
			}
			for(int i=0; i<googlePointsArrEnd.size();i++){
				cpPointsArrModified.add(googlePointsArrEnd.get(i));
			}
			PathOverlay cpPointsArrModifiedPath = new PathOverlay(cpPointsArrModified, Color.GREEN, directionInfo, Color.BLUE);
			mapOverlays.add(cpPointsArrModifiedPath);
		}
		else{
			PathOverlay cpPointsArrPath = new PathOverlay(cpPointsArr, Color.GREEN, directionInfo, Color.BLUE);
			mapOverlays.add(cpPointsArrPath);
		}
		
		OverlayItem overlayto = new OverlayItem(toGeoPt, "To Address: ", toStr);
		itemizedoverlay1.addOverlay(overlayto);
		mapOverlays.add(itemizedoverlay1);
		
		//adding arrows
		MyOverlay myoverlay = new MyOverlay();
		mapOverlays.add(myoverlay);
		
		//mapController.animateTo(new GeoPoint(Double.valueOf(34.0522 * 1E6).intValue(),Double.valueOf(-118.2428 * 1E6).intValue()));
        mapController.animateTo(fromGeoPt);
        //mapController.setZoom(20);	//swapnil.c        

        arrow = getApplicationContext().getResources().getDrawable(R.drawable.arrow0);
        itemizedoverlay2 = new CustomItemizedOverlay(arrow, GpsNavScreenActivity.this);
	}
	
	
	public void rotateItem(float rotation){
//		Matrix matrix = new Matrix();
//		matrix.postRotate(rotation);
//		original = BitmapFactory.decodeResource(getResources(),R.drawable.arrow0);
//		Bitmap rotatedBitmap = Bitmap.createBitmap(original,0,0,original.getWidth(),original.getHeight(), matrix, true);
//		BitmapDrawable rotatedImage = new BitmapDrawable(getResources(), rotatedBitmap);
//		itemizedoverlay2 = new CustomItemizedOverlay(rotatedImage, GpsNavScreenActivity.this);
//		mapView.invalidate();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
