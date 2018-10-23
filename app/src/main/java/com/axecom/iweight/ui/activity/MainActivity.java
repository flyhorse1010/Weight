package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.MacManager;
import com.axecom.iweight.manager.SystemSettingManager;
import com.axecom.iweight.my.adapter.DigitalAdapter;
import com.axecom.iweight.my.adapter.OrderAdapter;
import com.axecom.iweight.my.entity.Goods;
import com.axecom.iweight.my.entity.OrderBean;
import com.axecom.iweight.my.entity.OrderInfo;
import com.axecom.iweight.my.entity.ResultInfo;
import com.axecom.iweight.my.entity.dao.GoodsDao;
import com.axecom.iweight.my.entity.dao.OrderBeanDao;
import com.axecom.iweight.my.entity.dao.OrderInfoDao;
import com.axecom.iweight.my.helper.HeartBeatServcice;
import com.axecom.iweight.my.rzl.utils.ApkUtils;
import com.axecom.iweight.my.rzl.utils.DownloadDialog;
import com.axecom.iweight.my.rzl.utils.Version;
import com.axecom.iweight.ui.activity.setting.GoodsSettingActivity;
import com.axecom.iweight.ui.adapter.GoodMenuAdapter;
import com.axecom.iweight.utils.ButtonUtils;
import com.axecom.iweight.utils.FileUtils;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;
import com.luofx.help.CashierInputFilter;
import com.luofx.listener.VolleyListener;
import com.luofx.listener.VolleyStringListener;
import com.luofx.utils.DateUtils;
import com.luofx.utils.PreferenceUtils;
import com.luofx.utils.ToastUtils;
import com.luofx.utils.log.MyLog;
import com.luofx.view.CustomScrollBar;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.shangtongyin.tools.serialport.Print;
import com.shangtongyin.tools.serialport.WeightHelper;
import com.sunfusheng.marqueeview.MarqueeView;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.axecom.iweight.utils.CommonUtils.parseFloat;
import static com.shangtongyin.tools.serialport.IConstants_ST.KEY;
import static com.shangtongyin.tools.serialport.IConstants_ST.MARKET_ID;
import static com.shangtongyin.tools.serialport.IConstants_ST.MCHID;
import static com.shangtongyin.tools.serialport.IConstants_ST.NOTIFY_CLEAR;
import static com.shangtongyin.tools.serialport.IConstants_ST.NOTIFY_MARQUEE;
import static com.shangtongyin.tools.serialport.IConstants_ST.NOTIFY_WEIGHT;
import static com.shangtongyin.tools.serialport.IConstants_ST.SELLER;
import static com.shangtongyin.tools.serialport.IConstants_ST.SELLER_ID;
import static com.shangtongyin.tools.serialport.IConstants_ST.TID;

public class MainActivity extends MainBaseActivity implements VolleyListener, VolleyStringListener, View.OnClickListener, View.OnLongClickListener {

    private GridView gvGoodMenu;
    private GridView digitalGridView;
    private ListView commoditysListView;
    private OrderAdapter orderAdapter;
    private GoodMenuAdapter goodMenuAdapter;

    private LinearLayout weightLayout;
    private LinearLayout countLayout;
    private EditText countEt;
    private EditText etPrice;
    private TextView tvGoodsName;
    private TextView tvgrandTotal;
    private TextView tvTotalWeight;
    private TextView weightTv;
    private TextView weightNumberTv;
    private TextView tvTotalPrice;
    private TextView operatorTv;
    private TextView stallNumberTv;
    private TextView componyTitleTv;
    private TextView weightTopTv;
    private List<HotKeyBean> HotKeyBeanList;
    private List<OrderBean> orderBeans;
    private Goods selectedGoods;
    private int mTotalCopies = 1;


    public SecondPresentation banner;
    boolean switchSimpleOrComplex;
    boolean stopPrint;

    /************************************************************************************/

//    private ThreadPool threadPool;  //线程池 管理
    private boolean flag = true;
    private TextView loginTypeName;
    private boolean mNotPushRemote;


    private void initViewFirst() {
        gvGoodMenu = findViewById(R.id.gvGoodMenu);
        digitalGridView = findViewById(R.id.main_digital_keys_grid_view);
        commoditysListView = findViewById(R.id.main_commoditys_list_view);
        weightLayout = findViewById(R.id.main_weight_layout);
        countLayout = findViewById(R.id.main_count_layout);
        countEt = findViewById(R.id.main_count_et);
        tvGoodsName = findViewById(R.id.tvGoodsName);
        tvgrandTotal = findViewById(R.id.main_grandtotal_tv);
        tvTotalWeight = findViewById(R.id.main_weight_total_tv);
//        weightTotalMsgTv = findViewById(R.id.main_weight_total_msg_tv);
        weightTv = findViewById(R.id.main_weight_tv);
        operatorTv = findViewById(R.id.main_operator_tv);
        loginTypeName = findViewById(R.id.tv_login_type_name);
        stallNumberTv = findViewById(R.id.main_stall_number_tv);
        weightNumberTv = findViewById(R.id.main_weight_number_tv);
        componyTitleTv = findViewById(R.id.main_compony_title_tv);
        tvTotalPrice = findViewById(R.id.main_price_total_tv);
        weightTopTv = findViewById(R.id.main_weight_top_tv);
//        weightTopMsgTv = findViewById(R.id.main_weight_msg_tv);
        findViewById(R.id.main_cash_btn).setOnClickListener(this);
        findViewById(R.id.main_settings_btn).setOnClickListener(this);
        findViewById(R.id.main_clear_btn).setOnClickListener(this);
        findViewById(R.id.btnClearn).setOnClickListener(this);
        findViewById(R.id.btnClearn).setOnLongClickListener(this);
        findViewById(R.id.btnAdd).setOnClickListener(this);
        findViewById(R.id.main_scan_pay).setOnClickListener(this);
        etPrice = findViewById(R.id.main_commodity_price_et);
        etPrice.requestFocus();

        InputFilter[] filters = {new CashierInputFilter()};
        etPrice.setFilters(filters); //设置
//        etPrice.addTextChangedListener(new MoneyTextWatcher(etPrice));


        countEt.addTextChangedListener(countTextWatcher);

        disableShowInput(etPrice);
        disableShowInput(countEt);

        weightTopTv.setOnClickListener(this);

    }

