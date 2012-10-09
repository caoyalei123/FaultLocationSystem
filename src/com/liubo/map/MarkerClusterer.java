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
 * @fileoverview MarkerClusterer标记聚合器用来解决加载大量点要素到地图上产生覆盖现象的问题，并提高性能。 主入口类是
 *               基于Baidu Map API 1.2。
 * 
 * @author Baidu Map Api Group
 * @version 1.2
 */
public class MarkerClusterer<Item extends SingleMarkOverlayImp> extends Overlay {

	// 地图的一个实例
	private MapView _map;
	// 要聚合的标记数组
	private ArrayList<Item> _markers;
	// 聚合计算时网格的像素大小，默认60
	private final int DEFAULT_GRID_SIZE = 60;
	private int _gridSize = DEFAULT_GRID_SIZE;
	// 最大的聚合级别，大于该级别就不进行相应的聚合
	private int maxZoom;
	// 最小的聚合数量，小于该数量的不能成为一个聚合，默认为2
	private int minClusterSize;
	// 聚合点的落脚位置是否是所有聚合在内点的平均值，默认为否，落脚在聚合内的第一个点
	private boolean isAverangeCenter;
	private ArrayList<Cluster> _clusters;
	private int _maxZoom;
	private int _minClusterSize;
	private boolean _isAverageCenter;

	/**
	 * 获取一个扩展的视图范围，把上下左右都扩大一样的像素值。
	 * 
	 * @param {Map} map BMap.Map的实例化对象
	 * @param {BMap.Bounds} bounds BMap.Bounds的实例化对象
	 * @param {Number} gridSize 要扩大的像素值
	 * 
	 * @return {BMap.Bounds} 返回扩大后的视图范围。
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
	 * 按照百度地图支持的世界范围对bounds进行边界处理
	 * 
	 * @param {BMap.Bounds} bounds BMap.Bounds的实例化对象
	 * 
	 * @return {BMap.Bounds} 返回不越界的视图范围
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
	 * 对单个值进行边界处理。
	 * 
	 * @param {Number} i 要处理的数值
	 * @param {Number} min 下边界值
	 * @param {Number} max 上边界值
	 * 
	 * @return {Number} 返回不越界的数值
	 */
	public int getRange(int i, int min, int max) {
		i = Math.max(i, min);
		i = Math.min(i, max);
		return i;
	}

	/**
	 * MarkerClusterer
	 * 
	 * @class 用来解决加载大量点要素到地图上产生覆盖现象的问题，并提高性能
	 * @constructor
	 * @param {Map} map 地图的一个实例。
	 * @param {Json Object} options 可选参数，可选项包括：<br />
	 *        markers {Array<Marker>} 要聚合的标记数组<br />
	 *        girdSize {Number} 聚合计算时网格的像素大小，默认60<br />
	 *        maxZoom {Number} 最大的聚合级别，大于该级别就不进行相应的聚合<br />
	 *        minClusterSize {Number} 最小的聚合数量，小于该数量的不能成为一个聚合，默认为2<br />
	 *        isAverangeCenter {Boolean} 聚合点的落脚位置是否是所有聚合在内点的平均值，默认为否，落脚在聚合内的第一个点<br />
	 *        styles {Array<IconStyle>} 自定义聚合后的图标风格，请参考TextIconOverlay类<br />
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
	 * 添加要聚合的标记数组。
	 * 
	 * @param {Array<Marker>} markers 要聚合的标记数组
	 * 
	 * @return 无返回值。
	 */
	public void addMarkers(ArrayList<Item> markers) {
		for (Item item : markers) {
			this._pushMarkerTo(item);
		}
		this._createClusters();
	}

	/**
	 * 把一个标记添加到要聚合的标记数组中
	 * 
	 * @param {BMap.Marker} marker 要添加的标记
	 * 
	 * @return 无返回值。
	 */
	public void _pushMarkerTo(Item marker) {
		int index = this._markers.indexOf(marker);
		if (index == -1) {
			marker.isInCluster = false;
			towerId2Marker.put(marker.tower.getStationNum() + marker.tower.getCircuit() + marker.tower.getTowerNum(), marker);
			this._markers.add(marker);// Marker拖放后enableDragging不做变化，忽略
		}
	}

	/**
	 * 添加一个聚合的标记。
	 * 
	 * @param {BMap.Marker} marker 要聚合的单个标记。
	 * @return 无返回值。
	 */
	public void addMarker(Item marker) {
		this._pushMarkerTo(marker);
		_createClusters();
	}

