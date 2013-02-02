package cp.screens;

import java.util.Calendar;

import android.app.Activity; 
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DayTimeSelectActivity extends Activity {
	private TimePicker picker;
    private int mHour;
    private int mMinute;
    private int mDay;
    static final boolean DEPARTURE = true;
    static final boolean ARRIVAL = false;
    private boolean depTime = DEPARTURE;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Calendar cal = Calendar.getInstance();
		mDay = cal.get(Calendar.DAY_OF_WEEK)-1; 
        
        setContentView(R.layout.day_time);
        picker = (TimePicker) findViewById(R.id.timePicker1);  
        Button setbutton = (Button) findViewById(R.id.setTimeButton);
        setbutton.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
				mHour = picker.getCurrentHour();
				mMinute = picker.getCurrentMinute();
				Toast.makeText(DayTimeSelectActivity.this, NavScreenActivity.days[mDay]+", "+mHour+" : "+mMinute, Toast.LENGTH_SHORT).show();
				Intent i = new Intent();
				i.putExtra("hr", mHour);
				i.putExtra("min", mMinute);
				i.putExtra("day", mDay);
				i.putExtra("deporarrival", depTime);
				setResult(RESULT_OK, i);
				finish();
			}
        });

        Spinner spinner =(Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.day_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(mDay);
        
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

    }
    
    public void onToggleClicked(View v) {
        // Perform action on clicks
        if (((ToggleButton) v).isChecked()) {
            depTime = DEPARTURE;
        } else {
            depTime = ARRIVAL;
        }
    }

    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	//mDay = parent.getItemAtPosition(pos).toString();
        	mDay = pos;
        	Log.d("dayseldialog", "day" + mDay);
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }

}