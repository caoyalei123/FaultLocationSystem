package com.liubo.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKMapViewListener;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.liubo.AlertRecordActiviry;
import com.liubo.FaultLocationSystemApp;
import com.liubo.MapMainActivity;
import com.liubo.R;
import com.liubo.db.TowerDao;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.modal.Tower;

/**
 * @fileoverview MarkerClusterer��Ǿۺ�������������ش�����Ҫ�ص���ͼ�ϲ���������������⣬��������ܡ� ���������
 *               ����Baidu Map API 1.2��
 * 
 * @author Baidu Map Api Group
 * @version 1.2
 */
public class MarkerClusterer<Item extends SingleMarkOverlayImp> extends Overlay {

	// ��ͼ��һ��ʵ��
	private MapView _map;
	// Ҫ�ۺϵı������
	private ArrayList<Item> _markers;
	// �ۺϼ���ʱ��������ش�С��Ĭ��60
	private final int DEFAULT_GRID_SIZE = 60;
	private int _gridSize = DEFAULT_GRID_SIZE;
	// ���ľۺϼ��𣬴��ڸü���Ͳ�������Ӧ�ľۺ�
	private int maxZoom;
	// ��С�ľۺ�������С�ڸ������Ĳ��ܳ�Ϊһ���ۺϣ�Ĭ��Ϊ2
	private int minClusterSize;
	// �ۺϵ�����λ���Ƿ������оۺ����ڵ��ƽ��ֵ��Ĭ��Ϊ������ھۺ��ڵĵ�һ����
	private boolean isAverangeCenter;
	private ArrayList<Cluster> _clusters;
	private int _maxZoom;
	private int _minClusterSize;
	private boolean _isAverageCenter;

	/**
	 * ��ȡһ����չ����ͼ��Χ�����������Ҷ�����һ��������ֵ��
	 * 
	 * @param {Map} map BMap.Map��ʵ��������
	 * @param {BMap.Bounds} bounds BMap.Bounds��ʵ��������
	 * @param {Number} gridSize Ҫ���������ֵ
	 * 
	 * @return {BMap.Bounds} ������������ͼ��Χ��
	 */
	public Bounds getExtendedBounds(MapView map, Bounds bounds, int gridSize) {
		bounds = cutBoundsInRange(bounds);
		Point pixelNE = new Point();
		map.getProjection().toPixels(bounds.getNorthEast(), pixelNE);
		Point pixelSW = new Point();
		map.getProjection().toPixels(bounds.getSouthWest(), pixelSW);
		pixelNE.x += gridSize;
		pixelNE.y -= gridSize;
		pixelSW.x -= gridSize;
		pixelSW.y += gridSize;
		GeoPoint newNE = map.getProjection().fromPixels(pixelNE.x, pixelNE.y);
		GeoPoint newSW = map.getProjection().fromPixels(pixelSW.x, pixelSW.y);
		return new Bounds(newSW, newNE);
	}

	/**
	 * ���հٶȵ�ͼ֧�ֵ����緶Χ��bounds���б߽紦��
	 * 
	 * @param {BMap.Bounds} bounds BMap.Bounds��ʵ��������
	 * 
	 * @return {BMap.Bounds} ���ز�Խ�����ͼ��Χ
	 */
	public Bounds cutBoundsInRange(Bounds bounds) {
		int maxX = getRange(bounds.getNorthEast().getLongitudeE6(),
				(int) (-180 * 1E6), (int) (180 * 1E6));
		int minX = getRange(bounds.getSouthWest().getLongitudeE6(),
				(int) (-180 * 1E6), (int) (180 * 1E6));
		int maxY = getRange(bounds.getNorthEast().getLatitudeE6(),
				(int) (-74 * 1E6), (int) (74 * 1E6));
		int minY = getRange(bounds.getSouthWest().getLatitudeE6(),
				(int) (-74 * 1E6), (int) (74 * 1E6));
		return new Bounds(new GeoPoint(minY, minX), new GeoPoint(maxY, maxX));
	}

	/**
	 * �Ե���ֵ���б߽紦��
	 * 
	 * @param {Number} i Ҫ�������ֵ
	 * @param {Number} min �±߽�ֵ
	 * @param {Number} max �ϱ߽�ֵ
	 * 
	 * @return {Number} ���ز�Խ�����ֵ
	 */
	public int getRange(int i, int min, int max) {
		i = Math.max(i, min);
		i = Math.min(i, max);
		return i;
	}