    //检查版本更新
    private void checkVersion(){
        final DownloadDialog downloadDialog=new DownloadDialog(this);
        ApkUtils.checkRemoteVersion(1,sysApplication,new Handler(){
            @Override
            public void handleMessage(final Message msg) {
                if(msg.what==10012){//有下载进度
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(msg.arg2>0){
                                downloadDialog.setProgress(msg.arg1,msg.arg2);//arg1:已下载字节数,arg2:总字节数
                            }
                        }
                    });
                }else if(msg.what==10013){//显示下载进度对话框
                    final Version v=(Version) msg.obj;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadDialog.setVersion(v.version);//版本
                            downloadDialog.setDate(v.date);//更新日期
                            downloadDialog.setDescription(v.description);//更新描述
                            downloadDialog.show();
                        }
                    });
                }
                super.handleMessage(msg);
            }
        });
    }
    private void initData() {
        HotKeyBeanList = new ArrayList<>();
        orderBeans = new ArrayList<>();
        goodMenuAdapter = new GoodMenuAdapter(this);

//        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
//        assert displayManager != null;
//        Display[] presentationDisplays = displayManager.getDisplays();
////        LogUtils.d("------------: " + presentationDisplays.length + "  --- " + presentationDisplays[1].getName());
//        if (presentationDisplays.length > 1) {
//            banner = new SecondPresentation(this.getApplicationContext(), presentationDisplays[1]);
//            SysApplication.bannerActivity = banner;
//            Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//            banner.show();
//        }


        //初始化 键盘 设置
        initDigital();
        initHandler();
        weighUtils = new WeightHelper(handler);
        weighUtils.open();

        initHeartBeat();

    }


    private MarqueeView marqueeView; //走马灯控件
    private TextView tvmarqueeView; //走马灯控件

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        checkVersion();//检查版本更新
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewFirst();
        initData();
        initView();

        goodsDao = new GoodsDao<>(context);
        initSecondScreen();


//        marqueeView = findViewById(R.id.marqueeView);
//        tvmarqueeView = findViewById(R.id.tvmarqueeView);


        askOrderState();

//       ScrollTextView autoScrollTextView= findViewById(R.id.tv123);
//
//        autoScrollTextView.init(getWindowManager());
//        autoScrollTextView.startScroll();
//        autoScrollTextView.setText("你大爷的 奶奶个熊哦");

        customScrollBar = findViewById(R.id.tvScrollBar);
//        customScrollBar.setText("niafasfafj发建安费劲啊发生放假啦");
//        customScrollBar.setTimes(5);


        orderInfoDao = new OrderInfoDao<>(context);
        orderBeanDao = new OrderBeanDao<>(context);

    }

    private OrderInfoDao<OrderInfo> orderInfoDao;
    private OrderBeanDao<OrderBean> orderBeanDao;
    private CustomScrollBar customScrollBar;
    boolean isAskOrdering = true;

    /**
     * 轮询 询问订单信息
     */
    private void askOrderState() {
        sysApplication.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (isAskOrdering) {
                    try {
                        if (askInfos.size() > 0) {
                            handler.sendEmptyMessage(NOTIFY_MARQUEE);
                            for (int i = 0; i < askInfos.size(); i++) {
                                helper.askOrder(mchid, askInfos.get(i).getBillcode(), MainActivity.this, 4);
                                Thread.sleep(100);
                            }
                        }
                        Thread.sleep(3000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        getGoods();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initSecondScreen() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        assert displayManager != null;
        Display[] presentationDisplays = displayManager.getDisplays();
//        LogUtils.d("------------: " + presentationDisplays.length + "  --- " + presentationDisplays[1].getName());
        if (presentationDisplays.length > 1) {
            banner = new SecondPresentation(this.getApplicationContext(), presentationDisplays[1]);
            SysApplication.bannerActivity = banner;
            Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            banner.show();
        }
    }

    private GoodsDao<Goods> goodsDao;

    private void getGoods() {

        List<Goods> goodsList = goodsDao.queryAll();
        goodMenuAdapter.setDatas(goodsList);
    }


    public void setInitView() {

        //TODO 数据
//        getLoginInfo();
//        advertising();

//        SystemSettingManager.getSettingData(this);

    }


    /**
     * 获取称重重量信息
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case NOTIFY_WEIGHT:
                        String weight = (String) msg.obj;
                        weightTv.setText(weight);
                        weightTopTv.setText(weight);
                        setGrandTotalTxt();
                        break;
                    case NOTIFY_MARQUEE:
                        setmarQueeNotify();
                        break;
                    case NOTIFY_CLEAR:
                        clearnContext();
                        break;
                }
                return false;
            }
        });
    }

    private StringBuilder scrollBarBuilder;
    private final String textEmpty = "              ";

    private void setmarQueeNotify() {
        if (scrollBarBuilder == null) {
            scrollBarBuilder = new StringBuilder();
        }
        scrollBarBuilder.setLength(0);

        for (OrderInfo orderinfo : askInfos) {
            scrollBarBuilder.append(orderinfo.getTotalamount()).append("元，待支付").append(textEmpty);
        }

        customScrollBar.setText(scrollBarBuilder.toString());
        banner.notifyData(askInfos);


//        marqueeView.startWithList(info);
//
//        tvmarqueeView.setText("发生法律建安费链接啊司法局发顺丰萨拉飞机按了");
        // 在代码里设置自己的动画
//        marqueeView.startWithList(info, R.anim.anim_bottom_in, R.anim.anim_top_out);
    }

    /**
     * 清理内容
     */
    private void clearnContext() {
        orderBeans.clear();
        orderAdapter.notifyDataSetChanged();
        tvTotalPrice.setText("0.00");
        tvTotalWeight.setText("0.00");
        etPrice.setText("0");
        tvGoodsName.setText("");
    }

    /**
     * 初始化结算列表
     */
    private void initSettlement() {
        orderAdapter = new OrderAdapter(this, orderBeans);
        commoditysListView.setAdapter(orderAdapter);
        commoditysListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                orderBeans.remove(position);
                orderAdapter.notifyDataSetChanged();
                calculatePrice();
                return true;
            }
        });
    }


    private void initDigital() {

        final DigitalAdapter digitalAdapter = new DigitalAdapter(this, null);
        digitalGridView.setAdapter(digitalAdapter);
        digitalGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedGoods == null) {
                    return;
                }
                String text = digitalAdapter.getItem(position);
                String price = etPrice.getText().toString();
                if ("删除".equals(text)) {
                    if (price.length() > 0) {
                        String priceNew = price.substring(0, price.length() - 1);
                        etPrice.setText(priceNew);
                    }
                } else {
                    if (".".equals(text)) {
                        if (!price.contains(".")) {
                            String priceNew = price + text;
                            etPrice.setText(priceNew);
                        }
                    } else {
                        String priceNew = price + text;
                        etPrice.setText(priceNew);
                    }
                }

