package com.liubo.map;

import com.baidu.mapapi.GeoPoint;
import com.liubo.modal.Tower;

public class SingleMarkOverlayImp extends SingleMarkOverLay {

	public boolean isInCluster = false;
	private boolean isError = false;
	public SingleMarkOverlayImp(GeoPoint geoPoint, Tower tower) {
		super(geoPoint, tower);
	}
	public boolean isError() {
		return this.isError;
	}

	public void setIsError(boolean b) {
		this.isError = b;
		
	}
}
