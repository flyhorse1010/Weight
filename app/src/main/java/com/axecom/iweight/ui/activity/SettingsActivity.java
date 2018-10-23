package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.SettingsBean;
import com.axecom.iweight.bean.WeightBean;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.MacManager;
import com.axecom.iweight.manager.SystemSettingManager;
import com.axecom.iweight.my.entity.Goods;
import com.axecom.iweight.my.entity.ResultInfo;
import com.axecom.iweight.my.entity.dao.GoodsDao;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.activity.setting.GoodsSettingActivity;
import com.axecom.iweight.utils.SPUtils;
import com.luofx.listener.VolleyListener;
import com.luofx.utils.common.MyToast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018-5-16.
 */

public class SettingsActivity extends BaseActivity  implements VolleyListener {
    public static final String KET_SWITCH_SIMPLE_OR_COMPLEX = "key_switch_simple_or_complex";
    public static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String IS_RE_BOOT = "is_re_boot";
    ThreadPoolExecutor executor;

    private static final int POSITION_SWITCH = 0;
    private static final int POSITION_PATCH = 1;
    private static final int POSITION_REPORTS = 2;
    private static final int POSITION_SERVER = 3;
    private static final int POSITION_INVALID = 4;
    private static final int POSITION_ABNORMAL = 5;

    private static final int POSITION_COMMODITY = 6;
    private static final int POSITION_UPDATE = 7;
    private static final int POSITION_RE_CONNECTING = 8;
    private static final int POSITION_WIFI = 9;
    private static final int POSITION_LOCAL = 10;
    private static final int POSITION_WEIGHT = 11;
    private static final int POSITION_RE_BOOT = 12;
    private static final int POSITION_BD = 13;
    private static final int POSITION_SYSTEM = 14;
    private static final int POSITION_BLUETOOTH = 15;

    private static final int[] ICONS = {
            R.drawable.switching_setting,
            R.drawable.patch_setting,
            R.drawable.reports_setting,
            R.drawable.server_setting,
            R.drawable.invalid,
            R.drawable.abnormal_setting,
            R.drawable.bd_setting,
            R.drawable.commodity_setting,
            R.drawable.update_setting,
            R.drawable.re_connecting,
            R.drawable.wifi_setting,
            R.drawable.local_setting,
            R.drawable.weight_setting,
            R.drawable.re_boot,
            R.drawable.system_setting,
            R.drawable.system_setting
    };

    private static final int[] TITLES = {R.string.string_switching_setting_txt,
            R.string.string_patch_setting_txt,
            R.string.string_reports_setting_txt,
            R.string.string_server_setting_txt,
            R.string.string_order_setting_txt,
            R.string.string_abnormal_setting_txt,
            R.string.string_commodity_setting_txt,
            R.string.string_update_setting_txt,
            R.string.string_reconnection_txt,
            R.string.string_wifi_setting_txt,
            R.string.string_local_setting_txt,
            R.string.string_back_txt,
            R.string.string_reboot_txt,
            R.string.string_bd_setting_txt,
            R.string.string_system_setting_txt,
            R.string.string_bluetooth_setting_txt};

    private GridView settingsGV;

    private WifiManager wifiManager;


    private  Context context;
    private SysApplication sysApplication;
    @Override
    public View setInitView() {
        @SuppressLint("InflateParams") View rootView = LayoutInflater.from(this).inflate(R.layout.settings_activity, null);
        settingsGV = rootView.findViewById(R.id.settings_grid_view);

        context=this;
        sysApplication= (SysApplication) getApplication();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        registerReceiver(netWorkReceiver, new IntentFilter(ACTION_NET_CHANGE));
        BlockingQueue workQueue = new LinkedBlockingDeque<>();
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.DAYS, workQueue, threadFactory);

        return rootView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netWorkReceiver);
    }

    @Override
    public void initView() {
        List<SettingsBean> settngsList = new ArrayList<>();
        for (int i = 0; i < ICONS.length; i++) {
            SettingsBean bean = new SettingsBean();
            bean.setId(i);
            bean.setIcon(ICONS[i]);
            bean.setTitle(TITLES[i]);
            settngsList.add(bean);
        }

        int userType = AccountManager.getInstance().getUserType();
        if (userType == 1 || userType == 2) {
            settngsList.remove(15);
            settngsList.remove(14);
            settngsList.remove(13);
        }
        SettingsAdapter settingsAdapter = new SettingsAdapter(this, settngsList);
        settingsGV.setAdapter(settingsAdapter);
        settingsGV.setOnItemClickListener(settingsOnItemClickListener);


        findViewById(R.id.btnDataUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGoods(sysApplication.getTid());
            }
        });
    }

    private void initGoods(int tid) {
        String url = "http://119.23.43.64/api/smartsz/getquick?terid=" + tid;
        sysApplication.volleyGet(url, this, 2);
    }

    @Override
    public void onResponse(VolleyError volleyError, int flag) {
        MyToast.toastShort(context, "网络请求失败");

        switch (flag) {
            case 1:
                break;
            case 2:

                MyToast.toastShort(context, "初始化数据不完全");
                break;

            default:
                break;
        }
    }

    @Override
    public void onResponse(JSONObject jsonObject, int flag) {
        ResultInfo resultInfo = JSON.parseObject(jsonObject.toString(), ResultInfo.class);
        switch (flag) {
            case 2:
                if (resultInfo != null) {
                    List<Goods> goodsList = JSON.parseArray(resultInfo.getData(), Goods.class);
                    if (goodsList != null && goodsList.size() > 0) {
                        GoodsDao<Goods> goodsDao=new GoodsDao<>(context);
                        goodsDao.insert(goodsList);
                    }
                }
                break;
        }
    }



    AdapterView.OnItemClickListener settingsOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case POSITION_PATCH:
                    EventBus.getDefault().post(new BusEvent(BusEvent.POSITION_PATCH, SPUtils.getString(SettingsActivity.this, "print_orderno", ""), SPUtils.getString(SettingsActivity.this, "print_payid", "")));