//                setGrandTotalTxt();

//                switch (view.getId()) {
//                    case R.id.main_count_et:
//                        setEditText(countEt, position, text);
//                        break;
//                    case R.id.main_commodity_price_et:
////                        if(priceEt.requestFocus()){
////                            priceEt.setText("");
////                        }
//                        setEditText(etPrice, position, text, 0);


            }


        });
    }


    private Context context;


    public void initView() {
        context = this;
        weightNumberTv.setText(AccountManager.getInstance().getScalesId());
        initSettlement();


        gvGoodMenu.setAdapter(goodMenuAdapter);

//        List<HotKeyBean> hotKeyBeanList = SQLite.select().from(HotKeyBean.class).queryList();
//        if (hotKeyBeanList.size() > 0) {
//            HotKeyBeanList.addAll(hotKeyBeanList);
//            goodMenuAdapter.notifyDataSetChanged();
//        } else {
//            getGoodsData();
//        }

        gvGoodMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedGoods = goodMenuAdapter.getItem(position);
                tvGoodsName.setText(selectedGoods.getName());
                etPrice.setText("");
                etPrice.setHint(selectedGoods.getPrice());

                float count = 0;
                if (!TextUtils.isEmpty(countEt.getText())) {
                    count = parseFloat(countEt.getText().toString());
                } else if (!TextUtils.isEmpty(countEt.getHint())) {
                    count = parseFloat(countEt.getHint().toString());
                }
                if (switchSimpleOrComplex) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.getPrice()) * count));
                } else {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.getPrice()) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.getPrice()) * parseFloat(weightTopTv.getText().toString())));
                    }
                }

                goodMenuAdapter.setCheckedAtPosition(position);
                goodMenuAdapter.notifyDataSetChanged();
            }
        });


        LinkedHashMap valueMap = (LinkedHashMap) SPUtils.readObject(this, SystemSettingsActivity.KEY_STOP_PRINT);
        if (valueMap != null) {
            stopPrint = (boolean) valueMap.get("val");
        }
        switchSimpleOrComplex = (boolean) SPUtils.get(this, SettingsActivity.KET_SWITCH_SIMPLE_OR_COMPLEX, false);
        if (switchSimpleOrComplex) {
            countLayout.setVisibility(View.VISIBLE);
            weightLayout.setVisibility(View.GONE);
        } else {
            countEt.setText("0");
            countLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.VISIBLE);
        }
        mTotalCopies = (int) SPUtils.get(this, LocalSettingsActivity.KEY_PRINTER_COUNT, mTotalCopies);
    }

    // 商通的称重  工具类
    private WeightHelper weighUtils;
    private HeartBeatServcice.MyBinder myBinder;
    private HeartBeatServcice heartBeatServcice;
    private ServiceConnection mConnection;
    private int tid = -1;  //秤的编号
    private int marketId = -1;  // 市场id
    private String seller;  //售卖人
    private String key;  //售卖人
    private String mchid;  //售卖人
    private int sellerid;  // 售卖人id

    /**
     * 初始化心跳
     */
    private void initHeartBeat() {
        tid = PreferenceUtils.getInt(context, TID, -1);
        marketId = PreferenceUtils.getInt(context, MARKET_ID, -1);
        sellerid = PreferenceUtils.getInt(context, SELLER_ID, -1);
        seller = PreferenceUtils.getString(context, SELLER, null);
        key = PreferenceUtils.getString(context, KEY, null);
        mchid = PreferenceUtils.getString(context, MCHID, null);


        if (tid > 0 && marketId > 0) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    myBinder = (HeartBeatServcice.MyBinder) service;
                    heartBeatServcice = myBinder.getService();
                    heartBeatServcice.setMarketid(marketId);
                    heartBeatServcice.setTerid(tid);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("MainActivity", "onServiceDisconnected");
                }
            };
            Intent serviceIntent = new Intent(this, HeartBeatServcice.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        } else {
            //TODO  未获取到秤的市场id 和秤编号 ，需要重新登陆
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }


    private TextWatcher countTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    countEt.setText(s.subSequence(1, 2));
                    countEt.setSelection(1);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            String temp = s.toString();
            int posDot = temp.indexOf(".");
            //小数点之前保留3位数字或者一千
            if (posDot <= 0) {
                //temp
                if (temp.equals("10000")) {
                    return;
                } else {
                    if (temp.length() <= 4) {
                        return;
                    } else {
                        s.delete(4, 5);
                        return;
                    }
                }
            }
            //保留三位小数
            if (temp.length() - posDot - 1 > 1) {
                s.delete(posDot + 2, posDot + 3);
            }
        }
    };