	/**
	 * MarkerClusterer
	 * 
	 * @class ����������ش�����Ҫ�ص���ͼ�ϲ���������������⣬���������
	 * @constructor
	 * @param {Map} map ��ͼ��һ��ʵ����
	 * @param {Json Object} options ��ѡ��������ѡ�������<br />
	 *        markers {Array<Marker>} Ҫ�ۺϵı������<br />
	 *        girdSize {Number} �ۺϼ���ʱ��������ش�С��Ĭ��60<br />
	 *        maxZoom {Number} ���ľۺϼ��𣬴��ڸü���Ͳ�������Ӧ�ľۺ�<br />
	 *        minClusterSize {Number} ��С�ľۺ�������С�ڸ������Ĳ��ܳ�Ϊһ���ۺϣ�Ĭ��Ϊ2<br />
	 *        isAverangeCenter {Boolean} �ۺϵ�����λ���Ƿ������оۺ����ڵ��ƽ��ֵ��Ĭ��Ϊ������ھۺ��ڵĵ�һ����<br />
	 *        styles {Array<IconStyle>} �Զ���ۺϺ��ͼ������ο�TextIconOverlay��<br />
	 */
	public MarkerClusterer(MapView map, ArrayList<Item> markers, int girdSize,
			int maxZoom, int minClusterSize, boolean isAverangeCenter) {
		// super(MapMainActivity.getDrawableByID(R.drawable.marker_default));
		if (map == null) {
			return;
		}
		this._map = map;
		this._markers = new ArrayList<Item>();
		this._clusters = new ArrayList<Cluster>();
		this._gridSize = girdSize;
		if (maxZoom > this._map.getZoomLevel()) {
			maxZoom = this._map.getZoomLevel();
		}
		this._maxZoom = maxZoom;
		if (minClusterSize < 2) {
			minClusterSize = 2;
		}
		this._minClusterSize = minClusterSize;
		this._isAverageCenter = isAverangeCenter;
		final MarkerClusterer that = this;
		map.regMapViewListener(FaultLocationSystemApp.mBMapMan,
				new MKMapViewListener() {
					@Override
					public void onMapMoveFinish() {
						that._redraw();
					}

				});
		if (markers != null && markers.size() > 0) {
			this.addMarkers(markers);
		}
	}

	public MarkerClusterer(MapView map) {
		this(map, null, 60, map.getMaxZoomLevel() - 1, 2, false);
	}

	private final Map<String, SingleMarkOverlayImp> towerId2Marker = new HashMap<String, SingleMarkOverlayImp>();
	public SingleMarkOverlayImp getMarker(Tower tower) {
		if (tower == null) {
			return null;
		}
		return towerId2Marker.get(tower.getStationNum() + tower.getCircuit() + tower.getTowerNum());
	}
	/**
	 * ���Ҫ�ۺϵı�����顣
	 * 
	 * @param {Array<Marker>} markers Ҫ�ۺϵı������
	 * 
	 * @return �޷���ֵ��
	 */
	public void addMarkers(ArrayList<Item> markers) {
		for (Item item : markers) {
			this._pushMarkerTo(item);
		}
		this._createClusters();
	}

	/**
	 * ��һ�������ӵ�Ҫ�ۺϵı��������
	 * 
	 * @param {BMap.Marker} marker Ҫ��ӵı��
	 * 
	 * @return �޷���ֵ��
	 */
	public void _pushMarkerTo(Item marker) {
		int index = this._markers.indexOf(marker);
		if (index == -1) {
			marker.isInCluster = false;
			towerId2Marker.put(marker.tower.getStationNum() + marker.tower.getCircuit() + marker.tower.getTowerNum(), marker);
			this._markers.add(marker);// Marker�Ϸź�enableDragging�����仯������
		}
	}

	/**
	 * ���һ���ۺϵı�ǡ�
	 * 
	 * @param {BMap.Marker} marker Ҫ�ۺϵĵ�����ǡ�
	 * @return �޷���ֵ��
	 */
	public void addMarker(Item marker) {
		this._pushMarkerTo(marker);
		_createClusters();
	}

	/**
	 * �����������ı�ǣ������ۺϵ�
	 * 
	 * @return �޷���ֵ
	 */
	public void _createClusters() {
		Bounds mapBounds = getBounds();
		Bounds extendedBounds = getExtendedBounds(this._map, mapBounds,
				this._gridSize);
		for (Item marker : this._markers) {
			if (!marker.isInCluster
					&& extendedBounds.containsPoint(marker.getPoint())) {
				this._addToClosestCluster(marker);
			}
		}
	}

