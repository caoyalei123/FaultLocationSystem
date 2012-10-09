package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKOLUpdateElement;
import com.baidu.mapapi.MKOfflineMap;
import com.baidu.mapapi.MKOfflineMapListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.liubo.db.LocationDao;
import com.liubo.db.TowerDao;
import com.liubo.map.Constant;
import com.liubo.map.MarkerClusterer;
import com.liubo.map.SingleMarkOverlayImp;
import com.liubo.modal.Location_;
import com.liubo.modal.Tower;
import com.liubo.util.MapUtil;

public class MapMainActivity extends MapActivity {
	public static MapView mapView = null;
	private final String[] menuTexts = {"查看城市"};
	private final Class[] menuIntentClass = {CityChooseDialog.class};
	private final Intent[] menuIntents = new Intent[menuIntentClass.length];
	private TowerDao towerDao = new TowerDao(this);
	MKOfflineMap mOffline = null; // 申明变量
	FaultLocationSystemApp app;
	public static MapMainActivity self;
	public static volatile boolean isFromNotify = false;
	public static final String CITY_CODE = "CITY_CODE";
	public static final int SET_CENTER = 0x000001;
	public static final int ADD_ALERT_TOWER = 0x000002;
	private MarkerClusterer markerClusterer = null;
	public static boolean ifInited = false;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SET_CENTER: {
				String cityCode = msg.getData().getString(CITY_CODE);
				if (cityCode != null) {
					LocationDao locationDao = new LocationDao(MapMainActivity.this);
					Location_ location = locationDao.getLocationByCode(cityCode);
					GeoPoint center = new GeoPoint(location.getLatE6(), location.getLongE6());
					MapMainActivity.mapView.getController().setCenter(center);
					SharedPreferences sharedPre = MapMainActivity.this.getPreferences(MODE_PRIVATE);
					Editor editor = sharedPre.edit();
					editor.putInt(Constant.Common.LATITUDE_E6, center.getLatitudeE6());
					editor.putInt(Constant.Common.LONGITUDE_E6, center.getLongitudeE6());
					editor.commit();
					MapMainActivity.this.refresh();
				}
			}
			break;
			case ADD_ALERT_TOWER : {
				TowerDao towerDao = new TowerDao(MapMainActivity.this);
				Tower tower = towerDao.getTowerById( msg.getData().getString(Constant.Common.TOWER_ID));
				SingleMarkOverlayImp overlay = self.markerClusterer.getMarker(tower);
				GeoPoint point = tower.getGeoPoint();
				mapView.getController().setCenter(point);
				if (overlay == null) {
					boolean isError = tower.isError();
					final SingleMarkOverlayImp singleMark = new SingleMarkOverlayImp(point, tower);
					singleMark.setIsError(isError);
					singleMark.setMarker(getDrawableByID(isError ? R.drawable.mark_error : R.drawable.marker_default));
					self.markerClusterer.addMarker(singleMark);
				} else {
					boolean isError = tower.isError();
					overlay.setMarker(getDrawableByID(isError ? R.drawable.mark_error : R.drawable.marker_default));
					overlay.tower.setStatus(tower.getStatus());
				}
				self.refresh();
			}
			break;
			}
			super.handleMessage(msg);
		}
	};
	private void refresh() {
		markerClusterer._redraw();
	}
	@Override
	public void onNewIntent(Intent intent) {
		isFromNotify = false;
		initMap(intent);
		this.refresh();
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ifInited = true;
		isFromNotify = false;
		self =this;
		setContentView(R.layout.mapview);
		for (int i = 0; i < menuIntents.length; i++) {
			menuIntents[i] = new Intent(this, menuIntentClass[i]);
		}
		app = (FaultLocationSystemApp) this.getApplication();
		if (app.mBMapMan == null) {
			app.mBMapMan = new BMapManager(getApplication());
			app.mBMapMan.init(app.mStrKey, new FaultLocationSystemApp.MyGeneralListener());
		}
		// 如果使用地图SDK，请初始化地图Activity
		app.mBMapMan.start();
		super.initMapActivity(app.mBMapMan);
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mapView.setDrawOverlayWhenZooming(false);
		mapView.setDoubleClickZooming(false);
		mapView.getController().setZoom(12);
		scanOfflineMap();
		initMap(this.getIntent());
	}
	private void initMap(Intent intent) {
		String errorTowerID = String.valueOf(Integer.MIN_VALUE);
		// 如果是从通知栏进入map则设置故障杆塔为地图的中心
		if (intent != null && intent.getStringExtra(Constant.Common.TOWER_ID) != null) {
			isFromNotify = true;
			errorTowerID = intent.getStringExtra(Constant.Common.TOWER_ID);
		} else {
			//读取保存的经纬度
			SharedPreferences sharePre = this.getPreferences(MODE_PRIVATE);
			int latE6 = sharePre.getInt(Constant.Common.LATITUDE_E6, -1);
			int logE6 = sharePre.getInt(Constant.Common.LONGITUDE_E6, -1);
			if (latE6 != -1 && logE6 != -1) {
				mapView.getController().setCenter(new GeoPoint(latE6, logE6));	
			}
			
		}
		// 添加坐标点并且当从通知栏进入map的时候把故障点设置为中心点，设置放大级别为最大
		List<Tower> towers = quaryAllTower();
		ArrayList<SingleMarkOverlayImp> singleOverlays = new ArrayList<SingleMarkOverlayImp>();
		for (final Tower tower : towers) {
			GeoPoint point = tower.getGeoPoint();
			if (point == null) {
				continue;
			}
			if (isFromNotify && tower.getId().equalsIgnoreCase(errorTowerID)) {
				mapView.getController().setCenter(point);
			}
			boolean isError = tower.isError();
			final SingleMarkOverlayImp singleMark = new SingleMarkOverlayImp(point, tower);
			singleMark.setIsError(isError);
			singleMark.setMarker(getDrawableByID(isError ? R.drawable.mark_error : R.drawable.marker_default));
			singleOverlays.add(singleMark);
		}
		markerClusterer = new MarkerClusterer(mapView, singleOverlays, 60, mapView.getMaxZoomLevel() - 1, 2, false);
		mapView.getOverlays().add(markerClusterer);
	}

	public static final Map<Integer, Drawable> idToDawableMap = new HashMap<Integer, Drawable>();

	public static Drawable getDrawableByID(int drawableId) {
		if (idToDawableMap.containsKey(drawableId)) {
			return idToDawableMap.get(drawableId);
		} else {
			Drawable drawable = MapUtil.getDrawableAndSetBounds(
					mapView.getContext(), drawableId);
			drawable = MapUtil.boundCenterBottom(drawable);
			idToDawableMap.put(drawableId, drawable);
			return drawable;
		}
	}

	private void scanOfflineMap() {
		// 写在继承MapActivity里
		// 写在onCreate函数里
		mOffline = new MKOfflineMap();
		mOffline.init(app.mBMapMan, new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				switch (type) {
				case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
					MKOLUpdateElement update = mOffline.getUpdateInfo(state);
					// mText.setText(String.format("%s : %d%%", update.cityName,
					// update.ratio));
				}
					break;
				case MKOfflineMap.TYPE_NEW_OFFLINE:
					Log.d("OfflineDemo",
							String.format("add offlinemap num:%d", state));
					break;
				case MKOfflineMap.TYPE_VER_UPDATE:
					Log.d("OfflineDemo", String.format("new offlinemap ver"));
					break;
				}
			}
		});
		int num = mOffline.scan();
		Log.e("离线地图", "" + num);
	}

	public static String getTowerInfo(final Tower tower) {
		if (tower != null) {
			final String separator = "：\t";
			final String suffix = "\n";
			return self.getString(R.string.tower_num) + separator
					+ tower.getTowerNum() + suffix
					+ self.getText(R.string.station_num) + separator
					+ tower.getStationNum() + suffix
					+ self.getText(R.string.circuit_num) + separator
					+ tower.getCircuit() + suffix
					+ self.getText(R.string.longitude) + separator
					+ tower.getDisplayLogitude() + suffix
					+ self.getText(R.string.latitude) + separator
					+ tower.getDisplayLatitude() + suffix
					+ self.getText(R.string.tower_status) + separator
					+ tower.getStatus() + suffix;
		}
		return "";
	}

	@Override
	protected void onPause() {
		FaultLocationSystemApp app = (FaultLocationSystemApp) this
				.getApplication();
		if (app.mBMapMan != null)
			app.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		FaultLocationSystemApp app = (FaultLocationSystemApp) this
				.getApplication();
		app.mBMapMan.start();
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	protected void onDestroy() {
		FaultLocationSystemApp app = (FaultLocationSystemApp) this
				.getApplication();
		if (app.mBMapMan != null) {
			app.mBMapMan.destroy();
			app.mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		for (int i = 0; i < menuTexts.length; i++) {
			menu.add(Menu.NONE, Menu.FIRST + i + 1, i + 1, menuTexts[i])
					.setIcon(getDrawableIdByIndex(i));
		}
		return true;

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 2 :{
			new CityChooseDialog(this).show();
			break;
		}
		default: {
			return false;
		}
		}
		return true;
	}
	private int getDrawableIdByIndex(int index) {
		int drawableId = R.drawable.set_center;
		switch (index) {
		case 0: {
			drawableId = R.drawable.set_center;
		}
			break;
		case 1: {
			drawableId = R.drawable.set_center;
		}
			break;
		}
		return drawableId;
	}

	private List<Tower> quaryAllTower() {
		return towerDao.getAllTowers();
	}
}