//    public void advertising() {
//        RetrofitFactory.getInstance().API()
//                .advertising()
//                .compose(this.<BaseEntity<Advertis>>setThread())
//                .subscribe(new Observer<BaseEntity<Advertis>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(BaseEntity<Advertis> advertisBaseEntity) {
//                        if (advertisBaseEntity.isSuccess()) {
//                            final Advertis advertis = advertisBaseEntity.getData();
//                            List<String> list = new ArrayList<>();
//                            for (int i = 0; i < advertis.list.size(); i++) {
//                                list.add(advertis.list.get(i).img);
//                            }
//                            banner.convenientBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
//                                @Override
//                                public NetworkImageHolderView createHolder() {
//                                    return new NetworkImageHolderView();
//                                }
//                            }, list).startTurning(2000);
////                            banner.showAutoCancel(2000);
//                            banner.show();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//    }

    @SuppressLint("DefaultLocale")
    public void setGrandTotalTxt() {
        try {
            float count = 0;
            if (!TextUtils.isEmpty(countEt.getText())) {
                count = parseFloat(countEt.getText().toString());
            } else if (!TextUtils.isEmpty(countEt.getHint())) {
                count = parseFloat(countEt.getHint().toString());
            }
            if (switchSimpleOrComplex) {
                if (!TextUtils.isEmpty(etPrice.getText())) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * count));
                } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * count));
                }

            } else {
                if (!TextUtils.isEmpty(etPrice.getText())) {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * parseFloat(weightTopTv.getText().toString())));
                    }
                } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * parseFloat(weightTopTv.getText().toString())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
        weighUtils.closeSerialPort();
