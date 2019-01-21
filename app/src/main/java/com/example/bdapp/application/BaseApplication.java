package com.example.bdapp.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * 项目名     SmartBulter
 * 包名      online.icording.smartbulter.application
 * 创建时间   2018/4/14 0014 11:50
 * 创建者    yangtaoyao
 * 描述
 **/
public class BaseApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        //SDKInitializer.initialize(this);
    }
}