	private Bounds getBounds() {
		Log.e("getBounds",
				"L:" + this._map.getLeft() + "R: " + this._map.getRight()
						+ "T: " + this._map.getTop() + "B: "
						+ this._map.getBottom());
		return new Bounds(this._map.getProjection().fromPixels(0,
				this._map.getBottom() - this._map.getTop()), this._map
				.getProjection().fromPixels(
						this._map.getRight() - this._map.getLeft(), 0));
	}

	/**
	 * ���ݱ�ǵ�λ�ã�������ӵ�����ľۺ���
	 * 
	 * @param {BMap.Marker} marker Ҫ���оۺϵĵ������
	 * 
	 * @return �޷���ֵ��
	 */
	public void _addToClosestCluster(Item marker) {
		int distance = this._gridSize * this._gridSize;
		Cluster clusterToAddTo = null;
		GeoPoint position = marker.getPoint();
		for (Cluster cluster : this._clusters) {
			GeoPoint center = cluster.getCenter();
			if (center != null) {
				Point centerPoint = new Point();
				this._map.getProjection().toPixels(center, centerPoint);
				Point positionPoint = new Point();
				this._map.getProjection().toPixels(position, positionPoint);
				int d = ((centerPoint.x - positionPoint.x) * (centerPoint.x - positionPoint.x))
						+ ((centerPoint.y - positionPoint.y) * (centerPoint.y - positionPoint.y));
				if (d < this._gridSize * this._gridSize) {
					distance = d;
					clusterToAddTo = cluster;
				}
			}
		}

		if (clusterToAddTo != null
				&& clusterToAddTo.isMarkerInClusterBounds(marker)) {
			clusterToAddTo.addMarker(marker);
		} else {
			Cluster cluster = new Cluster(this);
			cluster.addMarker(marker);
			this._clusters.add(cluster);
		}
	}

	/**
	 * �����һ�εľۺϵĽ��
	 * 
	 * @return �޷���ֵ��
	 */
	public void _clearLastClusters() {
		for (Cluster cluster : _clusters) {
			cluster.remove();
		}
		this._clusters.clear();// �ÿ�Cluster����
		this._removeMarkersFromCluster();// ��Marker��cluster�����Ϊfalse
	}

	/**
	 * ���ĳ���ۺ��е����б��
	 * 
	 * @return �޷���ֵ
	 */
	public void _removeMarkersFromCluster() {
		for (Item marker : _markers) {
			marker.isInCluster = false;
		}
	}

	/**
	 * �����еı�Ǵӵ�ͼ�����
	 * 
	 * @return �޷���ֵ
	 */
	public void _removeMarkersFromMap() {
		for (Item marker : _markers) {
			marker.isInCluster = false;
			// this._map.getOverlays().remove(marker);
		}
	}

	/**
	 * ɾ���������
	 * 
	 * @param {BMap.Marker} marker ��Ҫ��ɾ����marker
	 * 
	 * @return {Boolean} ɾ���ɹ�����true�����򷵻�false
	 */
	public boolean _removeMarker(Item marker) {
		int index = _markers.indexOf(marker);
		if (index == -1) {
			return false;
		}
		// this._map.getOverlays().remove(marker);
		this._markers.remove(index);
		return true;
	};

	/**
	 * ɾ���������
	 * 
	 * @param {BMap.Marker} marker ��Ҫ��ɾ����marker
	 * 
	 * @return {Boolean} ɾ���ɹ�����true�����򷵻�false
	 */
	public boolean removeMarker(Item marker) {
		boolean success = this._removeMarker(marker);
		if (success) {
			this._clearLastClusters();
			this._createClusters();
		}
		return success;
	}

	/**
	 * ɾ��һ����
	 * 
	 * @param {Array<BMap.Marker>} markers ��Ҫ��ɾ����marker����
	 * 
	 * @return {Boolean} ɾ���ɹ�����true�����򷵻�false
	 */
	public boolean removeMarkers(ArrayList<Item> markers) {
		boolean success = false;
		for (Item marker : markers) {
			boolean r = this._removeMarker(marker);
			success = success || r;
		}

		if (success) {
			this._clearLastClusters();
			this._createClusters();
		}
		return success;
	}