//        FileUtils.saveObject(this, (Serializable) HotKeyBeanList, GoodsSettingActivity.SelectedGoodsState.selectedGoods);
        isAskOrdering = false;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            startDDMActivity(SettingsActivity.class, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_cash_btn:
                if (SystemSettingManager.disable_cash_mode()) {
                    Toast.makeText(this, "已停用现金模式", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!ButtonUtils.isFastDoubleClick(R.id.main_cash_btn)) {
                    //结算时带上当前称重的记录
                    appendCurrentGood();
                    if (parseFloat(tvTotalPrice.getText().toString()) > 0 || parseFloat(tvgrandTotal.getText().toString()) > 0) {
                        payCashDirect(0);
                    }
                }


                break;
            case R.id.main_scan_pay:
                if (!ButtonUtils.isFastDoubleClick(R.id.main_scan_pay)) {
                    //结算时带上当前称重的记录
                    appendCurrentGood();
                    if (parseFloat(tvTotalPrice.getText().toString()) > 0 || parseFloat(tvgrandTotal.getText().toString()) > 0) {
                        if (!NetworkUtil.isConnected(this)) {
                            ToastUtils.showToast(this, "使用第三方支付需要连接网络！");
                        } else {
                            payCashDirect(1);

//                            Intent intent = new Intent(context, UseCashActivity.class);
//                            startActivityForResult(intent, 1011);
//                            charge(false);
                        }
                    }
                }
                break;
            case R.id.main_settings_btn:
//                if (!ButtonUtils.isFastDoubleClick(R.id.home_card_number_tv)) {
//                    Intent intent2 = new Intent();
//                    intent2.setClass(this, StaffMemberLoginActivity.class);
//                    startActivityForResult(intent2, 1002);
//                }

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                break;
            case R.id.main_clear_btn:
                clear(0, false);
                break;
            case R.id.btnClearn:
                clear(1, false);
                break;
            case R.id.btnAdd:
                accumulative(false);
                break;
        }
    }

    private void appendCurrentGood() {
        if (orderBeans.size() == 0)
            accumulative(true);
    }

    /**
     * 累计 菜品价格
     */
    private void accumulative(boolean clean) {
        if (selectedGoods == null) {
            return;
        }
        if (TextUtils.isEmpty(etPrice.getText()) && TextUtils.isEmpty(etPrice.getHint())) {
            return;
        }
        if (parseFloat(tvgrandTotal.getText().toString()) <= 0) {
            return;
        }


        OrderBean orderBean = new OrderBean();
        String price = TextUtils.isEmpty(etPrice.getText().toString()) ? etPrice.getHint().toString() : etPrice.getText().toString();
        orderBean.setPrice(price);
        String weight = weightTopTv.getText().toString();
        orderBean.setWeight(weight);

        String grandTotal = tvgrandTotal.getText().toString();
        orderBean.setMoney(grandTotal);

        orderBean.setItemno(String.valueOf(selectedGoods.getCid()));
        orderBean.setName(tvGoodsName.getText().toString());

        orderBeans.add(orderBean);
        orderAdapter.notifyDataSetChanged();
        calculatePrice();

//        orderBean.name = MainActivity.this.selectedGoods.name;
//        orderBean.traceable_code = MainActivity.this.selectedGoods.traceable_code;
//        orderBean.is_default = MainActivity.this.selectedGoods.is_default;
//        orderBean.batch_code = MainActivity.this.selectedGoods.batch_code;
//        selectedGoods.weight = weightTopTv.getText().toString();
//        selectedGoods.grandTotal = tvgrandTotal.getText().toString();
//        selectedGoods.count = countEt.getText().toString();
//        seledtedGoodsList.add(selectedGoods);
//        orderAdapter.notifyDataSetChanged();
//        banner.showSelectedGoods(seledtedGoodsList);
//
//        SQLite.update(HotKeyBean.class)
//                .set(HotKeyBean_Table.price.eq(selectedGoods.price))
//                .where(HotKeyBean_Table.id.eq(selectedGoods.id))
//                .query();
//        selectedGoods.getPrice() = selectedGoods.price;
//        calculatePrice();
//        if (clean)
//            clear(3, false);


    }


    @SuppressLint("DefaultLocale")
    public void calculatePrice() {
        float weightTotalF = 0.0000f;
        float priceTotal = 0;
        for (int i = 0; i < orderBeans.size(); i++) {
            OrderBean goods = orderBeans.get(i);
            if (!TextUtils.isEmpty(goods.getPrice())) {
                weightTotalF += parseFloat(goods.getWeight());
                priceTotal += parseFloat(goods.getMoney());
            }
        }

        tvTotalWeight.setText(String.format("%.3f", weightTotalF));
        tvTotalPrice.setText(String.format("%.2f", priceTotal));
    }

    @Subscribe
    public void onEventMainThread(BusEvent event) {
        if (event != null) {
            if (event.getType() == BusEvent.POSITION_PATCH22) {  //补打上一笔 交易
                String bmpUrl = SPUtils.getString(MainActivity.this, "print_bitmap", "");
                String price = SPUtils.getString(MainActivity.this, "print_price", "");
                String orderNo = SPUtils.getString(MainActivity.this, "print_orderno", "");
                String payId = SPUtils.getString(MainActivity.this, "print_payid", "");
                String priceTotal = SPUtils.getString(MainActivity.this, "print_price", "");
//                btnShangtongPrint(bmpUrl,
//                        orderNo,
//                        payId,
//                        operatorTv.getText().toString(),
//                        priceTotalTv.getText().toString(),
//                        (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));

            }

            if (event.getType() == BusEvent.PRINTER_LABEL || event.getType() == BusEvent.POSITION_PATCH) {
                if (event.getType() == BusEvent.PRINTER_LABEL) {
                    String title = "支付成功";
                    String message = "支付金额：" + tvTotalPrice.getText().toString() + "元";
                    int delayTimes = 2000;
                    showLoading(title, message, delayTimes);
                    banner.showPayResult(title, message, delayTimes);
                }

//                bitmap = (Bitmap) event.getParam();
//                bitmap = event.getQrString();
//                if (TextUtils.isEmpty(bitmap)) {
//                    String bmpUrl = SPUtils.getString(MainActivity.this, "print_bitmap", "");
//                    String price = SPUtils.getString(MainActivity.this, "print_price", "");
//                    String orderNo = SPUtils.getString(MainActivity.this, "print_orderno", "");
//                    String payId = SPUtils.getString(MainActivity.this, "print_payid", "");
//                    String priceTotal = SPUtils.getString(MainActivity.this, "print_price", "");
////                    btnShangtongPrint(bmpUrl,
////                            orderNo,
////                            payId,
////                            operatorTv.getText().toString(),
////                            priceTotalTv.getText().toString(),
////                            (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));
//                } else

                {
                    String orderNo = event.getStrParam();
                    String payId = event.getStrParam02();
                    SPUtils.putString(this, "print_orderno", orderNo);
                    SPUtils.putString(this, "print_payid", payId);
                    SPUtils.putString(this, "print_price", tvTotalPrice.getText().toString());

//                    btnShangtongPrint(bitmap,
//                            orderNo,
//                            payId,
//                            operatorTv.getText().toString(),
//                            priceTotalTv.getText().toString(),
//                            (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));
                    clear(1, true);
                }
            }
            if (event.getType() == BusEvent.PRINTER_NO_BITMAP) {
                String title = "支付成功";
                String message = "支付金额：" + tvTotalPrice.getText().toString() + "元";
                int delayTimes = 2000;
                showLoading(title, message, delayTimes);
//                banner.showPayResult(title, message, delayTimes);
//                banner.showSelectedGoods(orderBeans);


//                orderNo = (Math.random() * 9 + 1r) * 100000 + getCurrentTime("yyyyMMddHHmmss");
                int random = (int) (Math.random() * 9 + 1) * 100;
                String orderNo = "AX" + DateUtils.getSampleNo() + sysApplication.getTid();
                String payId = event.getStrParam02();
                SPUtils.putString(this, "print_price", tvTotalPrice.getText().toString());

//                btnShangtongPrint(bitmap,
//                        orderNo,
//                        payId,
//                        operatorTv.getText().toString(),
//                        priceTotalTv.getText().toString(),
//                        (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));

                clear(1, true);
            }
            if (event.getType() == BusEvent.NET_WORK_AVAILABLE) {
                boolean available = event.getBooleanParam();
                if (available) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = null;
                    if (connectivityManager != null) {
                        networkInfo = connectivityManager.getActiveNetworkInfo();
                    }
                    if (networkInfo != null && networkInfo.isAvailable()) {
                        Object object = SPUtils.readObject(MainActivity.this, "local_order");
                        List<SubOrderReqBean> orders = (List<SubOrderReqBean>) object;


//                        submitOrders(orders);


                    }
                }
            }
            if (event.getType() == BusEvent.SAVE_COMMODITY_SUCCESS) {
                getGoodsData();
            }

            if (event.getType() == BusEvent.LOGIN_OUT) {
                finish();
            }
            if (event.getType() == BusEvent.notifiySellerInfo) {
                //TODO 数据
//                getLoginInfo();
//                getGoodsData();
            }
        }
    }


    /**
     * shangtong 打印机打印
     */
    @SuppressLint("StaticFieldLeak")
    public void btnShangtongPrint(final OrderInfo orderInfo) {
        final String companyName = "深圳市安鑫宝科技发展有限公司";
        final String stallNumber2 = stallNumberTv.getText().toString();//摊位号
        final String currentTime = DateUtils.getYY_TO_ss(new Date());

//        threadPool = ThreadPool.getInstantiation();

        final Print print = sysApplication.getPrint();

        //TODO  打印功能  带开启
        sysApplication.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                List<OrderBean> orderBeans = orderInfo.getOrderItem();
                orderInfoDao.insert(orderInfo);

                StringBuilder sb = new StringBuilder();
                sb.append("------------交易明细------------\n");
                sb.append(companyName + "\n");
                String stallNumber;
                if (TextUtils.isEmpty(stallNumber2)) {
                    stallNumber = " ";
                } else {
                    stallNumber = stallNumber2;
                }

                sb.append("交易日期：").append(currentTime).append("\n");
                sb.append("交易单号：").append(orderInfo.getBillcode()).append("\n");

                if (1 == orderInfo.getSettlemethod()) {
                    sb.append("结算方式：微信支付\n");
                }
                if (2 == orderInfo.getSettlemethod()) {
                    sb.append("结算方式：支付宝支付\n");
                }
                if (3 == orderInfo.getSettlemethod()) {
                    sb.append("结算方式：现金支付\n");
                }

                sb.append("卖方名称：").append(orderInfo.getSeller()).append("\n");
                sb.append("摊位号：").append(stallNumber).append("\n");
                sb.append("商品名\b" + "单价/元\b" + "重量/kg\b" + "金额/元" + "\n");

                for (int i = 0; i < orderBeans.size(); i++) {
                    OrderBean goods = orderBeans.get(i);
                    goods.setOrderInfo(orderInfo);
                    sb.append(goods.getName()).append("\t").append(goods.getPrice()).append("\t").append(goods.getWeight()).append("\t").append(goods.getMoney()).append("\n");
                }
                orderBeanDao.insert(orderBeans);

                sb.append("--------------------------------\n");
                sb.append("合计(元)：").append(orderInfo.getTotalamount()).append("\n");
                sb.append("司磅员：").append(orderInfo.getSeller()).append("\t").append("秤号：").append(tid);
                sb.append("\n\n");

                sb.append("\n\n");
                print.setLineSpacing((byte) 32);
                print.PrintString(sb.toString());


//                boolean isAvailable = NetworkUtil.isAvailable(context);// 有网打印二维码
//                if (!isAvailable) {
//                    sb.append("\n\n");
//                    print.setLineSpacing((byte) 32);
//                    print.PrintString(sb.toString());
//                } else {
//                    print.setLineSpacing((byte) 32);
//                    print.PrintString(sb.toString());
//
//                    byte[] bytes = null;
//                    try {
//                        int index1 = bitmap.indexOf("url=");
//                        if (index1 > 0) {
//                            String qrString = bitmap.substring(index1 + 4);
//                            if (qrString.length() > 0) {
//                                bytes = print.getbyteData(qrString, 32, 32);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        if (bytes != null) {
//                            print.PrintltString("扫一扫获取追溯信息：");
//                            print.printQR(bytes);
//                            print.PrintltString("--------------------------------\n\n\n");
//                        }
//
//                    } catch (IOException | InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }


            }
        });
    }


    @SuppressLint("SetTextI18n")
    public void clear(int type, boolean b) {
        if (type == 0) {
            weighUtils.resetBalance();
        }
        if (type == 1) {
            weightTopTv.setText("0.000");
            weightTv.setText("");
            etPrice.setHint("0");
            etPrice.setText("");
            tvTotalWeight.setText("0");
            tvgrandTotal.setText("0.00");
            tvTotalPrice.setText("0.00");
            orderBeans.clear();
            tvGoodsName.setText("");

            orderAdapter = new OrderAdapter(this, orderBeans);
            commoditysListView.setAdapter(orderAdapter);
            selectedGoods = null;

            goodMenuAdapter.cleanCheckedPosition();
            goodMenuAdapter.notifyDataSetChanged();

        }
        if (type == 3) {
            selectedGoods = null;
            tvGoodsName.setText("");
            goodMenuAdapter.cleanCheckedPosition();
            goodMenuAdapter.notifyDataSetChanged();
            String hint = "";
            if (!TextUtils.isEmpty(etPrice.getText())) {
                hint = etPrice.getText().toString();
            } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                hint = etPrice.getHint().toString();
            }
            etPrice.setText("");
            etPrice.setHint(hint);
        }
    }

    public void charge(boolean useCash) {

        if (orderBeans.size() < 1) {
            accumulative(true);
        }
        Intent intent = new Intent();
        SubOrderReqBean subOrderReqBean = new SubOrderReqBean();
        SubOrderReqBean.Goods good;
        List<SubOrderReqBean.Goods> goodsList = new ArrayList<>();
        for (int i = 0; i < orderBeans.size(); i++) {
            good = new SubOrderReqBean.Goods();
            OrderBean HotKeyBean = orderBeans.get(i);
            good.setGoods_id(HotKeyBean.getItemno() + "");
            good.setGoods_name(HotKeyBean.getName());
            good.setGoods_price(HotKeyBean.getPrice());
            good.setGoods_number(countEt.getText().toString());
            good.setGoods_weight(HotKeyBean.getWeight());
            good.setGoods_amount(HotKeyBean.getMoney());
            good.setBatch_code(HotKeyBean.getTraceno());
            goodsList.add(good);
        }
        subOrderReqBean.setToken(AccountManager.getInstance().getToken());
        subOrderReqBean.setMac(MacManager.getInstace(this).getMac());
        subOrderReqBean.setTotal_amount(tvTotalPrice.getText().toString());
        subOrderReqBean.setTotal_weight(tvTotalWeight.getText().toString());
        subOrderReqBean.setCreate_time(getCurrentTime());
        String orderNo = "AX" + getCurrentTime("yyyyMMddHHmmss") + AccountManager.getInstance().getScalesId();
        subOrderReqBean.setOrder_no(orderNo);
        subOrderReqBean.setGoods(goodsList);
        if (switchSimpleOrComplex) {
            subOrderReqBean.setPricing_model("2");
        } else {
            subOrderReqBean.setPricing_model("1");
        }


//        Bundle bundle = new Bundle();
//        bundle.putSerializable("orderBean", subOrderReqBean);
//        intent.putExtras(bundle);


        intent.setClass(this, UseCashActivity.class);


//        SPUtils.saveObject(this, "selectedGoodList", orderBeans);

//        if (useCash) {
//            payCashDirect(subOrderReqBean);
//        } else {
//            banner.showPayAmount(subOrderReqBean.getTotal_amount(), "");
//            startActivity(intent);
//        }

    }

    public void payEbay(SubOrderReqBean orderBean) {


    }

    /**
     * 直接现金支付
     */
    public void payCashDirect(int payType) {
//        现金直接支付
//        banner.showPayAmount(orderBean.getTotal_amount(), "支付方式：现金支付");
//        String payId = "4";
//        orderBean.setPayment_id(payId);
//        if (NetworkUtil.isConnected(this)) {
//            //TODO 直接現金 支付  待恢复
////            ne
//
// w PayCheckManage(this, banner, null, orderBean, payId).submitOrder();
//        } else {
//            List<SubOrderReqBean> orders = (List<SubOrderReqBean>) SPUtils.readObject(this, "local_order");
//            if (orders != null) {
//                orders.add(orderBean);
//                SPUtils.saveObject(this, "local_order", orders);
//            } else {
//                List<SubOrderReqBean> localOrder = new ArrayList<>();
//                localOrder.add(orderBean);
//                SPUtils.saveObject(this, "local_order", localOrder);
//            }
//            EventBus.getDefault().post(new BusEvent(BusEvent.PRINTER_NO_BITMAP, "", payId, ""));
//        }

        OrderInfo orderInfo = new OrderInfo();
        List<OrderBean> orderlist = new ArrayList<>(orderBeans);
        orderInfo.setOrderItem(orderlist);
        String billcode = "AX" + DateUtils.getSampleNo() + tid;
        String currentTime = DateUtils.getYY_TO_ss(new Date());

        if (payType == 0) {
            orderInfo.setBillstatus("成功");
        } else {
            orderInfo.setBillstatus("待支付");
        }

        orderInfo.setSeller(seller);
        orderInfo.setSellerid(sellerid);
        orderInfo.setTerid(tid);
        orderInfo.setTotalamount(tvTotalPrice.getText().toString());
        orderInfo.setTotalweight(tvTotalWeight.getText().toString());

        orderInfo.setMarketid(marketId);
        orderInfo.setTime(currentTime);
        orderInfo.setSettlemethod(0);
        orderInfo.setBillcode(billcode);

        helper.commitDD(orderInfo, MainActivity.this, 3);

//        OrderInfoDao<OrderInfo> orderInfoDao = new OrderInfoDao<>(context);
//        orderInfoDao.insert(orderInfo);



        btnShangtongPrint(orderInfo);

        if (payType == 1) {
//         String baseUrl  = "http://pay.axebao.com/payInterface_gzzh/pay?key=" + key + "&mchid=" + mchid;

            askInfos.add(orderInfo);
            banner.notifyData(askInfos);
        }
    }

    List<OrderBean> secondOrderbeans = new ArrayList<>();
    List<OrderInfo> askInfos = new ArrayList<>();