	/**
	 * 根据所给定的标记，创建聚合点
	 * 
	 * @return 无返回值
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
	 * 根据标记的位置，把它添加到最近的聚合中
	 * 
	 * @param {BMap.Marker} marker 要进行聚合的单个标记
	 * 
	 * @return 无返回值。
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
	 * 清除上一次的聚合的结果
	 * 
	 * @return 无返回值。
	 */
	public void _clearLastClusters() {
		for (Cluster cluster : _clusters) {
			cluster.remove();
		}
		this._clusters.clear();// 置空Cluster数组
		this._removeMarkersFromCluster();// 把Marker的cluster标记设为false
	}

	/**
	 * 清除某个聚合中的所有标记
	 * 
	 * @return 无返回值
	 */
	public void _removeMarkersFromCluster() {
		for (Item marker : _markers) {
			marker.isInCluster = false;
		}
	}

	/**
	 * 把所有的标记从地图上清除
	 * 
	 * @return 无返回值
	 */
	public void _removeMarkersFromMap() {
		for (Item marker : _markers) {
			marker.isInCluster = false;
			// this._map.getOverlays().remove(marker);
		}
	}

	/**
	 * 删除单个标记
	 * 
	 * @param {BMap.Marker} marker 需要被删除的marker
	 * 
	 * @return {Boolean} 删除成功返回true，否则返回false
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
	 * 删除单个标记
	 * 
	 * @param {BMap.Marker} marker 需要被删除的marker
	 * 
	 * @return {Boolean} 删除成功返回true，否则返回false
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
	 * 删除一组标记
	 * 
	 * @param {Array<BMap.Marker>} markers 需要被删除的marker数组
	 * 
	 * @return {Boolean} 删除成功返回true，否则返回false
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
	 * 从地图上彻底清除所有的标记
	 * 
	 * @return 无返回值
	 */
	public void clearMarkers() {
		this._clearLastClusters();
		this._removeMarkersFromMap();
		this._markers.clear();
	};

	/**
	 * 重新生成，比如改变了属性等
	 * 
	 * @return 无返回值
	 */
	public void _redraw() {
		this._clearLastClusters();
		this._createClusters();
	}

	/**
	 * 获取网格大小
	 * 
	 * @return {Number} 网格大小
	 */
	public int getGridSize() {
		return this._gridSize;
	};

	/**
	 * 设置网格大小
	 * 
	 * @param {Number} size 网格大小
	 * @return 无返回值
	 */
	public void setGridSize(int size) {
		this._gridSize = size;
		this._redraw();
	};

	/**
	 * 获取聚合的最大缩放级别。
	 * 
	 * @return {Number} 聚合的最大缩放级别。
	 */
	public int getMaxZoom() {
		return this._maxZoom;
	}

	/**
	 * 设置聚合的最大缩放级别
	 * 
	 * @param {Number} maxZoom 聚合的最大缩放级别
	 * @return 无返回值
	 */
	public void setMaxZoom(int maxZoom) {
		this._maxZoom = maxZoom;
		this._redraw();
	}

	/**
	 * 获取单个聚合的最小数量。
	 * 
	 * @return {Number} 单个聚合的最小数量。
	 */
	public int getMinClusterSize() {
		return this._minClusterSize;
	}

	/**
	 * 设置单个聚合的最小数量。
	 * 
	 * @param {Number} size 单个聚合的最小数量。
	 * @return 无返回值。
	 */
	public void setMinClusterSize(int size) {
		this._minClusterSize = size;
		this._redraw();
	};

	/**
	 * 获取单个聚合的落脚点是否是聚合内所有标记的平均中心。
	 * 
	 * @return {Boolean} true或false。
	 */
	public boolean isAverageCenter() {
		return this._isAverageCenter;
	}

	/**
	 * 获取聚合的Map实例。
	 * 
	 * @return {Map} Map的示例。
	 */
	public MapView getMap() {
		return this._map;
	};

	/**
	 * 获取所有的标记数组。
	 * 
	 * @return {Array<Marker>} 标记数组。
	 */
	public ArrayList<Item> getMarkers() {
		return this._markers;
	}

