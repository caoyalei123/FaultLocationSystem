package com.liubo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liubo.db.TowerDao;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.exception.ZeroNumerException;
import com.liubo.map.Constant;
import com.liubo.modal.Tower;
import com.liubo.util.StringUtils;

public class EditTowerActivity extends Activity{
	String towerId = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_tower);
		// 东西经的选择
		Spinner longdirectionSpinner = (Spinner) this.findViewById(R.id.long_direction);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.longitude_direction, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		longdirectionSpinner.setAdapter(adapter2);
		longdirectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ConvertParam.longitudeDirection = ConvertParam.LongitudeDirection.values()[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		// 南北维度的选择
		Spinner latitudedirectionSpinner = (Spinner) this.findViewById(R.id.lat_direction);
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.latitude_direction, android.R.layout.simple_spinner_item);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		latitudedirectionSpinner.setAdapter(adapter3);
		latitudedirectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ConvertParam.latitudeDirection = ConvertParam.LatitudeDirection.values()[position];

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		Intent intent = this.getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				towerId = bundle.getString(Constant.Common.TOWER_ID);
				if (StringUtils.isNotEmpty(towerId)) {
					TowerDao towerDao = new TowerDao(this);
					Tower tower = towerDao.getTowerById(towerId);
					towerDao.close();
					TextView _editTowerId = (TextView) findViewById(R.id.edit_tower_id);
					EditText _towerNum = (EditText) findViewById(R.id.tower_num);
					EditText _circuitNum = (EditText) findViewById(R.id.circuit_num);
					EditText _stationNum = (EditText) findViewById(R.id.station_num);
					EditText _preTower = (EditText) findViewById(R.id.pre_tower);
					EditText _sim = (EditText) findViewById(R.id.sim);
					_towerNum.setEnabled(false);
					_towerNum.setFocusable(false);
					// 经纬度的设置
					// 经度
					EditText longDegree = ((EditText) (this.findViewById(R.id.long_degree)));
					EditText longMinute = ((EditText) (this.findViewById(R.id.long_minute)));
					EditText longSecond = ((EditText) (this.findViewById(R.id.long_second)));
					String[] longArray = tower.getLongitudeArray();
					if (longArray.length > 0) {
						int index = 0;
						for (String str : this.getResources().getStringArray(R.array.longitude_direction)) {
							if (str.equals(longArray[0])) {
								longdirectionSpinner.setSelection(index);
								break;
							}
							index++;
						}
						longDegree.setText(longArray[1]);
						longMinute.setText(longArray[2]);
						longSecond.setText(longArray[3]);
					}

					// 纬度
					EditText latDegree = ((EditText) (this.findViewById(R.id.lat_degree)));
					EditText latMinute = ((EditText) (this.findViewById(R.id.lat_minute)));
					EditText latSecond = ((EditText) (this.findViewById(R.id.lat_second)));

					String[] latArray = tower.getLatitudeArray();
					if (latArray.length > 0) {
						int index = 0;
						for (String str : this.getResources().getStringArray(R.array.latitude_direction)) {
							if (str.equals(latArray[0])) {
								latitudedirectionSpinner.setSelection(index);
								break;
							}
							index++;
						}
						latDegree.setText(latArray[1]);
						latMinute.setText(latArray[2]);
						latSecond.setText(latArray[3]);
					}
					
					_editTowerId.setText(tower.getId());
					_towerNum.setText(tower.getTowerNum());
					_circuitNum.setText(tower.getCircuit());
					_stationNum.setText(tower.getStationNum());
					_preTower.setText(tower.getPreTowerNum());
					_sim.setText(tower.getSimNum());
				}
			}
		}
		Button button = (Button) findViewById(R.id.updateBtn);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView _editTowerId = (TextView) findViewById(R.id.edit_tower_id);
				EditText _towerNum = (EditText) findViewById(R.id.tower_num);
				if (_towerNum.getText().toString().trim().length() <= 0) {
					Toast.makeText(getApplicationContext(), "杆塔号不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				EditText _circuitNum = (EditText) findViewById(R.id.circuit_num);
				EditText _stationNum = (EditText) findViewById(R.id.station_num);
				EditText _preTower = (EditText) findViewById(R.id.pre_tower);
				EditText _sim = (EditText) findViewById(R.id.sim);
				EditText _comment = (EditText) findViewById(R.id.comment);

				Tower tower = new Tower();
				tower.setId(_editTowerId.getText().toString());
				tower.setTowerNum(_towerNum.getText().toString());
				tower.setCircuit(_circuitNum.getText().toString().trim());
				tower.setStationNum(_stationNum.getText().toString().trim());
				tower.setPreTowerNum(_preTower.getText().toString().trim());
				tower.setSimNum(_sim.getText().toString().trim());
				tower.setComment(_comment.getText().toString().trim());
				
				//验证
				if(tower.getTowerNum() == null || tower.getTowerNum().length() != 4){
					Toast.makeText(EditTowerActivity.this, "杆塔号长度必须为4位数字！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(tower.getCircuit() == null || tower.getCircuit().length() != 4){
					Toast.makeText(EditTowerActivity.this, "线路号不能为空且长度必须为4位数字！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(tower.getStationNum() == null || tower.getStationNum().length() != 4){
					Toast.makeText(EditTowerActivity.this, "变电站号不能为空且长度必须为4位数字！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(tower.getPreTowerNum() == null || tower.getPreTowerNum().length() != 4){
					Toast.makeText(EditTowerActivity.this, "父杆塔号不能为空且长度必须为4位数字！", Toast.LENGTH_SHORT).show();
					return;
				}
				
				
				// 经度
				Spinner longdirectionSpinner = (Spinner) findViewById(R.id.long_direction);
				EditText longDegree = ((EditText) (findViewById(R.id.long_degree)));
				EditText longMinute = ((EditText) (findViewById(R.id.long_minute)));
				EditText longSecond = ((EditText) (findViewById(R.id.long_second)));
				String longitude = getResources().getStringArray(R.array.longitude_direction)[longdirectionSpinner.getSelectedItemPosition()];
				if(longDegree.getText() == null || !StringUtils.isNotEmpty(longDegree.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "经度的度数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(longMinute.getText() == null || !StringUtils.isNotEmpty(longMinute.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "经度的分数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(longSecond.getText() == null || !StringUtils.isNotEmpty(longSecond.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "经度的秒数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				String longDegreeStr = longDegree.getText().toString().trim();
				if (longDegreeStr.length() > 0) {
					if (Integer.parseInt(longDegreeStr) > 180) {
						Toast.makeText(EditTowerActivity.this, "经度的度数为" + longDegreeStr + "大于180", Toast.LENGTH_SHORT).show();
						return;
					}

				}
				longitude += "," + longDegreeStr;
				// 分
				String longMinuteStr = longMinute.getText().toString().trim();
				if (longMinuteStr.length() > 0) {
					if (Integer.parseInt(longMinuteStr) > 60) {
						Toast.makeText(EditTowerActivity.this, "经度的分数为" + longMinuteStr + "大于60", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				longitude += "," + longMinuteStr;
				// 秒
				String longSecondStr = longSecond.getText().toString().trim();
				if (longSecondStr.length() > 0) {
					float sencond = (float) ((Integer.parseInt(longSecondStr) * 60.00f) / (Math.pow(10, longSecondStr.length())));
					if (sencond > 60) {
						Toast.makeText(EditTowerActivity.this, "经度的秒数为" + sencond + "大于60", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				longitude += "," + longSecondStr;
				if(StringUtils.isNotEmpty(longDegreeStr) && StringUtils.isNotEmpty(longMinuteStr) && StringUtils.isNotEmpty(longSecondStr)){
					tower.setLongitude(longitude);
				}
				// 纬度
				Spinner latitudedirectionSpinner = (Spinner) findViewById(R.id.lat_direction);
				EditText latDegree = ((EditText) (findViewById(R.id.lat_degree)));
				EditText latMinute = ((EditText) (findViewById(R.id.lat_minute)));
				EditText latSecond = ((EditText) (findViewById(R.id.lat_second)));

				if(latDegree.getText() == null || !StringUtils.isNotEmpty(latDegree.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "纬度的度数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(latMinute.getText() == null || !StringUtils.isNotEmpty(latMinute.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "纬度的分数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if(latSecond.getText() == null || !StringUtils.isNotEmpty(latSecond.getText().toString())){
					Toast.makeText(EditTowerActivity.this, "纬度的秒数不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				String latitude = getResources().getStringArray(R.array.latitude_direction)[latitudedirectionSpinner.getSelectedItemPosition()];
				String latDegreeStr = latDegree.getText().toString().trim();
				if (latDegreeStr.length() > 0) {
					if (Integer.parseInt(latDegreeStr) > 180) {
						Toast.makeText(EditTowerActivity.this, "纬度的度数为" + latDegreeStr + "大于180", Toast.LENGTH_SHORT).show();
						return;
					}

				}
				latitude += "," + latDegreeStr;
				// 分
				String latMinuteStr = latMinute.getText().toString().trim();
				if (latMinuteStr.length() > 0) {
					if (Integer.parseInt(latMinuteStr) > 60) {
						Toast.makeText(EditTowerActivity.this, "纬度的分数为" + latMinuteStr + "大于60", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				latitude += "," + latMinuteStr;
				// 秒
				String latSecondStr = latSecond.getText().toString().trim();
				if (latSecondStr.length() > 0) {
					float second = (float) (Integer.parseInt(latSecondStr) * 60.00f / (Math.pow(10, latSecondStr.length())));
					if (second > 60) {
						Toast.makeText(EditTowerActivity.this, "纬度的秒数为" + latSecondStr + "大于60", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				latitude += "," + latSecondStr;
				if(StringUtils.isNotEmpty(latDegreeStr) && StringUtils.isNotEmpty(latMinuteStr) && StringUtils.isNotEmpty(latSecondStr)){
					tower.setLatitude(latitude);
				}
				
				if(tower.getSimNum() == null || tower.getSimNum().length() != 11 ){
					Toast.makeText(EditTowerActivity.this, "手机号不能为空且长度必须为11位数字！", Toast.LENGTH_SHORT).show();
					return;
				}

				TowerDao towerDao = new TowerDao(EditTowerActivity.this.getApplicationContext());
				if (StringUtils.isNotEmpty(tower.getId())) {
					try {
						towerDao.update(tower);
					} catch (TowerNotUniqueException e) {
						Toast.makeText(getApplicationContext(), "杆塔号、线路号、变电站号冲突。", Toast.LENGTH_SHORT).show();
						return;
					} catch (ZeroNumerException e) {
						Toast.makeText(getApplicationContext(), "杆塔号不能为0000。", Toast.LENGTH_SHORT).show();
						return;
					}finally{
						towerDao.close();
					}
				} else {
					try {
						towerDao.save(tower);
					} catch (TowerNotUniqueException e) {
						Toast.makeText(getApplicationContext(), "杆塔号、线路号、变电站号冲突。", Toast.LENGTH_SHORT).show();
						return;
					} catch (ZeroNumerException e) {
						Toast.makeText(getApplicationContext(), "杆塔号不能为0000。", Toast.LENGTH_SHORT).show();
						return;
					}finally{
						towerDao.close();
					}
				}
				Intent intent = new Intent();
				TowerDao towerDao2 = new TowerDao(EditTowerActivity.this.getApplicationContext());
				try {
					tower = towerDao2.getTower(tower.getTowerNum(), tower.getCircuit(), tower.getStationNum());
				} catch (TowerNotUniqueException e) {
					e.printStackTrace();
					return;
				}finally{
					towerDao2.close();
				}
				intent.putExtra(Constant.Common.TOWER_ID, tower.getId());
				intent.setClass(EditTowerActivity.this, TowerInfoActivity.class);
				startActivity(intent);
				EditTowerActivity.this.finish();
			}
		});


	}
	private static class ConvertParam {
		enum ConverType {
			Wgs84ToBaidu, GcjToBaidu;
		}

		static ConverType convertType;
		static LongitudeDirection longitudeDirection;

		enum LongitudeDirection {
			East, West;
		}

		static LatitudeDirection latitudeDirection;

		enum LatitudeDirection {
			North, South;
		}
	}
	
	/*自定义对话框*/
    private void showCustomDia()
    {
        AlertDialog.Builder customDia=new AlertDialog.Builder(EditTowerActivity.this);
        final View viewDia=LayoutInflater.from(EditTowerActivity.this).inflate(R.layout.custom_dialog, null);
        customDia.setTitle("自定义对话框");
        customDia.setView(viewDia);
        customDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        customDia.create().show();
    }
    
}