	/**
	 * �ӵ�ͼ�ϳ���������еı��
	 * 
	 * @return �޷���ֵ
	 */
	public void clearMarkers() {
		this._clearLastClusters();
		this._removeMarkersFromMap();
		this._markers.clear();
	};

	/**
	 * �������ɣ�����ı������Ե�
	 * 
	 * @return �޷���ֵ
	 */
	public void _redraw() {
		this._clearLastClusters();
		this._createClusters();
	}

	/**
	 * ��ȡ�����С
	 * 
	 * @return {Number} �����С
	 */
	public int getGridSize() {
		return this._gridSize;
	};

	/**
	 * ���������С
	 * 
	 * @param {Number} size �����С
	 * @return �޷���ֵ
	 */
	public void setGridSize(int size) {
		this._gridSize = size;
		this._redraw();
	};

	/**
	 * ��ȡ�ۺϵ�������ż���
	 * 
	 * @return {Number} �ۺϵ�������ż���
	 */
	public int getMaxZoom() {
		return this._maxZoom;
	}

	/**
	 * ���þۺϵ�������ż���
	 * 
	 * @param {Number} maxZoom �ۺϵ�������ż���
	 * @return �޷���ֵ
	 */
	public void setMaxZoom(int maxZoom) {
		this._maxZoom = maxZoom;
		this._redraw();
	}

	/**
	 * ��ȡ�����ۺϵ���С������
	 * 
	 * @return {Number} �����ۺϵ���С������
	 */
	public int getMinClusterSize() {
		return this._minClusterSize;
	}

	/**
	 * ���õ����ۺϵ���С������
	 * 
	 * @param {Number} size �����ۺϵ���С������
	 * @return �޷���ֵ��
	 */
	public void setMinClusterSize(int size) {
		this._minClusterSize = size;
		this._redraw();
	};

	/**
	 * ��ȡ�����ۺϵ���ŵ��Ƿ��Ǿۺ������б�ǵ�ƽ�����ġ�
	 * 
	 * @return {Boolean} true��false��
	 */
	public boolean isAverageCenter() {
		return this._isAverageCenter;
	}

	/**
	 * ��ȡ�ۺϵ�Mapʵ����
	 * 
	 * @return {Map} Map��ʾ����
	 */
	public MapView getMap() {
		return this._map;
	};

	/**
	 * ��ȡ���еı�����顣
	 * 
	 * @return {Array<Marker>} ������顣
	 */
	public ArrayList<Item> getMarkers() {
		return this._markers;
	}

	/**
	 * ��ȡ�ۺϵ���������
	 * 
	 * @return {Number} �ۺϵ���������
	 */
	public int getClustersCount() {
		int count = 0;
		for (Cluster cluster : _clusters) {
			if (cluster.isReal()) {
				count++;
			}
		}
		return count;
	}

	public class Cluster<T extends SingleMarkOverlayImp> {
		private MarkerClusterer _markerClusterer;
		private MapView _map;
		private int _minClusterSize;
		private boolean _isAverageCenter;
		public ArrayList<T> _markers;
		private GeoPoint _center = null;
		private TextIconOverlay _clusterMarker;
		private Bounds _gridBounds;
		private boolean _isReal;

		/**
		 * @ignore Cluster
		 * @class ��ʾһ���ۺ϶��󣬸þۺϣ�������N����ǣ���N�������ɵķ�Χ������������ʾ��Map�ϵ�TextIconOverlay�ȡ�
		 * @constructor
		 * @param {MarkerClusterer} markerClusterer һ����Ǿۺ���ʾ����
		 */
		public Cluster(MarkerClusterer markerClusterer) {
			this._markerClusterer = markerClusterer;
			this._map = markerClusterer.getMap();
			this._minClusterSize = markerClusterer.getMinClusterSize();
			this._isAverageCenter = markerClusterer.isAverageCenter();
			this._center = null;// ���λ��
			this._markers = new ArrayList<T>();// ���Cluster����������markers
			this._gridBounds = null;// �����ĵ�Ϊ׼�����ı�����gridSize�����صķ�Χ��Ҳ������Χ
			this._isReal = false; // ����Ǹ��ۺ�

			this._clusterMarker = new TextIconOverlay(this._center,
					this._markers.size(), getStyle(this._markers));
		}