//                    finish();
                    break;
                case POSITION_REPORTS:
                    startDDMActivity(DataSummaryActivity.class, false);
                    break;
                case POSITION_INVALID:
                    startDDMActivity(OrderInvalidActivity.class, false);
                    break;
                case POSITION_ABNORMAL:
                    startDDMActivity(AbnormalOrderActivity.class, false);
                    break;
                case POSITION_SERVER:
                    startDDMActivity(ServerTestActivity.class, false);
                    break;
                case POSITION_BD:
                    startDDMActivity(CalibrationActivity.class, false);
                    break;
                case POSITION_WIFI:
                    startDDMActivity(WifiSettingsActivity.class, false);
                    break;
                case POSITION_COMMODITY:
                    startDDMActivity(GoodsSettingActivity.class, false);
                    break;
                case POSITION_LOCAL:
                    startDDMActivity(LocalSettingsActivity.class, false);
                    break;
                case POSITION_SYSTEM:
                    startDDMActivity(SystemSettingsActivity2.class, false);
                    break;
                case POSITION_RE_BOOT:
                    EventBus.getDefault().post(new BusEvent(BusEvent.GO_HOME_PAGE, true));
                    Intent intent = new Intent();
                    intent.setClass(SettingsActivity.this, HomeActivity.class);
                    intent.putExtra(IS_RE_BOOT, true);
                    startActivity(intent);
                    break;
                case POSITION_WEIGHT:
                    finish();
                    break;
                case POSITION_SWITCH:
                    showLoading("切换成功");
                    boolean switchSimpleOrComplex = (boolean) SPUtils.get(SettingsActivity.this, KET_SWITCH_SIMPLE_OR_COMPLEX, false);
                    SPUtils.put(SettingsActivity.this, KET_SWITCH_SIMPLE_OR_COMPLEX, !switchSimpleOrComplex);
                    break;
                case POSITION_RE_CONNECTING:
                    showLoading();
                    String wifiSSID = SPUtils.getString(SettingsActivity.this, WifiSettingsActivity.KEY_SSID_WIFI_SAVED, "");
                    if (!TextUtils.isEmpty(wifiSSID)) {
                        WifiConfiguration mWifiConfiguration;
                        WifiConfiguration tempConfig = IsExsits(wifiSSID);
                        if (tempConfig != null) {
                            mWifiConfiguration = tempConfig;
                            boolean b = wifiManager.enableNetwork(mWifiConfiguration.networkId, true);
                            if (b) {
//                                showLoading("连接成功");
                                closeLoading();
                            }
                        }
                    }
                    break;
                case POSITION_UPDATE:
                    showLoading();
                    SystemSettingManager.updateData(SettingsActivity.this);
                    updateScalesId();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeLoading();
                        }
                    }, 2000);
                    break;
//                case POSITION_BLUETOOTH:
//                    BTHelperDialog.Builder builder = new BTHelperDialog.Builder(SettingsActivity.this);
//                    builder.create(new BTHelperDialog.OnBtnClickListener() {
//
//                        @Override
//                        public void onConfirmed(BtHelperClient.STATUS mCurrStatus, String deviceAddress) {
//                            if (mCurrStatus == BtHelperClient.STATUS.CONNECTED) {
////                                showLoading("连接成功");
//                                SPUtils.putString(SettingsActivity.this, BTHelperDialog.KEY_BT_ADDRESS, deviceAddress);
//                                EventBus.getDefault().post(new BusEvent(BusEvent.BLUETOOTH_CONNECTED, true));
//                            }
//                        }
//
//                        @Override
//                        public void onCanceled(String result) {
//
//                        }
//                    }).show();
//                    break;
            }
        }
    };

    private void updateScalesId() {
        RetrofitFactory.getInstance().API()
                .getScalesIdByMac(MacManager.getInstace(this).getMac())
                .compose(this.<BaseEntity<WeightBean>>setThread())
                .subscribe(new Observer<BaseEntity<WeightBean>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }
                    @Override
                    public void onNext(final BaseEntity<WeightBean> baseEntity) {
                        if (baseEntity.isSuccess()) {
                            AccountManager.getInstance().saveScalesId(baseEntity.getData().getId() + "");
                        } else {
                            showLoading(baseEntity.getMsg());
                        }
                    }


                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    private BroadcastReceiver netWorkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_NET_CHANGE.equals(intent.getAction())) {
                closeLoading();
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (connectivityManager != null) {
                    networkInfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkInfo != null && networkInfo.isAvailable()) {
                    Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {

    }

    class SettingsAdapter extends BaseAdapter {

        private Context context;
        private List<SettingsBean> settingList;

        public SettingsAdapter(Context context, List<SettingsBean> settingList) {
            this.context = context;
            this.settingList = settingList;
        }

        @Override
        public int getCount() {
            return settingList.size();
        }

        @Override
        public Object getItem(int position) {
            return settingList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.settings_item, null);
                holder = new ViewHolder();
                holder.iconIv = convertView.findViewById(R.id.settings_item_icon_iv);
                holder.titleTv = convertView.findViewById(R.id.settings_item_title_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SettingsBean item = settingList.get(position);
            holder.iconIv.setImageDrawable(SettingsActivity.this.getResources().getDrawable(item.getIcon()));
            holder.titleTv.setText(getString(item.getTitle()));
            return convertView;
        }

        class ViewHolder {
            ImageView iconIv;
            TextView titleTv;
        }
    }
}
