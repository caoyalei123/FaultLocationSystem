package com.liubo.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.liubo.MapMainActivity;
import com.liubo.map.MarkerClusterer.Style;

public class TextIconOverlay extends Overlay {
	private int markerCount;
	private Style style;
	private Paint paint;
	private GeoPoint position;
	protected OnClickListener onClicListener;
	private Drawable markIcon = null;
	/**
	 * TextIconOverlay
	 * 
	 * @class 此类表示地图上的一个覆盖物，该覆盖物由文字和图标组成，从Overlay继承。文字通常是数字（0-9）或字母（A-Z
	 *        ），而文字与图标之间有一定的映射关系。
	 *        该覆盖物适用于以下类似的场景：需要在地图上添加一系列覆盖物，这些覆盖物之间用不同的图标和文字来区分
	 *        ，文字可能表示了该覆盖物的某一属性值，根据该文字和一定的映射关系，自动匹配相应颜色和大小的图标。
	 * 
	 * @constructor
	 * @param {Point} position 表示一个经纬度坐标位置。
	 */
	public TextIconOverlay(GeoPoint position,  int markerCount, Style style) {
		this.position = position;
		this.setStyle(style);
		this.setMarkerCount(markerCount);
		this.paint = new Paint();
	}
	@Override
	public  void draw(Canvas canvas, MapView mapView, boolean shadow) {
	      Point localPoint = mapView.getProjection().toPixels(position, null);
	      int m = mapView.getLeft();
	      int n = mapView.getRight();
	      int i1 = mapView.getTop();
	      int i2 = mapView.getBottom();
	      localPoint.x += m;
	      localPoint.y += i1;
	      if ((localPoint.x < m) || (localPoint.y < i1) || (localPoint.x > n) || (localPoint.y > i2)) {
	        return ;
	      }
	      //重新取下位置
	      mapView.getProjection().toPixels(position, localPoint);
	      drawIcon(canvas, markIcon, localPoint.x, localPoint.y);
	      draw(canvas, mapView,localPoint.x, localPoint.y);
	}
	  static void drawIcon(Canvas paramCanvas, Drawable paramDrawable, int paramInt1, int paramInt2)
	  {
	    Rect localRect = paramDrawable.getBounds();
	    if (localRect.width() == 0)
	      localRect.right = (localRect.left + paramDrawable.getIntrinsicWidth());
	    if (localRect.height() == 0)
	      localRect.bottom = (localRect.top + paramDrawable.getIntrinsicHeight());
	    paramDrawable.setBounds(localRect.left + paramInt1, localRect.top + paramInt2, localRect.right + paramInt1, localRect.bottom + paramInt2);
	    paramDrawable.draw(paramCanvas);
	    paramDrawable.setBounds(localRect.left - paramInt1, localRect.top - paramInt2, localRect.right - paramInt1, localRect.bottom - paramInt2);
	  }
		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			boolean bool = false;
			Projection localProjection = mapView.getProjection();
			Point localPoint1 = localProjection.toPixels(p, null);
			double d1 = Double.MAX_VALUE;
			double d2 = -1.0D;
			Point localPoint2 = a(localProjection, localPoint1);
			Drawable localDrawable = markIcon;
			if (hitTest(localDrawable, localPoint2.x, localPoint2.y)) {
				d2 = localPoint2.x * localPoint2.x + localPoint2.y * localPoint2.y;
			}
			if ((d2 < 0.0D) || (d2 >= d1)) {
				bool = false;
			} else {
				bool = true;
				if (onClicListener != null) {
					onClicListener.onClick();
				}
			}
			return bool;
		}

		protected boolean hitTest(Drawable paramDrawable, int paramInt1, int paramInt2) {
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

		private Point a(Projection paramProjection, Point paramPoint) {
			Point localPoint = paramProjection.toPixels(position, null);
			return new Point(paramPoint.x - localPoint.x, paramPoint.y - localPoint.y);
		}

		public void setOnclickListener(OnClickListener onClickListener) {
			this.onClicListener = onClickListener;
		}
	protected void draw(Canvas canvas, MapView mapView, int x, int y) {
	      paint.setTextSize(this.style.textSize);
	      paint.setColor(style.color);
	      paint.setTextAlign(Align.CENTER);
	      canvas.drawText(String.valueOf(this.markerCount), x,  y - markIcon.getIntrinsicHeight() / 2+ style.textSize / 2, paint);
	}
	/**
	 * 设置marker数量
	 * 
	 * @param size
	 */
	public void setMarkerCount(int size) {
		this.markerCount = size;
	}
	public void setStyle(Style style) {
		this.style = style;
		this.markIcon = MapMainActivity.getDrawableByID(style.drawableId);
	}
	public void setPosition(GeoPoint _center) {
		this.position = _center;	
	}
	public interface OnClickListener {
		public void onClick();
	}
}
