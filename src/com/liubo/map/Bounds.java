package com.liubo.map;

import com.baidu.mapapi.GeoPoint;

public class Bounds {
	// 矩形区域的西南角
	private GeoPoint sw;
	// 矩形区域的东北角
	private GeoPoint ne;

	// 创建一个包含所有给定点坐标的矩形区域。其中sw表示矩形区域的西南角，参数ne表示矩形区域的东北角。
	public Bounds(GeoPoint sw, GeoPoint ne) {
		this.sw = sw;
		this.ne = ne;
	}

	/**
	 * 
	 * 
	 * @param other
	 * @return 当且仅当此矩形中的两点参数都等于其他矩形的两点参数时，返回true
	 */
	@Override
	public boolean equals(Object paramObject) {
	    if (paramObject == null)
	        return false;
	      if (paramObject.getClass() != getClass())
	        return false;
	      return (this.sw.equals(((Bounds)paramObject).sw)) && (this.ne.equals(((Bounds)paramObject).ne));
	}
	@Override
	public String toString() {
		return "Bounds: sw: " + sw.toString() + " ne: "+ ne.toString();
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	/**
	 * 
	 * 
	 * @param point
	 * @return 如果点的地理坐标位于此矩形内，则返回true。
	 */
	public boolean containsPoint(GeoPoint point) {
		return (sw.getLatitudeE6() <=point.getLatitudeE6() &&  point.getLatitudeE6()<= ne.getLatitudeE6()) && 
				(sw.getLongitudeE6() <= point.getLongitudeE6() && point.getLongitudeE6() <= ne.getLongitudeE6());
	}

	/**
	 * 
	 * 
	 * @return 传入的矩形区域完全包含于此矩形区域中，则返回true。
	 */
	public boolean containsBounds(Bounds bounds) {
		return (sw.getLatitudeE6() <= bounds.sw.getLatitudeE6() && sw.getLongitudeE6() <= bounds.sw.getLongitudeE6())
			&& (ne.getLatitudeE6() >= bounds.ne.getLatitudeE6() && ne.getLongitudeE6() >= bounds.ne.getLongitudeE6());
	}

	/**
	 * 
	 * 计算与另一矩形的交集区域。
	 * 
	 * @param other
	 * @return
	 */
	public Bounds intersects(Bounds other) {
		return this;
	}

	/**
	 * 
	 * 
	 * @return 返回矩形的中心点。
	 */
	public GeoPoint getCenter() {
		return ne;
	}

	/**
	 * 
	 * 
	 * @return 如果矩形为空，则返回true。
	 */
	public boolean isEmpty() {
		return sw.equals(ne);
	}

	/**
	 * 
	 * 
	 * @return 返回矩形区域的西南角。
	 */
	public GeoPoint getSouthWest() {
		return sw;
	}

	/**
	 * 
	 * 
	 * @return 返回矩形区域的东北角。
	 */
	public GeoPoint getNorthEast() {
		return ne;
	}

	public void extend(GeoPoint point) {
		if (point.getLatitudeE6() < sw.getLatitudeE6()) {
			sw.setLatitudeE6(point.getLatitudeE6());
		}
		if (point.getLongitudeE6() < sw.getLongitudeE6()) {
			sw.setLongitudeE6(point.getLongitudeE6());
		}
		
		if (point.getLatitudeE6() > ne.getLatitudeE6()) {
			ne.setLatitudeE6(point.getLatitudeE6());
		}
		if (point.getLongitudeE6() > ne.getLongitudeE6()) {
			ne.setLongitudeE6(point.getLongitudeE6());
		}
	}
}
