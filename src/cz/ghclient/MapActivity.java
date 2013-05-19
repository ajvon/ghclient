package cz.ghclient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class MapActivity extends FragmentActivity {
	
	private GoogleMap map;
	private Marker curPos;
	private LocationManager locationManager;
	private LatLng lastPos = null;
	static final int DEFAULT_ZOOM = 17;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		if (map != null) {
			startLocationServices();
			String locationProvider = LocationManager.NETWORK_PROVIDER;
			
			Location lastKnownLoc = locationManager
					.getLastKnownLocation(locationProvider);
			LatLng tempLatLng = new LatLng(
					lastKnownLoc.getLatitude(),
					lastKnownLoc.getLongitude()
			);
			
			/*Marker lastPos = map.addMarker(new MarkerOptions()
					.position(tempLatLng)
					.title("Last known loc")
					.snippet("Tady jsem byl vidìt naposledy"));*/
			

			curPos = map.addMarker(new MarkerOptions()
					.position(
							new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc
									.getLongitude())).title("Last known loc")
					.snippet("Tady jsem byl vidìt naposledy")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_launcher)));
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(tempLatLng, DEFAULT_ZOOM));
			bindLocationListener();

			/*Marker hamburg = map.addMarker(new MarkerOptions()
					.position(HAMBURG).title("Hamburg"));
			Marker kiel = map.addMarker(new MarkerOptions()
					.position(KIEL)
					.title("Kiel")
					.snippet("Kiel is cool")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_launcher)));*/

			startLocationServices();
		} else {
			Log.e("ghclient", "cannot prepare map");
		}
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startLocationServices() {
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}

	private void bindLocationListener() {

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location l) {
				// Called when a new location is found by the network location
				// provider.
				// makeUseOfNewLocation(location);

				LatLng curLatLng = new LatLng(l.getLatitude(), l.getLongitude());
				if (lastPos != null) {
					map.addPolyline(new PolylineOptions().add(lastPos)
							.add(curLatLng).width(5).color(Color.RED));
				}
				lastPos = curLatLng;

				curPos.setPosition(curLatLng);
				map.animateCamera(CameraUpdateFactory.newLatLng(curLatLng));

				Log.e("ghclient LOC", "location CHANGED: " + l.toString());
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i("ghclient loc", "STATUS CHANGED. p: " + provider
						+ ", st: " + status);
			}

			public void onProviderEnabled(String provider) {
				Log.i("ghclient loc", "provider ENABLED");
			}

			public void onProviderDisabled(String provider) {
				Log.i("ghclient loc", "provider DISABLED");
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
		/*locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);*/

	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								startActivity(
									new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
								);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							@SuppressWarnings("unused") final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

}
