package com.littt.blelibrary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;


import com.littt.blelibrary.bean.BlueDeviceList;
import com.littt.blelibrary.callback.BleConnectCallback;
import com.littt.blelibrary.callback.BleNotifyCallback;
import com.littt.blelibrary.callback.BleScanCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BleManager {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    public String TAG = "ABC";
    public boolean bBlueSocketConnected = false;
    BleScanCallback bleScanCallback;
    BleConnectCallback bleConnectCallback;
    BleNotifyCallback bleNotifyCallback;
    private BluetoothDevice mDevice;
    final  String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    AndroidTool androidTool = new AndroidTool();
    public OutputStream outputStream;
    public InputStream inputStream;
    //单例模式，获取blemanager唯一
    public static BleManager getInstance() {
        return BleManagerHolder.sBleManager;
    }

    private static class BleManagerHolder {
        private static final BleManager sBleManager = new BleManager();
    }
    //初始化方法,在activity的oncreate()中调用，传入context
    public void init(Context app){
        if (context == null && app != null) {
            context = app;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity)app).startActivityForResult(enableBtIntent, 1);
            }
            BlueToothIntentFilter();
            new BlueToothRxThread().start();
        }

    }
    //读取返回的数据流
    public class BlueToothRxThread extends Thread {
        public void run() {
            while (true)
            {
                if(bBlueSocketConnected)
                {
                    if(inputStream!=null)
                    {
                        byte Rxbuf[]=new byte[2048];
                        try {
                            int len = inputStream.read(Rxbuf);//
                            if (len > 0)
                            {
                                String s=androidTool.ByteToStr(Rxbuf,len);
                                Log.v("ABC","JAVA-收到数据 len="+len+","+s);//+
                                //这里是蓝牙接收到的数据流
                                if (null!=bleNotifyCallback)
                                bleNotifyCallback.notifyCallback(Rxbuf,len);
                            }
                        }
                        catch (IOException e)
                        {

                        }
                    }
                }
            }
        }
    }


    //将需要发送到蓝牙设备的数据用byte数组的形式发送。
    public void OutputByte(byte[]Buff)
    {
        if (outputStream==null){
            Log.v("ABC","outputstream==null");
            return;
        }
        if (Buff==null){
            Log.v("ABC","Buff==null");
            return;
        }
        try {
            outputStream.write(Buff);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //开启通知后可以接收到，蓝牙端返回的数据
    public void startNotify(BleNotifyCallback callback){
        bleNotifyCallback=callback;
    }


    //注册蓝牙监听
    public void BlueToothIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String sName=device.getName();//DC:0D:30:00:08:7B
                String sAddr=device.getAddress();
                int rssi=intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                if(sName!=null)
                {
                     GetBlueToothData(sName,sAddr,rssi);
                }
            }
        }
    };
//    public void unInit(){
//        context.unregisterReceiver(receiver);
//        try {
//            bluetoothSocket.close();
//            bluetoothSocket=null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        bluetoothAdapter.cancelDiscovery();
//        bluetoothAdapter=null;
//        bBlueSocketConnected=false;
//        bleScanCallback=null;
//    }


    //开启搜索蓝牙，数据会通过callback陆续返回
    public void startDiscovery(BleScanCallback callback)
    {
        try {
            if(bluetoothSocket!=null)
                bluetoothSocket.close();
        } catch (IOException e) {

        }
        bBlueSocketConnected=false;
        if (null!=bluetoothAdapter){
            bleScanCallback=callback;
            bluetoothAdapter.startDiscovery();
        }
    }
    //将搜索返回的数据返回回去
    public void GetBlueToothData(String sName, String sAddress, int sig) {
        bleScanCallback.onScaning(new BlueDeviceList(sName,sAddress,sig));
    }
    //连接指定蓝牙，需要转入mac地址进行连接，传入callback监听连接返回
    public void connectBlueTooth(String sMac, BleConnectCallback callback){
        if (!bBlueSocketConnected){
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("是要连接蓝牙么？\r\n " + sMac)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDevice =bluetoothAdapter.getRemoteDevice(sMac);
                            try {
                                BlueToothSocketConnet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }else{
            Toast.makeText(context,"蓝牙已连接，必须先断开再连接！",Toast.LENGTH_SHORT).show();
        }
        bleConnectCallback=callback;
    }
    public  boolean  BlueToothSocketConnet() throws IOException {
        boolean b=false;
        bluetoothAdapter.cancelDiscovery();

        if(bluetoothSocket!=null)
        {
            if(bluetoothSocket.isConnected())
                bluetoothSocket.close();
        }
        try {
            bluetoothSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try {
            bluetoothSocket.connect();
            bBlueSocketConnected=true;
            outputStream=bluetoothSocket.getOutputStream();
            bleConnectCallback.onConnect("蓝牙连接成功！");
        }
        catch (IOException e2)
        {
            bleConnectCallback.onConnect("蓝牙连接失败！");
            e2.printStackTrace();
            try
            {
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return false;
        }
        try
        {
            inputStream = bluetoothSocket.getInputStream();
            b=true;
        } catch (IOException e3)
        {
            bleConnectCallback.onConnect("蓝牙连接失败！");
            e3.printStackTrace();
        }
        return b;
    }

    //断开蓝牙连接
    public int  disConnect(){
        int disConnectResult=0;
        if (bBlueSocketConnected){
            if (bluetoothSocket!=null){
                try {
                    bluetoothSocket.close();
                    bBlueSocketConnected=false;
                    inputStream=null;
                    disConnectResult=1;
                } catch (IOException e) {
                    e.printStackTrace();
                    disConnectResult=-1;
                }
            }
        }else{
            disConnectResult=-100;
        }

        return disConnectResult;
    }









}
