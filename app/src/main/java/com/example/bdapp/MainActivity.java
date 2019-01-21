package com.example.bdapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn1;
    private Button btn2;
    private Button btn_desRadius;
    private Button btn_clearTs;
    private Button  btn_timerClear;
    private TextView tv_show;
    private TextView tv_gpscount;
    private TextView tv_gpscount30;
    private TextView tv_ts;
    private TextView tv_isEnter;
    private TextView tv_timerText;
    private TextView tv_isS;
    FloatingActionButton fab;
    Toolbar toolbar;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private EditText et_desLongitude;
    private EditText et_desLatitude;
    private EditText et_desRadius;

    LocationManager lm;
    Location lc;
    Marker marker = null;

    int gpscount = 0;
    StringBuilder ts = new StringBuilder();

    double desLongitude = 0;
    double desLatitude = 0;
    float desRadius = 50;     //定义半径，米
    Marker desMarker = null;

    /*****************计时器*******************/
    private Handler mhandle = new Handler();
    private boolean isPause = false;//是否暂停
    private long currentSecond = 0;//当前毫秒数
//    private Runnable timeRunable=new Runnable(){
//        @Override
//        public void run() {
//            currentSecond = currentSecond + 1000;
//            tv_timerText.setText(MainActivity.getFormatHMS(currentSecond));
//            if (!isPause) {
//                //递归调用本runable对象，实现每隔一秒一次执行任务
//                mhandle.postDelayed(this, 1000);
//                Log.i("当前毫秒数================",""+currentSecond);
//            }
//        }
//    };
    private Runnable timeRunable=null;
    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            currentSecond = currentSecond + 1000;
            tv_timerText.setText(MainActivity.getFormatHMS(currentSecond));
            mhandle.postDelayed(this, 1000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setBtn1();
    }

    private void initView() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
        btn_desRadius = findViewById(R.id.btn_desRadius);
        btn_desRadius.setOnClickListener(this);
        btn_clearTs = findViewById(R.id.btn_clearTs);
        btn_clearTs.setOnClickListener(this);
        btn_timerClear = findViewById(R.id.btn_timerClear);
        btn_timerClear.setOnClickListener(this);
        toolbar = findViewById(R.id.toolbar);
        tv_show = findViewById(R.id.tv_show);
        setSupportActionBar(toolbar);
        tv_gpscount = findViewById(R.id.tv_gpscount);
        tv_gpscount30 = findViewById(R.id.tv_gpscount30);
        tv_ts = findViewById(R.id.tv_ts);
        tv_ts.setText(ts);
        tv_isEnter = findViewById(R.id.tv_isEnter);
        tv_timerText=findViewById(R.id.tv_timerText);
        tv_isS=findViewById(R.id.tv_isS);
        et_desLongitude = findViewById(R.id.et_desLongitude);
        et_desLatitude = findViewById(R.id.et_desLatitude);
        et_desRadius = findViewById(R.id.et_desRadius);
        et_desLongitude.setText(desLongitude + "");
        et_desLatitude.setText(desLatitude + "");
        et_desRadius.setText(desRadius + "");

        mMapView = findViewById(R.id.bmapView);

        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);
        //开启交通图n
        mBaiduMap.setTrafficEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(26).build()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case R.id.btn1:
                desLongitude = Double.valueOf(et_desLongitude.getText().toString());
                desLatitude = Double.valueOf(et_desLatitude.getText().toString());
                desRadius = Float.valueOf(et_desRadius.getText().toString());     //定义半径，米
                setDesLocation(desLongitude, desLatitude, desRadius);
                Toast.makeText(MainActivity.this, "修改目的地成功", Toast.LENGTH_LONG).show();
                updateTv_ts("修改目的地成功！");
                break;
            case R.id.btn2:
                if (lc == null) {
                    Toast.makeText(MainActivity.this, "修改目的地失败！", Toast.LENGTH_LONG).show();
                    updateTv_ts("修改目的地失败！");
                    break;
                }
                desLongitude = lc.getLongitude();
                desLatitude = lc.getLatitude();
                setDesLocation(desLongitude, desLatitude, desRadius);
                Toast.makeText(MainActivity.this, "修改目的地成功！", Toast.LENGTH_LONG).show();
                updateTv_ts("修改目的地成功！");
                break;
            case R.id.btn_desRadius:
                desRadius = Float.valueOf(et_desRadius.getText().toString());
                setDesLocation(desLongitude, desLatitude, desRadius);
                Toast.makeText(MainActivity.this, "修改目的范围半径成功！", Toast.LENGTH_LONG).show();
                updateTv_ts("修改目的范围半径成功！");
                break;
            case R.id.btn_clearTs:
                ts = new StringBuilder();
                updateTv_ts("已清空.............");
                break;
            case R.id.btn_timerClear:
                //重置计时
                mhandle.removeCallbacks(timeRunable);
                timeRunable = null;
                currentSecond=0;
                Toast.makeText(MainActivity.this, "重置计时！", Toast.LENGTH_LONG).show();
                break;
        }
    }

    //设置定位
