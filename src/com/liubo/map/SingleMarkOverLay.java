package com.liubo.map;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.OverlayItem;
import com.liubo.modal.Tower;
public abstract class SingleMarkOverLay extends OverlayItem {
	public final Tower tower;
	public SingleMarkOverLay(GeoPoint geoPoint, Tower tower) {
		super(geoPoint, tower.getTowerNum(), "");
		this.tower = tower;
	}
	public interface OnClickListener {
		public void onClick();
	}
}
