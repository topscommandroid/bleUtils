package com.example.xiaojin20135.bleutils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;

import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.adapter.BleDeviceAdapter;
import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;
import com.example.xiaojin20135.blelib.helps.BleConstant;

import java.util.ArrayList;
import java.util.List;

public class ScanListActivity extends AppCompatActivity {
    private static final String TAG = "ScanListActivity";
    private SwipeRefreshLayout swipe_refresh;
    private RecyclerView recycler;
    private LinearLayoutManager linearLayoutManager;
    private int lastVisibleItem;
    private List<MyBluetoothDevice> datas = new ArrayList<>();
    private BleDeviceAdapter bleDeviceAdapter;

    BleManager bleManager = BleManager.BLE_MANAGER;
    private String uuid_service = "0000fff0-0000-1000-8000-00805f9b34fb";
    private String uuid_write = "0000fff6-0000-1000-8000-00805f9b34fb";
    private String uuid_notification = "0000fff4-0000-1000-8000-00805f9b34fb";
    private String uuid_confirm = "0000fff3-0000-1000-8000-00805f9b34fb";
    private String uuid_notification_des2 = "00002902-0000-1000-8000-00805f9b34fb";
    private Handler handler; //扫描到新设备通知
    private Handler conChangeHandler;//连接状态发生吧变化通知


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_list);
        initView();
        initEvents();
    }

    private void initView(){
        bleManager.init(uuid_service,uuid_write,uuid_notification,uuid_confirm,uuid_notification_des2,this);
        initHandler();
        swipe_refresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        recycler = (RecyclerView)findViewById(R.id.recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(linearLayoutManager);
        bleDeviceAdapter = new BleDeviceAdapter(this,datas,recycler,bleManager);
        recycler.setAdapter(bleDeviceAdapter);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary,R.color.colorAccent,R.color.colorPrimaryDark);
        swipe_refresh.setProgressViewOffset(false,0,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
    }

    private void initEvents(){
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bleDeviceAdapter.clearAll();
                bleDeviceAdapter.notifyDataSetChanged();
                swipe_refresh.setRefreshing(false);
                bleManager.startScan();
                Log.d(TAG,"清理数据，重新扫描");
            }
        });
    }
    private void initHandler(){
        ///扫描到新设备
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "msg.what = " + msg.what);
                super.handleMessage(msg);
                if(msg.what == BleConstant.SCANNEWDEVICE){//发现新设备
                    datas = bleManager.getDevicesList();
                    Log.d(TAG,"deviceList = " + datas.toString());
                    MyBluetoothDevice myBluetoothDevice = datas.get(datas.size()-1);
                    Log.d(TAG,"bluetoothDevice.getAddress() = " + myBluetoothDevice.toString());
                    bleDeviceAdapter.clearAll();
                    bleDeviceAdapter.addDatas(datas);
                    bleDeviceAdapter.notifyDataSetChanged();
                }else if(msg.what == BleConstant.STARTCONNECT){
                    Log.d(TAG,"开始连接");
                }else if(msg.what == BleConstant.CONNECTED){
                    Log.d(TAG,"连接成功！");
                    bleManager.stopScan ();
                }else if(msg.what == BleConstant.CONNECTDONE){
                    Log.d(TAG,"认证完成，可跳转");
                    Intent intent = new Intent(ScanListActivity.this,SendTestActivity.class);
                    startActivity(intent);
                }else{
                    Log.d(TAG,"连接状态发生变化； state = " + msg.what);
                }
            }
        };
        bleManager.setBleHandler(handler);

    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction () == KeyEvent.ACTION_DOWN){
            bleManager.stopScan ();

        }
        return super.onKeyDown (keyCode, event);
    }
}
