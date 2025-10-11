package nl.arnorob.pointlite.mapmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.jraf.android.androidwearcolorpicker.ColorPickActivity;

import nl.arnorob.pointlite.R;
import nl.arnorob.pointlite.db.DBAdapter;

public class WearEditMapMark extends Activity {

    static final int REQUEST_PICK_COLOR = 2;
    static final int REQUEST_PICK_LOCATION = 3;
    long id;
    String name;
    int color;
    double lat;
    double lon;
    DBAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getLongExtra("id",0);

        setContentView(R.layout.activity_wear_edit_map_mark);
        adapter = new DBAdapter(this);
        adapter.open();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = adapter.getMapMark(id);
        cursor.moveToFirst();
        name = cursor.getString(DBAdapter.NAME_COLUMN);
        lat = cursor.getDouble(DBAdapter.LATITUDE_COLUMN);
        lon = cursor.getDouble(DBAdapter.LONGITUDE_COLUMN);
        color = cursor.getInt(DBAdapter.COLOR_COLUMN);

        EditText edittext  = findViewById(R.id.name);
        edittext.setText(name);
        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d("nl.arnorob.pointlite","actionid"+actionId+"");
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEND ||
                        keyEvent == null ||
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    name=edittext.getText().toString();
                    adapter.updateMapMark(id,name,lat,lon,color,false,false,0);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edittext.getWindowToken(),0);

                    return true;
                }
                return false;
            }
        });


        LocationConverter lc = new LocationConverter();
        Button location = findViewById(R.id.location);
        location.setText(lc.latitudeAsDMS(lat,1)+", "+lc.longitudeAsDMS(lon,1));
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WearEditMapMark.this, WearLocationPicker.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                startActivityForResult(intent,REQUEST_PICK_LOCATION);
            }
        });


        Button bcolor = findViewById(R.id.color);
        bcolor.setTextColor(color);
        bcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new ColorPickActivity.IntentBuilder()
                        .oldColor(color)
                        .build(WearEditMapMark.this);
                startActivityForResult(intent, REQUEST_PICK_COLOR);
            }
        });


        Button delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO confirmation dialog
                adapter.deleteMapMark(id);
                finish();
            }
        });

        cursor.close();
    }

    @Override
    protected void onDestroy() {
        adapter.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case REQUEST_PICK_COLOR:
                color = data.getIntExtra("EXTRA_RESULT",0);
                break;
            case REQUEST_PICK_LOCATION:
                lat = data.getDoubleExtra("lat",0);
                lon = data.getDoubleExtra("lon",0);
                break;
        }
        adapter.updateMapMark(id,name,lat,lon,color,false,false,0);
    }

}
