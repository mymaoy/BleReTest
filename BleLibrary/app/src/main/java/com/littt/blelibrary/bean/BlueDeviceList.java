package com.littt.blelibrary.bean;

/**
 * Created by BJB001 on 2020/6/19.
 */

public class BlueDeviceList {
    public String name; // 蓝牙设备的名称
    public String address; // 蓝牙设备的MAC地址
    public int state; // 蓝牙设备信号值

    public BlueDeviceList(String name, String address, int state) {
        this.name = name;
        this.address = address;
        this.state = state;
    }
}