//    @SuppressLint("MissingPermission")
    public void setBtn1() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /******卫星数目******/
        //添加监听卫星
        GpsStatus.Listener statusListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    //第一次定位
                    updateTv_ts("第一次定位");
                } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    //卫星状态改变
                    @SuppressLint("MissingPermission")
                    GpsStatus gpsStauts = lm.getGpsStatus(null); // 取当前状态  
                    int maxSatellites = gpsStauts.getMaxSatellites(); //获取卫星颗数的默认最大值
                    Iterator<GpsSatellite> it = gpsStauts.getSatellites().iterator();//创建一个迭代器保存所有卫星
                    gpscount = 0;
                    int count = 0;//信噪比大于30
                    while (it.hasNext() && gpscount <= maxSatellites) {
                        GpsSatellite s = it.next();
                        gpscount++;
                        //可见卫星数量
//                        if(s.usedInFix()){
//                            //已定位卫星数量
//                            gpscount++;
//                        }
                        //卫星的信噪比
//                        if (s.getSnr() > 0) {
//                            updateTv_ts("第" + gpscount + "颗卫星的信噪比：" + s.getSnr());
//                        }
                        if (s.getSnr() > 20) {
                            count++;
                            updateTv_ts("第" + gpscount + "颗>20的卫星信噪比：" + s.getSnr());
                        }
                    }
//                    Log.e("卫星状态","搜索到：" + gpscount + "颗卫星  max :"+maxSatellites);
                    tv_gpscount.setText("搜索到" + gpscount + "颗卫星  max :" + maxSatellites);
                    tv_gpscount30.setText(count + "");
                    if (count >= 4) {
                        //表示有信号
                        //室内计时结束
                        mhandle.removeCallbacks(timeRunable);
                        timeRunable = null;
                        currentSecond=0;
                        tv_isS.setText("室外有信号");
                    } else {
                        //信号弱或无信号
                        //室内计时开始
                        if (timeRunable == null) {
                            timeRunable = new MyRunnable();
                            mhandle.postDelayed(timeRunable, 0);
                        }
                        tv_isS.setText("室内信号弱或无信号");
                    }
                } else if (event == GpsStatus.GPS_EVENT_STARTED) {
                    //定位启动  
                    Toast.makeText(MainActivity.this, "定位启动", Toast.LENGTH_LONG).show();
                    updateTv_ts("定位启动");
                } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    //定位结束  
                    Toast.makeText(MainActivity.this, "定位结束", Toast.LENGTH_LONG).show();
                    updateTv_ts("定位结束");
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             if (!isGpsAble(lm)) {
                Toast.makeText(MainActivity.this, "请打开GPS~", Toast.LENGTH_SHORT).show();
                openGPS2();
            }else{
                 return;
             }
        }
        lm.addGpsStatusListener(statusListener);
