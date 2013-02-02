package cp.overlays;

import java.util.ArrayList; 

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import cp.screens.FavListScreenActivity;
import cp.screens.FavMapScreenActivity;

public class CustomItemizedOverlay extends ItemizedOverlay {

	@Override
	public void draw(Canvas arg0, MapView arg1, boolean arg2) {
		// TODO Auto-generated method stub
		super.draw(arg0, arg1, false);
	}

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	
	public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}
	
	public void emptyOverlays(){
		if(mOverlays.size() > 0) //swapnil.c
			while(mOverlays.size() != 0)
				mOverlays.remove(0);
	}
	
	protected boolean onTap(int index){
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("New Favorite: " + item.getSnippet());
		builder.setMessage("Name this Place:");
		final String addr = item.getSnippet();
		// Set an EditText view to get user input 
		final EditText input = new EditText(mContext);
		builder.setView(input);

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				SharedPreferences favPref = mContext.getSharedPreferences(FavListScreenActivity.FAV_PREFS_NAME, 0);		
				SharedPreferences.Editor editor = favPref.edit();
				editor.putString(name, addr);
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
		return true;
	}
}
