package com.example.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.coolweather.R;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherinfoLayout;

	private TextView cityNameText;

	private TextView publishText;

	private TextView weatherDespText;

	private TextView temp1Text;

	private TextView temp2Text;

	private TextView currentDateText;

	private Button switchCity;

	private Button refresh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		weatherinfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button)findViewById(R.id.switch_city);
		refresh = (Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refresh.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中...");
			weatherinfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeather(countyCode);
		} else {
			showWeather();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
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

	private void queryWeather(String countyCode) {
		String address = "http://apis.baidu.com/apistore/weatherservice/cityid?cityid=";
		address = address.concat(countyCode);
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(WeatherActivity.this, response);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						showWeather();
					}
				});
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new  Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
				
			}
		});
		
	}

	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name",""));
		temp1Text.setText(prefs.getString("temp1",""));
		temp2Text.setText(prefs.getString("temp2",""));
		publishText.setText("今天"+prefs.getString("publish_time","")+" 发布");
		weatherDespText.setText(prefs.getString("weather_desp",""));
		currentDateText.setText(prefs.getString("current_date",""));
		weatherinfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city :
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weatheractivity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather :
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String countyCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(countyCode)) {
				queryWeather(countyCode);
			}
			break;
			default:
				break;
		}
	}
}
