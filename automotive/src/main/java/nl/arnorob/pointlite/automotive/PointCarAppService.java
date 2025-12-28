package nl.arnorob.pointlite.automotive;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.car.app.validation.HostValidator;
import nl.arnorob.pointlite.db.DBAdapter;

public class PointCarAppService extends CarAppService {
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new MainSession();
    }

    public static class MainSession extends Session {
        @NonNull
        @Override
        public Screen onCreateScreen(@NonNull android.content.Intent intent) {
            return new MainScreen(getCarContext());
        }
    }

    public static class MainScreen extends Screen {

        protected MainScreen(@NonNull CarContext carContext) {
            super(carContext);
        }

        @NonNull
        @Override
        public Template onGetTemplate() {
            Pane.Builder paneBuilder = new Pane.Builder();
            DBAdapter db = new DBAdapter(getCarContext());
            db.open();
            Cursor cursor = db.getMapMarksCursor();
            boolean hasMarkers = false;
            if (cursor.moveToFirst()) {
                hasMarkers = true;
                do {
                    String name = cursor.getString(DBAdapter.NAME_COLUMN);
                    Row row = new Row.Builder().setTitle(name).build();
                    paneBuilder.addRow(row);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            if (!hasMarkers) {
                paneBuilder.addRow(new Row.Builder().setTitle("No map marks found.").build());
            }

            return new PaneTemplate.Builder(paneBuilder.build())
                    .setTitle("Point")
                    .build();
        }
    }
}
