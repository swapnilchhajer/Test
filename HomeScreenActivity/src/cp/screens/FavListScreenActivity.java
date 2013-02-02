package cp.screens;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FavListScreenActivity extends ListActivity{
	
	private static final int ADD_FAV = Menu.FIRST; 
	private static final int CLEAR_FAV = Menu.FIRST + 1;
	public static final String FAV_PREFS_NAME = "FavPrefsFile";
	private Map<String, ?> favMap;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  //get favorites if any
	  SharedPreferences scFav = getSharedPreferences(FAV_PREFS_NAME, 0);
	  favMap = scFav.getAll();
	  

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);
	  lv.setBackgroundColor(Color.WHITE);
	  TextView top = new TextView(this);	
	  top.setText("CP Favorites");
	  top.setBackgroundColor(Color.rgb(0, 100, 0));
	  top.setTextColor(Color.WHITE);
	  top.setTextSize(40.0f);
	  top.setPadding(5, 0, 0, 0);
	  top.setTypeface(null, Typeface.BOLD);
	  top.setClickable(false);
	  lv.addHeaderView(top);
	  
	  if(favMap.isEmpty()){					//if no favorites 
		  TextView empty = new TextView(this);
		  empty.setText("Favorites is Empty");
		  empty.setClickable(false);
		  empty.setTextSize(50.0f);
		  empty.setPadding(5, 0, 0, 0);
		  lv.addHeaderView(empty);
		  lv.setBackgroundColor(Color.WHITE);
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_itemrec));
	  } else {																	//else get a list of addressed
		  List<String> favList = new LinkedList<String>();
		  for(String key : favMap.keySet()){
			  favList.add(key);
		  }
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_itemrec, favList));
	  }
	  
	  	  
	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	      // When clicked, show a toast with the TextView text
	    	String name = ((TextView) view).getText().toString();
	    	if(name.equals("CP Favorites") || name.equals("Favorites is Empty")){
	    		Toast.makeText(getApplicationContext(), "No Addresses to be Selected", Toast.LENGTH_SHORT).show();
	    	} else{
	    		Intent i = new Intent(getApplicationContext(), NavScreenActivity.class);
	    		String favAddr = (String) favMap.get(name);
	    		i.putExtra("to", favAddr);
	    		startActivity(i);
	    	}
	    }
	  });
	}//onCreate ends	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, ADD_FAV, 0, R.string.add_fav);
        menu.add(0,CLEAR_FAV, 0, R.string.clear_fav);
        return result;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case ADD_FAV:
        	addFavorite();
        	return true;
        	
        case CLEAR_FAV:
        	clearFavorites();
        	return true;
        }
    	return super.onOptionsItemSelected(item);
    }
	
	public void clearFavorites(){
		SharedPreferences recPref = getSharedPreferences(FAV_PREFS_NAME, 0);		
		SharedPreferences.Editor editor = recPref.edit();
		editor.clear();
		editor.commit();
//		ListView lv = getListView();
//		lv.invalidateViews();
//		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_itemrec));
		Intent i = new Intent(this, FavListScreenActivity.class);
		startActivity(i);
		this.finish();
	}
	
	public void addFavorite(){
		Intent i = new Intent(getApplicationContext(), FavMapScreenActivity.class);
		startActivity(i);
	}
}