//    OrderInfo secondOrderInfo=new OrderInfo();


   /* public void getLoginInfo() {
        RetrofitFactory.getInstance().API()
                .getLoginInfo(AccountManager.getInstance().getToken(), MacManager.getInstace(this).getMac())
                .compose(this.<BaseEntity<LoginInfo>>setThread())
                .subscribe(new Observer<BaseEntity<LoginInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntity<LoginInfo> loginInfoBaseEntity) {
                        if (loginInfoBaseEntity.isSuccess()) {
                            LoginInfo data = loginInfoBaseEntity.getData();
                            stallNumberTv.setText(data.boothNumber);
                            operatorTv.setText(data.client_name);
                            componyTitleTv.setText(data.organizationName);
                            switch (data.user_type) {
                                case LoginInfo.TYPE_seller:
                                    loginTypeName.setText("商户:");
                                    break;
                                case LoginInfo.TYPE_ADMIN:
                                    loginTypeName.setText(MainActivity.this.getString(R.string.string_operator));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });

    }*/

    public void getGoodsData() {
//        mNotPushRemote = (boolean) SPUtils.get(this, GoodsSettingActivity.SelectedGoodsState.NOT_PUSH_REMOTE, false);
//        List<HotKeyBean> hotKeyGoods = (List<HotKeyBean>) FileUtils.readObject(MainActivity.this, GoodsSettingActivity.SelectedGoodsState.selectedGoods);
//        boolean connected = NetworkUtil.isConnected(MainActivity.this);
//        showSelectedGoods(hotKeyGoods);
//        if (!connected) return;
//        if (mNotPushRemote) {
//            //TODO  获取数据
////            requestPushStoreGoods(hotKeyGoods);
//        } else {
//            //TODO  获取数据
////            requestGoodsData();
//        }
    }


    //TODO  獲取數據
