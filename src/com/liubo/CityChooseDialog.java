package com.liubo;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.liubo.db.CityDao;
import com.liubo.db.ProvinceDao;
import com.liubo.modal.City;
import com.liubo.modal.Province;


public class CityChooseDialog extends Dialog {
	private MapMainActivity mapActivity;
	public CityChooseDialog(MapMainActivity context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.mapActivity = context;	
		this.setContentView(new DialogView(context));
        initSpinner1();
	}
	private Spinner spinner1 = null;
	private Spinner spinner2=null;
	private String province;
	private String provinceCode;
	private String city;
	private String cityCode ;
    public void initSpinner1(){
    	ProvinceDao provinceDao = new ProvinceDao(mapActivity);
    	List<CityChooseListItem> list = new ArrayList<CityChooseListItem>();
    	List<Province> provinces = provinceDao.getAll();
    	for (Province province : provinces) {
    		list.add(new CityChooseListItem(province.getName(), province.getCode()));
    	}
    	CityChooseAdapter myAdapter = new CityChooseAdapter(mapActivity,list);
	 	spinner1.setAdapter(myAdapter);
		spinner1.setOnItemSelectedListener(new SpinnerOnSelectedListener1());
	}
    public void initSpinner2(String pcode){
    	CityDao cityDao = new CityDao(mapActivity);
    	List<CityChooseListItem> list = new ArrayList<CityChooseListItem>();
    	List<City> cities = cityDao.getCitiesByPCode(pcode);
    	for (City city : cities) {
    		list.add(new CityChooseListItem(city.getName(), city.getCode()));
    	}
    	CityChooseAdapter myAdapter = new CityChooseAdapter(mapActivity,list);
	 	spinner2.setAdapter(myAdapter);
		spinner2.setOnItemSelectedListener(new SpinnerOnSelectedListener2());
	}
	class SpinnerOnSelectedListener1 implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				long id) {
			province=((CityChooseListItem) adapterView.getItemAtPosition(position)).getName();
			provinceCode =((CityChooseListItem) adapterView.getItemAtPosition(position)).getPcode();
			
			initSpinner2(provinceCode);
		}

		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
		}		
	}
	class SpinnerOnSelectedListener2 implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> adapterView, View view, int position,
				long id) {
			city=((CityChooseListItem) adapterView.getItemAtPosition(position)).getName();
			cityCode =((CityChooseListItem) adapterView.getItemAtPosition(position)).getPcode();
		}

		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
		}		
	}
	class DialogView extends LinearLayout {
		public static final String LOG_TAG = "DialogView";

		public DialogView(Context context) {
			super(context);
			this.setOrientation(VERTICAL);
			int left = 40;
			int right = 40;
			int top = 10;
			int bottom = 10;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					300, LayoutParams.WRAP_CONTENT);
			params.setMargins(left, right, 0, bottom);

			spinner1 = new Spinner(context);
			addView(spinner1, params);

			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					300, LayoutParams.WRAP_CONTENT);
			params2.setMargins(left, right, top, bottom);
			
			spinner2 = new Spinner(context);
			addView(spinner2, params2);
			spinner1.setPrompt("省");
			spinner2.setPrompt("城市");	
			LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
					300, LayoutParams.WRAP_CONTENT);
			params2.setMargins(left, right, top, 20);
			params3.gravity = Gravity.CENTER_HORIZONTAL;
			Button button = new Button(context);
			button.setText("前往");
			button.setTextSize(18);
	        button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Message msg =  new Message();
					msg.what = MapMainActivity.SET_CENTER;
					msg.getData().putString(MapMainActivity.CITY_CODE, cityCode);
					mapActivity.handler.sendMessage(msg);
					CityChooseDialog.this.dismiss();
				}
			});
	        this.addView(button, params3);
		}

	}
}