		/**
		 * ��þۺ����һ����ǡ�
		 * 
		 * @param {Marker} marker Ҫ��ӵı�ǡ�
		 * @return �޷���ֵ��
		 */
		public boolean addMarker(T marker) {
			if (this.isMarkerInCluster(marker)) {
				return false;
			}// Ҳ����marker.isInCluster�ж�,�����ж�OK�����������������

			if (_center == null) {
				this._center = marker.getPoint();
				this.updateGridBounds();//
			} else {
				if (this._isAverageCenter) {
					int l = this._markers.size() + 1;
					int lat = (this._center.getLatitudeE6() * (l - 1) + marker
							.getPoint().getLatitudeE6()) / l;
					int lng = (this._center.getLongitudeE6() * (l - 1) + marker
							.getPoint().getLongitudeE6()) / l;
					this._center = new GeoPoint(lat, lng);
					this.updateGridBounds();
				}// �����µ�Center
			}

			marker.isInCluster = true;
			this._markers.add(marker);

			int len = this._markers.size();
			if (len < this._minClusterSize) {
				_isReal = false;
				return true;
			}
			if (this._map.getOverlays().indexOf(this._clusterMarker) == -1) {
				this._map.getOverlays().add(this._clusterMarker);				
			}
			this._isReal = true;
			this.updateClusterMarker();
			return true;
		}

		/**
		 * �ж�һ������Ƿ��ڸþۺ��С�
		 * 
		 * @param {Marker} marker Ҫ�жϵı�ǡ�
		 * @return {Boolean} true��false��
		 */
		public boolean isMarkerInCluster(T marker) {
			return _markers.contains(marker);
		}

		/**
		 * �ж�һ������Ƿ��ڸþۺ�����Χ�С�
		 * 
		 * @param {Marker} marker Ҫ�жϵı�ǡ�
		 * @return {Boolean} true��false��
		 */
		public boolean isMarkerInClusterBounds(T marker) {
			return this._gridBounds.containsPoint(marker.getPoint());
		}

		public boolean isReal() {
			return this._isReal;
		}

		/**
		 * ���¸þۺϵ�����Χ��
		 * 
		 * @return �޷���ֵ��
		 */
		public void updateGridBounds() {
			Bounds bounds = new Bounds(this._center, this._center);
			this._gridBounds = getExtendedBounds(this._map, bounds,
					this._markerClusterer.getGridSize());
		}

		/**
		 * ���¸þۺϵ���ʾ��ʽ��Ҳ��TextIconOverlay��
		 * 
		 * @return �޷���ֵ��
		 */
		public void updateClusterMarker() {
			if (this._map.getZoomLevel() > this._markerClusterer.getMaxZoom()) {
				_isReal = false;
				if (this._clusterMarker != null) {
					this._map.getOverlays().remove(_clusterMarker);
				}
				return;
			}

			if (this._markers.size() < this._minClusterSize) {
				_isReal = false;
				if (this._clusterMarker != null) {
					this._map.getOverlays().remove(_clusterMarker);
				}
				return;
			}

			this._clusterMarker.setPosition(this._center);
			this._clusterMarker.setStyle(getStyle(this._markers));
			this._clusterMarker.setMarkerCount(this._markers.size());
			final MapView thatMap = this._map;
			this._clusterMarker
					.setOnclickListener(new TextIconOverlay.OnClickListener() {

						@Override
						public void onClick() {
							thatMap.getController().setCenter(_center);
							if (thatMap.getZoomLevel() < thatMap
									.getMaxZoomLevel()) {
								thatMap.getController().zoomIn();
							}
						}
					});
		}

		/**
		 * ɾ���þۺϡ�
		 * 
		 * @return �޷���ֵ��
		 */
		public void remove() {
			_isReal = false;
			this._map.getOverlays().remove(_clusterMarker);
			_markers.clear();
		}

		/**
		 * ��ȡ�þۺ������������б�ǵ���С��Ӿ��εķ�Χ��
		 * 
		 * @return {BMap.Bounds} ������ķ�Χ��
		 */
		public Bounds getBounds() {
			Bounds bounds = new Bounds(this._center, this._center);
			for (T marker : _markers) {
				bounds.extend(marker.getPoint());
			}
			return bounds;

		}

		/**
		 * ��ȡ�þۺϵ���ŵ㡣
		 * 
		 * @return {BMap.Point} �þۺϵ���ŵ㡣
		 */
		public GeoPoint getCenter() {
			return this._center;
		}
	}

