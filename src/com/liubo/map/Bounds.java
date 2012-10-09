package com.liubo.map;

import com.baidu.mapapi.GeoPoint;

public class Bounds {
	// ������������Ͻ�
	private GeoPoint sw;
	// ��������Ķ�����
	private GeoPoint ne;

	// ����һ���������и���������ľ�����������sw��ʾ������������Ͻǣ�����ne��ʾ��������Ķ����ǡ�
	public Bounds(GeoPoint sw, GeoPoint ne) {
		this.sw = sw;
		this.ne = ne;
	}

	/**
	 * 
	 * 
	 * @param other
	 * @return ���ҽ����˾����е���������������������ε��������ʱ������true
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
	 * @return �����ĵ�������λ�ڴ˾����ڣ��򷵻�true��
	 */
	public boolean containsPoint(GeoPoint point) {
		return (sw.getLatitudeE6() <=point.getLatitudeE6() &&  point.getLatitudeE6()<= ne.getLatitudeE6()) && 
				(sw.getLongitudeE6() <= point.getLongitudeE6() && point.getLongitudeE6() <= ne.getLongitudeE6());
	}

	/**
	 * 
	 * 
	 * @return ����ľ���������ȫ�����ڴ˾��������У��򷵻�true��
	 */
	public boolean containsBounds(Bounds bounds) {
		return (sw.getLatitudeE6() <= bounds.sw.getLatitudeE6() && sw.getLongitudeE6() <= bounds.sw.getLongitudeE6())
			&& (ne.getLatitudeE6() >= bounds.ne.getLatitudeE6() && ne.getLongitudeE6() >= bounds.ne.getLongitudeE6());
	}

	/**
	 * 
	 * ��������һ���εĽ�������
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
	 * @return ���ؾ��ε����ĵ㡣
	 */
	public GeoPoint getCenter() {
		return ne;
	}

	/**
	 * 
	 * 
	 * @return �������Ϊ�գ��򷵻�true��
	 */
	public boolean isEmpty() {
		return sw.equals(ne);
	}

	/**
	 * 
	 * 
	 * @return ���ؾ�����������Ͻǡ�
	 */
	public GeoPoint getSouthWest() {
		return sw;
	}

	/**
	 * 
	 * 
	 * @return ���ؾ�������Ķ����ǡ�
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
