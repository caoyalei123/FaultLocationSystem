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
	 * @class �����ʾ��ͼ�ϵ�һ��������ø����������ֺ�ͼ����ɣ���Overlay�̳С�����ͨ�������֣�0-9������ĸ��A-Z
	 *        ������������ͼ��֮����һ����ӳ���ϵ��
	 *        �ø������������������Ƶĳ�������Ҫ�ڵ�ͼ�����һϵ�и������Щ������֮���ò�ͬ��ͼ�������������
	 *        �����ֿ��ܱ�ʾ�˸ø������ĳһ����ֵ�����ݸ����ֺ�һ����ӳ���ϵ���Զ�ƥ����Ӧ��ɫ�ʹ�С��ͼ�ꡣ
	 * 
	 * @constructor
	 * @param {Point} position ��ʾһ����γ������λ�á�
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
	      //����ȡ��λ��
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
	 * ����marker����
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