//        if (!isGpsAble(lm)) {
//            Toast.makeText(MainActivity.this, "请打开GPS~", Toast.LENGTH_SHORT).show();
//            openGPS2();
//        }

        //从GPS获取最近的定位信息
        lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateShow(lc);
        if(lc!=null){
            //临近警告(地理围栏)初始化
            setDesLocation(lc.getLongitude(),lc.getLatitude(),desRadius);
        }

        //设置间隔两秒获得一次GPS定位信息
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 8, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新定位
                updateShow(location);
                lc=location;
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            @SuppressLint("MissingPermission")
            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS LocationProvider可用时，更新定位
                updateShow(lm.getLastKnownLocation(provider));
            }
            @Override
            public void onProviderDisabled(String provider) {
                updateShow(null);
            }
        });
    }

    /*
     * 临近警告(地理围栏)
     */
    @SuppressLint("MissingPermission")
    public void setDesLocation(double longitude,double latitude,float radius){
        //定义固定点的经纬度
        Intent intent = new Intent(this, ProximityReceiver.class);
        // 将Intent包装成PendingIntent对象
        PendingIntent pi = PendingIntent.getBroadcast(this, -1, intent, 0);
        // 添加临近警告
        lm.addProximityAlert(latitude, longitude, radius, -1, pi);

        LatLng point=new LatLng(latitude,longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.des);
        OverlayOptions option=new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        if(desMarker!=null){
            desMarker.remove();
        }
        desMarker = (Marker)mBaiduMap.addOverlay(option);
    }

    /*
     * 更新数据显示
     */
    private void updateShow(Location location) {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("经度：" + location.getLongitude() + "\n");
            sb.append("纬度：" + location.getLatitude() + "\n");
            sb.append("高度：" + location.getAltitude() + "\n");
            sb.append("速度：" + location.getSpeed() + "\n");
            sb.append("方向：" + location.getBearing() + "\n");
            sb.append("定位精度：" + location.getAccuracy() + "\n");
            tv_show.setText(sb.toString());

            updateTv_ts("位置信息更新显示");
            SharedPreferences sp= getSharedPreferences("User", Context.MODE_PRIVATE);
            if(sp.getBoolean("isEnter", false)){
                tv_isEnter.setText("你已到达目的地附近");

            }else {
                tv_isEnter.setText("你已离开目的地附近");
            }

            //移动到我的位置
            //设置缩放
            MapStatusUpdate mapUpdate=MapStatusUpdateFactory.zoomTo(18);
            mBaiduMap.setMapStatus(mapUpdate);
            //移动
            MapStatusUpdate mapLatelng= (MapStatusUpdate) MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            mBaiduMap.setMapStatus(mapLatelng);
            //位置图标显示
            showLineMarker(location);

        } else {
            tv_show.setText("");
            updateTv_ts("获取位置信息失败！");
            Toast.makeText(MainActivity.this, "获取位置信息失败！", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * GPS设置
     */
    private boolean isGpsAble(LocationManager lm) {
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ? true : false;
    }
    //打开设置页面让用户自己设置
    private void openGPS2() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 0);

    }

    /**
     * 根据坐标点绘制Marker
     */
    private void showLineMarker(Location location) {
        //构建MarkerOption，用于在地图上添加Marker
        LatLng point=new LatLng(location.getLatitude(),location.getLongitude());
        //构建marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.dingwei);
        OverlayOptions option=new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        if(marker!=null){
            marker.remove();
        }
        marker = (Marker)mBaiduMap.addOverlay(option);
    }

    /*
     *更新tv_ts打印信息
     */
    private void updateTv_ts(String str){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式 new Date()为获取当前系统时间
        ts.append(df.format(new Date())+" "+str + "\n");
        tv_ts.setText(ts.toString());
    }

    /*
     * 生命周期
     */
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    /*
    *顶部菜单
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 根据毫秒返回时分秒
     * @param time
     * @return
     */
    public static String getFormatHMS(long time){
        time=time/1000;//总秒数
        int s= (int) (time%60);//秒
        int m= (int) (time/60);//分
        int h=(int) (time/3600);//秒
        return String.format("%02d:%02d:%02d",h,m,s);
    }
}
