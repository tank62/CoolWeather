package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.R;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVICE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	private int currentLevel;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0,View view, int index,long arg3){
				if(currentLevel == LEVEL_PROVICE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}else if(currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(index).getPinyin();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}

	private void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVICE;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(Integer.parseInt(selectedProvince.getProvinceCode()));
		if(cityList.size()>0){
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	private void queryCounties(){
		countyList = coolWeatherDB.loadCounties(Integer.parseInt(selectedCity.getCityCode()));
		if(countyList.size()>0){
			dataList.clear();
			for(County county : countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	private void queryFromServer(final String code, final String type){
		
		String apiAddress = "http://apis.baidu.com/apistore/weatherservice/weatherl";
		String provinceAddress = "http://www.tlkfhjcxjsytb.com/province.txt";
		String cityAddress = "http://www.tlkfhjcxjsytb.com/city.txt";
		String countyAddress = "http://www.tlkfhjcxjsytb.com/county.txt";
		showProgressDialog();
		if("province".equals(type)){
		HttpUtil.sendHttpRequest(provinceAddress, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				if(result){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
								queryProvinces();
						};
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new  Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
	}else if("city".equals(type)){
		HttpUtil.sendHttpRequest(cityAddress, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				if(result){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
							queryCities();
						};
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new  Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
	}else if("county".equals(type)){
		HttpUtil.sendHttpRequest(countyAddress, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				if(result){
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							closeProgressDialog();
							queryCounties();
						};
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new  Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
	}}
	
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed(){
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else {
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
