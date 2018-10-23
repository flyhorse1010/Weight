package com.axecom.iweight.my.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.axecom.iweight.base.SysApplication;
import com.luofx.listener.VolleyListener;
import com.shangtongyin.tools.serialport.IConstants_ST;

/**
 * author: luofaxin
 * date： 2018/9/26 0026.
 * email:424533553@qq.com
 * describe:
 */
public class NetHelper implements IConstants_ST {


   private  SysApplication application;
    private VolleyListener volleyListener;

    public NetHelper( SysApplication application, VolleyListener volleyListener) {
        this.application = application;
        this.volleyListener = volleyListener;
    }


    /**
     * 通过mac 获得用户信息
     * @param mac  机器的mac地址
     * @param flag 请求浮标
     */
    public void getUserInfo(String mac,  int flag) {
        String url = BASE_IP_ST + "api/smart/getinfobymac?mac="+mac;
        application.volleyGet(url, volleyListener, flag);
    }


    /**
     * 获取  设备的唯一标识
     *
     * @param context 上下文
     * @return 唯一标识 mac
     */
    @SuppressLint("HardwareIds")
    public  String getIMEI(Context context) {
        // 94:a1:a2:a4:70:66
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String mac = "";
        if (wm != null) {
            mac = wm.getConnectionInfo().getMacAddress();
        }
        return mac;

        //中科 样机 mac
//        return "78:63:11:00:03:75";
//        return "98:24:67:22:03:C0";
//         "02:16:03:C0:D6:F8";
    }
}