	enum Style {
		Style0_10(0, 10, R.drawable.m0, 20, 0xffff8f00), Style10_IntMax(10,
				Integer.MAX_VALUE, R.drawable.m1, 30, Color.BLUE);
		private final int min;
		private final int max;
		final int drawableId;
		final int textSize;
		final int color;

		private Style(int min, int max, int drawableId, int textSize, int color) {
			this.min = min;
			this.max = max;
			this.drawableId = drawableId;
			this.textSize = textSize;
			this.color = color;
		}

		boolean fit(int count) {
			return count >= min && count < max;
		}
	}

	private static <T extends SingleMarkOverlayImp> Style getStyle(
			ArrayList<T> marks) {
		// ��������治�����д���ĸ�����������ɫ�ķ����û�ɫ��
		for (T overlay : marks) {
			if (overlay.isError()) {
				return Style.Style10_IntMax;
			}
		}
		return Style.Style0_10;
	}
	Paint paint = new Paint(); 
	{
		paint.setTextSize(20);
		paint.setColor(Color.RED);
		paint.setStrokeWidth(3);
		paint.setAntiAlias(true);
	}
	@Override
	public void draw(Canvas paramCanvas, MapView paramMapView,
			boolean paramBoolean) {
		for (Cluster cluster : _clusters) {
			if (!cluster.isReal()) {
				for (int i = 0; i < cluster._markers.size(); i++) {
					SingleMarkOverlayImp localOverlayItem = (SingleMarkOverlayImp) cluster._markers
							.get(i);
					if (!isInVisible(paramMapView, localOverlayItem)) {
						continue;
					}
					Point localPoint = paramMapView.getProjection().toPixels(
							localOverlayItem.getPoint(), null);
					drawIcon(
							paramCanvas,
							localOverlayItem
									.getMarker(0),
							localPoint.x, localPoint.y);
				    if (localOverlayItem.getTitle() != null) {
					      paramCanvas.drawText("P:" + localOverlayItem.getTitle(), localPoint.x - paint.measureText("P:" + localOverlayItem.getTitle()) - 2, localPoint.y, paint);
					      paramCanvas.drawText("S:" + localOverlayItem.tower.getStationNum() + ";" + "L:" + localOverlayItem.tower.getCircuit(), localPoint.x + 2, localPoint.y, paint);
				    }
					if (towerId2Marker.containsKey(localOverlayItem.tower.getStationNum() + localOverlayItem.tower.getCircuit() + localOverlayItem.tower.getPreTowerNum())) {
						SingleMarkOverlayImp localOverlayItem2 = towerId2Marker
								.get(localOverlayItem.tower.getStationNum() + localOverlayItem.tower.getCircuit() + localOverlayItem.tower.getPreTowerNum());
						if (isInVisible(paramMapView, localOverlayItem2)) {
							Point point2 = paramMapView.getProjection()
									.toPixels(localOverlayItem2.getPoint(),
											null);
							paramCanvas.drawLine(localPoint.x, localPoint.y,
									point2.x, point2.y, paint);
						}
					}
				}
			}
		}
	}

	static boolean isInVisible(MapView paramMapView, SingleMarkOverlayImp marker) {
		Point localPoint = paramMapView.getProjection().toPixels(
				marker.getPoint(), null);
		int m = paramMapView.getLeft();
		int n = paramMapView.getRight();
		int i1 = paramMapView.getTop();
		int i2 = paramMapView.getBottom();
		localPoint.x += m;
		localPoint.y += i1;
		if ((localPoint.x < m) || (localPoint.y < i1) || (localPoint.x > n)
				|| (localPoint.y > i2))
			return false;
		return true;
	}

