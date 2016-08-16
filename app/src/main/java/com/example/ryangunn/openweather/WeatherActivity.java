package com.example.ryangunn.openweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class WeatherActivity extends AppCompatActivity {
    //Hard Coded Chicago Lat and Long
    private static final String CHICAGO_LAT = "41.85";
    private static final String CHICAGO_LON = "-87.65";
    Retrofit mRetrofit;
    OpenWeatherAPI mOpenWeatherAPI;
    ProgressDialog mProgressDialog;
    TextView mCityNameTextView;
    TextView mWeatherTextView;

    public interface OpenWeatherAPI{

        @GET("weather")
        Call<ResponseBody>getCurrentWeather(@Query("lat") String lat, @Query("lon") String lon, @Query("apiKey") String apiKey, @Query("units") String unit);
    }

    public static final String apiKey = "b3d34aa21fbec905163da3d45d27db66";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mRetrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        mOpenWeatherAPI = mRetrofit.create(OpenWeatherAPI.class);
        initViews();
        if (isConnected()) {
            displayProgressDialog();
            currentWeatherCall();
        }else {
            displayAlertDialog();
        }
    }


    private void initViews(){
        mCityNameTextView = (TextView) findViewById(R.id.cityNameTextView);
        mWeatherTextView = (TextView)findViewById(R.id.weatherTextView);
    }

    public void displayProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading Weather");
        mProgressDialog.setTitle("Open Weather");
        mProgressDialog.show();
    }


    public void currentWeatherCall(){
        Call<ResponseBody>  fiveDayCall= mOpenWeatherAPI.getCurrentWeather(CHICAGO_LAT,CHICAGO_LON,apiKey,"imperial");

        fiveDayCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                mProgressDialog.dismiss();
                try {
                    String json = response.body().string();
                    JSONObject jsonObj = new JSONObject(json);
                    String  temp = jsonObj.getJSONObject("main").getString("temp");
                    String cityName = jsonObj.getString("name");
                    displayWeatherDegree(mWeatherTextView,temp);
                    mCityNameTextView.setText(cityName);

                } catch (Exception e) {
                    e.printStackTrace();
                    displayAlertDialog();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mProgressDialog.dismiss();
                displayAlertDialog();
            }
        });

    }

    private void displayWeatherDegree(TextView degreeTextView,String degree){
        degreeTextView.setText(degree + "°F");
    }

    private void displayAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this).setMessage("Error Retriving Data")
                                    .setTitle("Open Weather");
        alertDialogBuilder.show();
    }

    private Boolean isConnected(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
