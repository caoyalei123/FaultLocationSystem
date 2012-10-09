package com.liubo.util;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class MapUtil {
	public static Drawable boundCenterBottom(Drawable paramDrawable) {
		if ((paramDrawable == null))
			return null;
		Rect localRect1 = paramDrawable.getBounds();
		if ((localRect1.height() == 0) || (localRect1.width() == 0))
			paramDrawable.setBounds(0, 0, paramDrawable.getIntrinsicWidth(),
					paramDrawable.getIntrinsicHeight());
		Rect localRect2 = paramDrawable.getBounds();
		int i = localRect2.width() / 2;
		int j = -localRect2.height();
		int k = 0;
		paramDrawable.setBounds(-i, j, i, k);
		return paramDrawable;
	}
	public static Drawable getDrawableAndSetBounds(Context context, int drawableId){
		Drawable marker = context.getResources().getDrawable(drawableId);  //得到需要标在地图上的资源
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
				.getIntrinsicHeight());   //为maker定义位置和边界
		return marker;
	}
}