//    private void requestGoodsData() {
//        RetrofitFactory.getInstance().API()
//                .getGoodsData(AccountManager.getInstance().getToken(), MacManager.getInstace(this).getMac())
//                .compose(this.<BaseEntity<ScalesCategoryGoods>>setThread())
//                .subscribe(new Observer<BaseEntity<ScalesCategoryGoods>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(BaseEntity<ScalesCategoryGoods> scalesCategoryGoodsBaseEntity) {
//                        if (scalesCategoryGoodsBaseEntity.isSuccess()) {
//                            ScalesCategoryGoods scalesCategoryGoods = scalesCategoryGoodsBaseEntity.getData();
//                            List<HotKeyBean> hotKeyGoods = scalesCategoryGoods.getHotKeyGoods();
//                            FileUtils.saveObject(MainActivity.this, (Serializable) hotKeyGoods, GoodsSettingActivity.SelectedGoodsState.selectedGoods);
//                            showSelectedGoods(hotKeyGoods);
//                        } else {
//                            showLoading(scalesCategoryGoodsBaseEntity.getMsg(), "数据加载失败");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        closeLoading();
//                    }
//                });
//    }

    private void showSelectedGoods(List<HotKeyBean> hotKeyGoods) {
        if (hotKeyGoods == null) return;
        HotKeyBeanList.clear();
        HotKeyBeanList.addAll(hotKeyGoods);
        HotKeyBean hotKey = new HotKeyBean();
        SQLite.delete(HotKeyBean.class).execute();
        ModelAdapter<HotKeyBean> modelAdapter = FlowManager.getModelAdapter(HotKeyBean.class);
        for (HotKeyBean goods : HotKeyBeanList) {
            hotKey.id = goods.id;
            hotKey.cid = goods.cid;
            hotKey.grandTotal = goods.grandTotal;
            hotKey.is_default = goods.is_default;
            hotKey.name = goods.name;
            hotKey.price = goods.price;
            hotKey.traceable_code = goods.traceable_code;
            hotKey.weight = goods.weight;
            hotKey.batch_code = goods.batch_code;
            modelAdapter.insert(hotKey);
        }
        goodMenuAdapter.notifyDataSetChanged();
    }


    //TODO shuju
