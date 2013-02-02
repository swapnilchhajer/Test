package cp.screens;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class RouteScreenActivity extends Activity {
	String directionInfo[];
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route);
		makeRouteList();
		//setListAdapter(new ArrayAdapter<String>(RouteScreenActivity.this, android.R.layout.simple_list_item_1, listItem));
	}
	
	public void makeRouteList() {
		
        // "myTableLayout" is the id of the element in route.xml
        TableLayout tableLayout = (TableLayout)findViewById(R.id.myTableLayout);
        // we will use service from inflater 
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		
    	ArrayList<String> ret = new ArrayList<String>();
    	Intent intent = getIntent();
    	String url = intent.getStringExtra("url");
    	String ptListStr;
    	String streetListStr;
    	String direction;
    	String street;
    	String miles;
        try{
        	ptListStr = (new HandleHTTP().doInBackground(url));
        	streetListStr = ptListStr.split("@")[1];
            directionInfo = streetListStr.split(",");
            int length = directionInfo.length;
            //int j = 0;
            for(int i = 0; i < length; i+=5){
            	// "route_single" is the name of the xml file "route_single.xml". 
                // Create the itemView who will be added.
                // itemView = route_single.xml
                View itemView = inflater.inflate(R.layout.route_single, null);
                
                direction = directionInfo[i];
                // get the textView, and set the text to something
                ImageView iv = (ImageView) itemView.findViewById(R.id.imageView1);
                
                if((direction.compareTo("F")== 0) || (direction.compareTo("N")== 0))
                	iv.setImageResource(R.drawable.straight);                
                else if((direction.compareTo("W")== 0) || (direction.compareTo("L")==0))
                	iv.setImageResource(R.drawable.left_turn);
                else if((direction.compareTo("E")== 0) || (direction.compareTo("R")==0))
                	iv.setImageResource(R.drawable.right_turn);
                else if(direction.compareTo("SL")== 0)
                	iv.setImageResource(R.drawable.sleft_turn);
                else if(direction.compareTo("SR")== 0)
                	iv.setImageResource(R.drawable.sright_turn);
                else if(direction.compareTo("M")== 0)
                	iv.setImageResource(R.drawable.merge);
                else if(direction.contains("M-VLR"))
                	iv.setImageResource(R.drawable.left_ramp);
                else if(direction.contains("EXIT"))
                	iv.setImageResource(R.drawable.exit);
                else
                	iv.setImageResource(R.drawable.straight);
                
            	street = directionInfo[i+1];
                TextView t1 = (TextView) itemView.findViewById(R.id.textView1); 
                t1.setText(street);                
            	
            	miles = directionInfo[i+4];
                TextView t2 = (TextView) itemView.findViewById(R.id.textView2); 
                t2.setText(miles);
                
                //add the itemView
                tableLayout.addView(itemView, new TableLayout.LayoutParams(
                  LayoutParams.FILL_PARENT, 
                  LayoutParams.WRAP_CONTENT));
            }
         } catch (Exception e){
        	e.getMessage();
        	Log.d("arrivalexec", e.getMessage());
         }
    }
	
}
