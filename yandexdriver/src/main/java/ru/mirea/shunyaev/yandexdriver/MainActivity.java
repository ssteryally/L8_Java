package ru.mirea.shunyaev.yandexdriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;

import ru.mirea.shunyaev.yandexdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements
        DrivingSession.DrivingRouteListener {
    private final Point ROUTE_START_LOCATION = new Point(55.660000, 37.479894);
    private final Point ROUTE_END_LOCATION = new Point(55.698894, 37.805699);
    private final Point SCREEN_CENTER = new Point(
            (ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
            (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2);
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String MAPKIT_API_KEY = "2042aedd-8f49-486c-abba-5b3df50bb250";
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        SearchFactory.initialize(this);
        ru.mirea.shunyaev.yandexdriver.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.mapview;
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 10, 0, 0));
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();

        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        submitRequest();
    }


    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(1);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null));
        requestPoints.add(new RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null));
        drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
        showMarker(ROUTE_END_LOCATION);
    }

    private void showMarker(Point point) {
        PlacemarkMapObject marker = mapObjects.addPlacemark(point);
        marker.addTapListener((mapObject, point1) -> {
            showMarkerInfo();
            return true;
        });
    }

    private void showMarkerInfo() {
        Toast.makeText(getApplicationContext(), "Бар по франшизе Звездные Войны" +
                //"Понедельник Выходной\n" +
                //"Вторник 11:00 – 24:00\n" +
                //"Среда 11:00 – 24:00\n" +
                //"Четверг 11:00 – 24:00\n" +
                //"Пятница 11:00 – 24:00\n" +
                //"Суббота 10:00 – 24:0\n" +
                //"Воскресенье 10:00 – 24:00"
                " | " +
                "Время работы 11:00 – 21:00",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        int color = 0xFFFF0000;
        for (int i = 0; i < list.size(); i++) {
            mapObjects.addPolyline(list.get(i).getGeometry()).setStrokeColor(color);
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}