//    public void requestPushStoreGoods(List<HotKeyBean> hotKeyGoods) {
//        if (hotKeyGoods == null) {
//            //TODO  获取数据
////            requestGoodsData();
//            return;
//        }
//        SaveGoodsReqBean goodsReqBean = new SaveGoodsReqBean();
//        List<SaveGoodsReqBean.Goods> goodsList = new ArrayList<>();
//        SaveGoodsReqBean.Goods good;
//        for (int i = 0; i < hotKeyGoods.size(); i++) {
//            good = new SaveGoodsReqBean.Goods();
//            HotKeyBean bean = hotKeyGoods.get(i);
//            good.id = bean.id;
//            good.cid = bean.cid;
//            good.is_default = bean.is_default;
//            good.name = bean.name;
//            good.price = bean.price;
//            good.traceable_code = bean.traceable_code;
//            goodsList.add(good);
//        }
//        goodsReqBean.setToken(AccountManager.getInstance().getAdminToken());
//        goodsReqBean.setMac(MacManager.getInstace(SysApplication.getContext()).getMac());
//        goodsReqBean.setGoods(goodsList);
//
//        RetrofitFactory.getInstance().API()
//                .storeGoodsData(goodsReqBean)
//                .compose(this.<BaseEntity>setThread())
//                .subscribe(new Observer<BaseEntity>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(BaseEntity baseEntity) {
//                        if (mNotPushRemote && baseEntity.isSuccess()) {
//                            SPUtils.put(MainActivity.this, GoodsSettingActivity.SelectedGoodsState.NOT_PUSH_REMOTE, false);
//                        }
//                        requestGoodsData();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//    }

    /**
     * 连接错误
     *
     * @param volleyError 错误信息
     * @param flag        请求表示索引
     */
    @Override
    public void onResponse(VolleyError volleyError, int flag) {

        switch (flag) {
            case 3:
                MyLog.myInfo("错误" + volleyError.getMessage());
                break;
            case 4:

                MyLog.myInfo("错误" + volleyError.getMessage());
                break;
        }

    }

    @Override
    public void onResponse(JSONObject jsonObject, int flag) {
        MyLog.myInfo("成功" + jsonObject.toString());
        switch (flag) {
            case 3:
                MyLog.myInfo("成功" + jsonObject.toString());
                break;
            case 4:
                ResultInfo resultInfo = JSON.parseObject(jsonObject.toString(), ResultInfo.class);
                if (resultInfo != null) {
                    if (resultInfo.getStatus() == 0) {//支付成功
                        String orderNo = resultInfo.getData();
                        for (OrderInfo orderInfo : askInfos) {
                            if (orderInfo.getBillcode().equals(orderNo)) {
                                askInfos.remove(orderInfo);
                                handler.sendEmptyMessage(NOTIFY_MARQUEE);
//                                handler.sendEmptyMessage(NOTIFY_MARQUEE);

                                String title = "支付成功";
                                String message = "支付金额：" + orderInfo.getTotalamount() + "元";
                                int delayTimes = 1500;
                                showLoading(title, message, delayTimes);
                                handler.sendEmptyMessage(NOTIFY_CLEAR);
                                break;
                            }
                        }
                    }
                }
                MyLog.myInfo("成功" + jsonObject.toString());
                break;
        }
    }


    @Override
    public void onResponseError(VolleyError volleyError, int flag) {
        MyLog.myInfo("错误" + volleyError);
        switch (flag) {
            case 3:
                MyLog.myInfo("失败" + volleyError.toString());
                break;
        }
    }


    //  返回支付状态
    @Override
    public void onResponse(String response, int flag) {
        MyLog.myInfo("成功" + response);
        ResultInfo resultInfo = JSON.parseObject(response.toString(), ResultInfo.class);
        switch (flag) {
            case 3:
                if (resultInfo.getStatus() == 0) {
                    String title = "支付成功";
                    String message = "支付金额：" + tvTotalPrice.getText().toString() + "元";
                    int delayTimes = 1500;
                    showLoading(title, message, delayTimes);
                    handler.sendEmptyMessage(NOTIFY_CLEAR);
                }

                MyLog.myInfo("成功" + response.toString());
                break;
        }
    }

    int pressCount = 0;

    @Override
    public void onBackPressed() {
        if (pressCount == 0) {
            ToastUtils.showToast(this, "再次点击退出！");
            pressCount++;
            Timer timer = new Timer();//实例化Timer类
            timer.schedule(new TimerTask() {
                public void run() {
                    pressCount = 0;
                    this.cancel();
                }
            }, 2000);//五百毫秒
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.btnClearn:

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("是否删除待支付订单")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                askInfos.clear();
                                handler.sendEmptyMessage(NOTIFY_MARQUEE);
//                                banner.notifyData(askInfos);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                builder.create().show();
                break;
        }
        return false;
    }
}
