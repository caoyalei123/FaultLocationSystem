package com.liubo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.liubo.db.TowerDao;
import com.liubo.exception.TowerNotUniqueException;
import com.liubo.exception.ZeroNumerException;
import com.liubo.map.Constant;
import com.liubo.modal.Tower;

public class NewTowerActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tower);
		Button button = (Button) findViewById(R.id.saveBtn);
		final TowerDao towerDao = new TowerDao(this.getApplicationContext());
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText _towerNum = (EditText) findViewById(R.id.tower_num);
				String towerNum = _towerNum.getText().toString().trim();
				EditText _circuitNum = (EditText) findViewById(R.id.circuit_num);
				String circuitNum = _circuitNum.getText().toString().trim();
				EditText _stationNum = (EditText) findViewById(R.id.station_num);
				String stationNum = _stationNum.getText().toString().trim();
				EditText _preTowerNum = (EditText) findViewById(R.id.pre_tower);
				String preTowerNum = _preTowerNum.getText().toString().trim();
				EditText _longitude = (EditText) findViewById(R.id.longitude);
				String longitude = _longitude.getText().toString().trim();
				EditText _latitude = (EditText) findViewById(R.id.latitude);
				String latitude = _latitude.getText().toString().trim();
				EditText _sim = (EditText) findViewById(R.id.sim);
				String sim = _sim.getText().toString().trim();
				Tower tower = new Tower();
				tower.setTowerNum(towerNum);
				tower.setCircuit(circuitNum);
				tower.setStationNum(stationNum);
				tower.setPreTowerNum(preTowerNum);
				tower.setLongitude(longitude);//经度
				tower.setLatitude(latitude);
				tower.setSimNum(sim);
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
				Intent intent = new Intent();
				intent.putExtra(Constant.Common.TOWER_NUM, towerNum);
				intent.setClass(NewTowerActivity.this,
						TowerInfoActivity.class);
				startActivity(intent);
				NewTowerActivity.this.finish();
			}
		});
	}
}
