package cp.screens;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecentScreenActivity extends ListActivity{
	
	private static final int CLEAR_REC = Menu.FIRST; 
	public static final String REC_PREFS_NAME = "RecPrefsFile";
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  //get recents if any
	  SharedPreferences scRec = getSharedPreferences(REC_PREFS_NAME, 0);
	  Map<String, ?> recMap = scRec.getAll();
	  

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);
	  lv.setBackgroundColor(Color.WHITE);
	  TextView top = new TextView(this);	//(TextView) findViewById(R.id.recent_toptv);
	  top.setText("CP Recents");
	  top.setBackgroundColor(Color.rgb(0, 100, 0));
	  top.setTextColor(Color.WHITE);
	  top.setTextSize(40.0f);
	  top.setPadding(5, 0, 0, 0);
	  top.setTypeface(null, Typeface.BOLD);
	  top.setClickable(false);
	  lv.addHeaderView(top);
	  
	  if(recMap.isEmpty()){					//if no recents add noRec text view
		  TextView empty = new TextView(this);
		  empty.setText("Recents is Empty");
		  empty.setClickable(false);
		  empty.setTextSize(50.0f);
		  empty.setPadding(5, 0, 0, 0);
		  lv.addHeaderView(empty);
		  lv.setBackgroundColor(Color.WHITE);
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_itemrec));
	  } else {																	//else get a list of addressed
		  LinkedList<String> recList = new LinkedList<String>();				//sorted on desc on timestamp
		  long[] recArr = new long[recMap.size()];								//and show it in list
		  List list = new LinkedList(recMap.entrySet());
		  Collections.sort(list, new Comparator() {
			  public int compare(Object o1, Object o2) {
				  return ((Comparable) ((Map.Entry) (o1)).getValue())
		          .compareTo(((Map.Entry) (o2)).getValue());
		      }
		  });
		  int len = list.size();
		  for (int i=len-1;i>=0;i--){
			  recList.add((String)((Map.Entry) list.get(i)).getKey());
		  }
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_itemrec, recList));
	  }
	  
	  	  
	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	      // When clicked, show a toast with the TextView text
	    	String rec = ((TextView) view).getText().toString();
	    	if(rec.equals("CP Recents") || rec.equals("Recents is Empty")){
	    		Toast.makeText(getApplicationContext(), "No Addresses to be Selected", Toast.LENGTH_SHORT).show();
	    	} else{
	    		Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
	    		i.putExtra("to", rec);
	    		startActivity(i);
	    	}
	    }
	  });
	}//onCreate ends	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, CLEAR_REC, 0, R.string.clear_rec);
        return result;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case CLEAR_REC:
        	clearRecent();
        	return true;
        }
    	return super.onOptionsItemSelected(item);
    }
	
	public void clearRecent(){
		SharedPreferences recPref = getSharedPreferences(REC_PREFS_NAME, 0);	
		SharedPreferences.Editor editor = recPref.edit();
		editor.clear();
		editor.commit();
		Intent i = new Intent(this, RecentScreenActivity.class);
		startActivity(i);
		this.finish();
	}
}