	/**
	 * 获取聚合的总数量。
	 * 
	 * @return {Number} 聚合的总数量。
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
		 * @class 表示一个聚合对象，该聚合，包含有N个标记，这N个标记组成的范围，并有予以显示在Map上的TextIconOverlay等。
		 * @constructor
		 * @param {MarkerClusterer} markerClusterer 一个标记聚合器示例。
		 */
		public Cluster(MarkerClusterer markerClusterer) {
			this._markerClusterer = markerClusterer;
			this._map = markerClusterer.getMap();
			this._minClusterSize = markerClusterer.getMinClusterSize();
			this._isAverageCenter = markerClusterer.isAverageCenter();
			this._center = null;// 落脚位置
			this._markers = new ArrayList<T>();// 这个Cluster中所包含的markers
			this._gridBounds = null;// 以中心点为准，向四边扩大gridSize个像素的范围，也即网格范围
			this._isReal = false; // 真的是个聚合

			this._clusterMarker = new TextIconOverlay(this._center,
					this._markers.size(), getStyle(this._markers));
		}

		/**
		 * 向该聚合添加一个标记。
		 * 
		 * @param {Marker} marker 要添加的标记。
		 * @return 无返回值。
		 */
		public boolean addMarker(T marker) {
			if (this.isMarkerInCluster(marker)) {
				return false;
			}// 也可用marker.isInCluster判断,外面判断OK，这里基本不会命中

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
				}// 计算新的Center
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
		 * 判断一个标记是否在该聚合中。
		 * 
		 * @param {Marker} marker 要判断的标记。
		 * @return {Boolean} true或false。
		 */
		public boolean isMarkerInCluster(T marker) {
			return _markers.contains(marker);
		}

		/**
		 * 判断一个标记是否在该聚合网格范围中。
		 * 
		 * @param {Marker} marker 要判断的标记。
		 * @return {Boolean} true或false。
		 */
		public boolean isMarkerInClusterBounds(T marker) {
			return this._gridBounds.containsPoint(marker.getPoint());
		}

		public boolean isReal() {
			return this._isReal;
		}

		/**
		 * 更新该聚合的网格范围。
		 * 
		 * @return 无返回值。
		 */
		public void updateGridBounds() {
			Bounds bounds = new Bounds(this._center, this._center);
			this._gridBounds = getExtendedBounds(this._map, bounds,
					this._markerClusterer.getGridSize());
		}

		/**
		 * 更新该聚合的显示样式，也即TextIconOverlay。
		 * 
		 * @return 无返回值。
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
		 * 删除该聚合。
		 * 
		 * @return 无返回值。
		 */
		public void remove() {
			_isReal = false;
			this._map.getOverlays().remove(_clusterMarker);
			_markers.clear();
		}

		/**
		 * 获取该聚合所包含的所有标记的最小外接矩形的范围。
		 * 
		 * @return {BMap.Bounds} 计算出的范围。
		 */
		public Bounds getBounds() {
			Bounds bounds = new Bounds(this._center, this._center);
			for (T marker : _markers) {
				bounds.extend(marker.getPoint());
			}
			return bounds;

		}

		/**
		 * 获取该聚合的落脚点。
		 * 
		 * @return {BMap.Point} 该聚合的落脚点。
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
		// 如果聚里面不包含有错误的覆盖物则用绿色的否则用黄色的
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
			builder.setTitle("确定\"" + singleMark.tower.getTowerNum() + "\"杆塔故障已经恢复？");
			builder.setMessage(MapMainActivity.getTowerInfo(singleMark.tower));
			builder.setPositiveButton("恢复正常状态", new OnClickListener() {

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
						Toast.makeText(MapMainActivity.self, "系统错误：杆塔信息冲突，请检查数据。", Toast.LENGTH_LONG).show();
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
			/*builder.setNeutralButton("告警信息", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MapMainActivity.self,
							AlertRecordActiviry.class);
					intent.putExtra(Constant.Common.TOWER_NUM,
							singleMark.tower.getTowerNum());
					MapMainActivity.self.startActivity(intent);
				}
			});*/
			builder.setNegativeButton("取消", null);
			builder.create().show();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(MapMainActivity.self);
			builder.setMessage(MapMainActivity.getTowerInfo(singleMark.tower));
			builder.setTitle("\"" + singleMark.tower.getTowerNum() + "\"杆塔信息");
			builder.setPositiveButton("历史故障信息", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(MapMainActivity.self, AlertRecordActiviry.class);
					intent.putExtra(Constant.Common.TOWER_ID,
							singleMark.tower.getId());
					MapMainActivity.self.startActivity(intent);
				}
			});
			builder.setNegativeButton("取消", null);
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
