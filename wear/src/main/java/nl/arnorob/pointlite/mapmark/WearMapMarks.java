package nl.arnorob.pointlite.mapmark;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import nl.arnorob.pointlite.R;
import nl.arnorob.pointlite.db.DBAdapter;


public class WearMapMarks extends Activity {

    float flat;
    float flon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wear_map_marks);

        flat = getIntent().getFloatExtra("latitude",0);
        flon = getIntent().getFloatExtra("longitude",0);
    }

    DBAdapter adapter;
    Cursor cursor;

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout ll = findViewById(R.id.mapmarklist);
        ll.removeAllViews();

         adapter = new DBAdapter(this);
        adapter.open();

        Button addmapmark = new Button(this);
        addmapmark.setText("Add New");
        addmapmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = adapter.insertMapMark("Here",flat,flon, Color.WHITE,false,false,0,DBAdapter.MAPMARK);
                Intent intent = new Intent(WearMapMarks.this,WearEditMapMark.class);
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });
        //TODO: add new mapmark and start editing it
        ll.addView(addmapmark);

        cursor = adapter.getMapMarksCursor();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            String name = cursor.getString(DBAdapter.NAME_COLUMN);
            int color = cursor.getInt(DBAdapter.COLOR_COLUMN);
            long id = cursor.getLong(DBAdapter.ID_COLUMN);
            Button button = new Button(this);
            button.setText(name);
            button.setTextColor(color);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: create wear edit mapmark activity.
                    Intent intent = new Intent(WearMapMarks.this,WearEditMapMark.class);
                    intent.putExtra("id",id);
                    startActivity(intent);
                }
            });
            ll.addView(button);
            cursor.moveToNext();
        }

        Button settings = new Button(this);
        settings.setText("Settings");
        //TODO: start settings metric / imperial
//        ll.addView(settings);

    }

    @Override
    protected void onPause() {
        cursor.close();
        adapter.close();

        super.onPause();
    }
}
