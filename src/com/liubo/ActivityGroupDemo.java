package com.liubo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.baidu.mapapi.GeoPoint;
import com.liubo.db.LocationDao;
import com.liubo.db.TowerDao;
import com.liubo.map.Constant;
import com.liubo.map.SingleMarkOverlayImp;
import com.liubo.modal.Location_;
import com.liubo.modal.Tower;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

public class ActivityGroupDemo extends ActivityGroup {

	private GridView gvTopBar;
	private SimpleAdapter topImgAdapter;
	public LinearLayout container;// 装载sub Activity的容器

	/** 顶部按钮图片 **/
	int[] topbar_image_array = { R.drawable.topimage_1, R.drawable.topimage_2,
			R.drawable.topimage_3, R.drawable.topimage_4 };
	String[] top_title_array = { "显示地图", "杆塔信息", "故障信息", "其他操作" };
	private final String COLUMN_NAME_1 = "topimageId";
	private final String COLUMN_NAME_2 = "toptitle";
	public static final int SWITCH_TAB = 0x000010;
	public static ActivityGroupDemo self;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SWITCH_TAB: {
				String activityStr = msg.getData().getString(Constant.Common.ACTIVITY_INDEX);
				int activityIndex = -1;
				if (activityStr != null) {
					activityIndex = Integer.parseInt(activityStr);
				}
				if (activityIndex != -1) {
					if (activityIndex == 0) {
						String towerId = msg.getData().getString(Constant.Common.TOWER_ID);
						Bundle mapBundle = new Bundle();
						mapBundle.putString(Constant.Common.TOWER_ID, towerId);
						SwitchActivity(activityIndex, mapBundle);
					} else {
						SwitchActivity(activityIndex);
					}
				}
			}
			break;
			}
			super.handleMessage(msg);
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group);
		self = this;
		gvTopBar = (GridView) this.findViewById(R.id.gvTopBar);
		gvTopBar.setNumColumns(topbar_image_array.length);// 设置每行列数
		gvTopBar.setSelector(new ColorDrawable(Color.TRANSPARENT));// 选中的时候为透明色
		gvTopBar.setGravity(Gravity.CENTER);// 位置居中
		gvTopBar.setVerticalSpacing(0);// 垂直间隔

//		int width = this.getWindowManager().getDefaultDisplay().getWidth()
//				/ topbar_image_array.length;
		List<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < topbar_image_array.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(COLUMN_NAME_1, topbar_image_array[i]);
			map.put(COLUMN_NAME_2, top_title_array[i]);
			listItem.add(map);
		}
		topImgAdapter = new SimpleAdapter(this.getBaseContext(), listItem,
				R.layout.toplist,
				new String[] { COLUMN_NAME_1, COLUMN_NAME_2 }, new int[] {
						R.id.topimage, R.id.toptitle });
		gvTopBar.setAdapter(topImgAdapter);// 设置菜单Adapter
		gvTopBar.setOnItemClickListener(new ItemClickEvent());// 项目点击事件
		gvTopBar.setSelector(R.drawable.bg);
		container = (LinearLayout) findViewById(R.id.Container);
		gvTopBar.setSelection(0);
		Intent intent = this.getIntent();
		
		int index = 0;
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String indexStr = bundle.getString(Constant.Common.ACTIVITY_INDEX);
				if (indexStr != null) {
					index = Integer.valueOf(indexStr.toString());
					Log.e("index", indexStr);
				}
				if (index == 0) {
					String towerId = bundle.getString(Constant.Common.TOWER_ID);
					Bundle mapBundle = new Bundle();
					mapBundle.putString(Constant.Common.TOWER_ID, towerId);
					Log.e("ActivityGroupDemo", towerId);
					SwitchActivity(index, mapBundle);
					return;
				}
			}
		}
		SwitchActivity(index);// 默认打开第0页
	}

	private void SwitchActivity(int index, Bundle bundle) {
		// topImgAdapter.setFocus(id);// 选中项获得高亮
		container.removeAllViews();// 必须先清除容器中所有的View
		Intent intent = null;
		switch (index) {
		case 0: // 地图信息
			intent = new Intent(ActivityGroupDemo.this, MapMainActivity.class);
			break;
		case 1: // 杆塔信息
			intent = new Intent(ActivityGroupDemo.this, TowerListAct.class);
			break;
		case 2:// 历史故障信息
			intent = new Intent(ActivityGroupDemo.this,
					AlertTowerListActivity.class);
			break;
		case 3:// 其他操作
			intent = new Intent(ActivityGroupDemo.this, DashboardActivity.class);
			break;
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		// Activity 转为 View
		Window subActivity = getLocalActivityManager().startActivity(
				"subActivity", intent);
		// 容器添加View
		container.addView(subActivity.getDecorView(), LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		
	}

	class ItemClickEvent implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SwitchActivity(arg2);
		}
	}

	/**
	 * 根据ID打开指定的Activity
	 * 
	 * @param id
	 *            GridView选中项的序号
	 */
	void SwitchActivity(int id) {
		this.SwitchActivity(id, null);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {

			this.getLocalActivityManager().getCurrentActivity()
					.openOptionsMenu();
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("确认退出？");// 标题
			// 设定按钮 标题 侦听
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							System.exit(0);
						}
					});

			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			builder.create().show();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

}
