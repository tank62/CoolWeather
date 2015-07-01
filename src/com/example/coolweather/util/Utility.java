package com.example.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

public class Utility {

	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces != null && allProvinces.length >0){
				for(String p : allProvinces){
					String[] arrary = p.split(" ");
					Province province = new Province();
					province.setProvinceCode(arrary[0]);
					province.setProvinceName(arrary[1]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities = response.split(",");
			if(allCities != null && allCities.length >0){
				for(String p : allCities){
					String[] arrary = p.split(" ");
					City city = new City();
					city.setCityCode(arrary[0]);
					city.setCityName(arrary[1]);
					city.setPinyin(arrary[2]);
					city.setProvinceId(Integer.parseInt(arrary[3]));
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounities = response.split(",");
			if(allCounities != null && allCounities.length >0){
				for(String p : allCounities){
					String[] arrary = p.split(" ");
					County county = new County();
					county.setCountycode(arrary[0]);
					county.setCountyName(arrary[1]);
					county.setPinyin(arrary[2]);
					county.setCityId(Integer.parseInt(arrary[3]));
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	public static void handleWeatherResponse(Context context, String response){
		try {
			JSONObject jsonobject = new JSONObject(response);
			int  status =   jsonobject.getInt("errNum");
			if(status==0){
				JSONObject weatherinfo =  jsonobject.getJSONObject("retData");
				String cityName = weatherinfo.getString("city");
				String citycode = weatherinfo.getString("citycode");
				String temp1 = weatherinfo.getString("l_tmp");
				String temp2 = weatherinfo.getString("h_tmp");
				String weatherDesp = weatherinfo.getString("weather");
				String publishTime = weatherinfo.getString("time");
				String cityPinyin = weatherinfo.getString("pinyin");
				saveWeatherInfo(context,cityName,citycode,temp1,temp2,weatherDesp,publishTime,cityPinyin);
			}else{
				String message =  (String) jsonobject.get("errMsg");
				Log.e("handleWeatherResponse", message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void saveWeatherInfo(Context context, String cityName,
			String citycode, String temp1, String temp2, String weatherDesp,
			String publishTime,String pinyin) {
		
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d",Locale.CHINA);
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", citycode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.putString("city_pinyin", pinyin);
		editor.commit();
		
	}
}