	static void drawIcon(Canvas paramCanvas, Drawable paramDrawable,
			int paramInt1, int paramInt2) {
		Rect localRect = paramDrawable.getBounds();
		if (localRect.width() == 0)
			localRect.right = (localRect.left + paramDrawable
					.getIntrinsicWidth());
		if (localRect.height() == 0)
			localRect.bottom = (localRect.top + paramDrawable
					.getIntrinsicHeight());
		paramDrawable.setBounds(localRect.left + paramInt1, localRect.top
				+ paramInt2, localRect.right + paramInt1, localRect.bottom
				+ paramInt2);
		paramDrawable.draw(paramCanvas);
		paramDrawable.setBounds(localRect.left - paramInt1, localRect.top
				- paramInt2, localRect.right - paramInt1, localRect.bottom
				- paramInt2);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		boolean bool = false;
		for (Cluster cluster : _clusters) {
			if (!cluster.isReal()) {
				for (int i = 0; i < cluster._markers.size(); i++) {
					SingleMarkOverlayImp localOverlayItem = (SingleMarkOverlayImp) cluster._markers
							.get(i);

					Projection localProjection = mapView.getProjection();
					Point localPoint1 = localProjection.toPixels(p, null);
					double d1 = Double.MAX_VALUE;
					double d2 = -1.0D;
					Point localPoint2 = a(localOverlayItem, localProjection,
							localPoint1);
					Drawable localDrawable = localOverlayItem
							.getMarker(OverlayItem.ITEM_STATE_NORMAL_MASK);
					if (hitTest(localOverlayItem, localDrawable, localPoint2.x,
							localPoint2.y)) {
						d2 = localPoint2.x * localPoint2.x + localPoint2.y
								* localPoint2.y;
					}
					if ((d2 < 0.0D) || (d2 >= d1)) {
						bool = false;
					} else {
						onclick(localOverlayItem);
						return true;
					}
				}
			}
		}
		return bool;
	}

	public void onclick(final SingleMarkOverlayImp singleMark) {
		if (singleMark.isError()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MapMainActivity.self);
			builder.setTitle("ȷ��\"" + singleMark.tower.getTowerNum() + "\"���������Ѿ��ָ���");
			builder.setMessage(MapMainActivity.getTowerInfo(singleMark.tower));
			builder.setPositiveButton("�ָ�����״̬", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					TowerDao towerDao = new TowerDao(MapMainActivity.self);
					String towerNum = singleMark.tower.getTowerNum();
					String circuitNum = singleMark.tower.getCircuit();
					String stationNum = singleMark.tower.getStationNum();
					Tower tower = new Tower();
					try {
						tower = towerDao.getTower(towerNum, circuitNum, stationNum);
					} catch (TowerNotUniqueException e) {
						Toast.makeText(MapMainActivity.self, "ϵͳ���󣺸�����Ϣ��ͻ���������ݡ�", Toast.LENGTH_LONG).show();
						e.printStackTrace();
						return ;
					}
					towerDao.updateTowerStatusById(tower.getId(),
							Tower.Status.NORMAL.value);
					singleMark.setIsError(false);
					singleMark.tower.setStatus(Tower.Status.NORMAL.value);
					singleMark.setMarker(MapMainActivity.getDrawableByID(R.drawable.marker_default));
					MapMainActivity.mapView.invalidate();
				}
			});
			/*builder.setNeutralButton("�澯��Ϣ", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MapMainActivity.self,
							AlertRecordActiviry.class);
					intent.putExtra(Constant.Common.TOWER_NUM,
							singleMark.tower.getTowerNum());
					MapMainActivity.self.startActivity(intent);
				}
			});*/
			builder.setNegativeButton("ȡ��", null);
			builder.create().show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(MapMainActivity.self);
			builder.setMessage(MapMainActivity.getTowerInfo(singleMark.tower));
			builder.setTitle("\"" + singleMark.tower.getTowerNum() + "\"������Ϣ");
			builder.setPositiveButton("��ʷ������Ϣ", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MapMainActivity.self, AlertRecordActiviry.class);
					intent.putExtra(Constant.Common.TOWER_ID,
							singleMark.tower.getId());
					MapMainActivity.self.startActivity(intent);
				}
			});
			builder.setNegativeButton("ȡ��", null);
			builder.create().show();
		}
	}

	protected boolean hitTest(SingleMarkOverLay paramOverlayItem,
			Drawable paramDrawable, int paramInt1, int paramInt2) {
		Rect localRect = paramDrawable.getBounds();
		int i = 10;
		localRect.left -= i;
		localRect.right += i;
		localRect.bottom += i;
		localRect.top -= i;
		boolean bool = localRect.contains(paramInt1, paramInt2);
		localRect.left += i;
		localRect.right -= i;
		localRect.bottom -= i;
		localRect.top += i;
		return bool;
	}

	private Point a(SingleMarkOverLay paramOverlayItem,
			Projection paramProjection, Point paramPoint) {
		Point localPoint = paramProjection.toPixels(
				paramOverlayItem.getPoint(), null);
		return new Point(paramPoint.x - localPoint.x, paramPoint.y
				- localPoint.y);
	}
}
