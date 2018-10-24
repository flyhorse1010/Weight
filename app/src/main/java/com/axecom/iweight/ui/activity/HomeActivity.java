package com.axecom.iweight.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.my.LogActivity;
import com.axecom.iweight.my.entity.AllGoods;
import com.axecom.iweight.my.entity.Goods;
import com.axecom.iweight.my.entity.GoodsType;
import com.axecom.iweight.my.entity.LogBean;
import com.axecom.iweight.my.entity.ResultInfo;
import com.axecom.iweight.my.entity.UserInfo;
import com.axecom.iweight.my.entity.dao.AllGoodsDao;
import com.axecom.iweight.my.entity.dao.GoodsDao;
import com.axecom.iweight.my.entity.dao.GoodsTypeDao;
import com.axecom.iweight.my.entity.dao.UserInfoDao;
import com.axecom.iweight.my.net.NetHelper;
import com.axecom.iweight.my.rzl.Constrant;
import com.axecom.iweight.my.rzl.utils.ApkUtils;
import com.axecom.iweight.my.rzl.utils.DownloadDialog;
import com.axecom.iweight.my.rzl.utils.Version;
import com.axecom.iweight.ui.view.CustomDialog;
import com.axecom.iweight.ui.view.SoftKeyborad;
import com.axecom.iweight.utils.ButtonUtils;
import com.axecom.iweight.utils.CommonUtils;
import com.axecom.iweight.utils.SPUtils;
import com.luofx.listener.VolleyListener;
import com.luofx.utils.DateUtils;
import com.luofx.utils.PreferenceUtils;
import com.luofx.utils.common.MyToast;
import com.luofx.utils.log.MyLog;
import com.shangtongyin.tools.serialport.IConstants_ST;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2018-5-8.
 */
public class HomeActivity extends Activity implements View.OnClickListener, VolleyListener, IConstants_ST {

    private static final String AUTO_LOGIN = "auto_login";
    private static final String WEIGHT_ID = "weight_id";
    private TextView cardNumberTv;
    private TextView pwdTv;

    private TextView weightTv;
    private int weightId;
    private CheckedTextView savePwdCtv;
    UsbManager manager;
    public boolean threadStatus = false;
    public SecondPresentation banner = null;
    private String loginType;
    private CheckedTextView autoLogin;
    private Handler mHandler = new Handler();
    private boolean cancelAutoLogin;
    private Button confirmBtn;
    private boolean reBoot;
    private TextView versionTv;
    SysApplication sysApplication;

    private GoodsDao<Goods> goodsDao;
    private GoodsTypeDao<GoodsType> goodsTypeDao;
    private AllGoodsDao<AllGoods> allGoodsDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sysApplication = (SysApplication) getApplication();

        setInitView();
        initView();
        initHandler();
        goodsDao = new GoodsDao<>(context);
        goodsTypeDao = new GoodsTypeDao<>(context);
        allGoodsDao = new AllGoodsDao<>(context);

    }


    private Handler handler;

    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case NOTIFY_INITDAT:
                        final int tid = msg.arg1;
                        sysApplication.getThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                initGoods(tid);
                                initGoodsType();
                                initAllGoods();
                            }
                        });

                        break;
                    case NOTIFY_SUCCESS:
                        if (successFlag == 3)
                            jumpActivity();
                        break;
                }

                return false;
            }
        });

    }

    /****************************************************************************************/


    public void setInitView() {
        reBoot = getIntent().getBooleanExtra(SettingsActivity.IS_RE_BOOT, false);
        try {
            confirmBtn = findViewById(R.id.home_confirm_btn);
            versionTv = findViewById(R.id.tv_version);
            versionTv.setText("V" + CommonUtils.getVersionName(this));
            cardNumberTv = findViewById(R.id.home_card_number_tv);
            pwdTv = findViewById(R.id.home_pwd_tv);
            TextView loginTv = findViewById(R.id.home_login_tv);
            weightTv = findViewById(R.id.home_weight_number_tv);
            savePwdCtv = findViewById(R.id.home_save_pwd_ctv);
            savePwdCtv.setChecked(AccountManager.getInstance().getRememberPwdState());
            autoLogin = findViewById(R.id.home_login_auto);
            autoLogin.setOnClickListener(this);
            boolean autoLoin = (boolean) SPUtils.get(this, AUTO_LOGIN, false);
            autoLogin.setChecked(autoLoin);
            pwdTv.setOnClickListener(this);
            loginTv.setOnClickListener(this);

            findViewById(R.id.btnTest22).setOnClickListener(this);
            cardNumberTv.setOnClickListener(this);
            confirmBtn.setOnClickListener(this);
            savePwdCtv.setOnClickListener(this);
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            //获取屏幕数量
            Display[] presentationDisplays = new Display[0];
            if (displayManager != null) {
                presentationDisplays = displayManager.getDisplays();
            }

            if (presentationDisplays.length > 1) {
                if (banner == null) {
                    banner = new SecondPresentation(this.getApplicationContext(), presentationDisplays[1]);
                    Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    banner.show();
                }
            }

            if (!banner.isShowing())
                banner.show();
        } catch (Exception e) {
            LogBean logBean = new LogBean();
            logBean.setMessage(e.getMessage());
            logBean.setTime(DateUtils.getYY_TO_ss(new Date()));
            logBean.setType(sysApplication.TYPE_ERROR);
            logBean.setLocation(getLocalClassName());
            sysApplication.getBaseDao().insert(logBean);
        }


    }


    /**
     * 上下文对象
     */
    private Context context;
    UserInfoDao<UserInfo> userInfoDao;


    public void initView() {
        context = this;
        findViewById(R.id.ivLog).setOnClickListener(this);
        userInfoDao = new UserInfoDao<>(context);

        startLogin();

//        if (NetworkUtil.isConnected(this)) {
//            getSettingData(MacManager.getInstace(this).getMac());
//        }
//        getScalesIdByMac(MacManager.getInstace(this).getMac());


    }

    private void startLogin() {
        UserInfo userInfo = userInfoDao.queryById(1);
        SysApplication application = (SysApplication) getApplication();
        if (userInfo == null) {
            //进行 信息获取
            NetHelper netHelper = new NetHelper(application, this);
            netHelper.getUserInfo(netHelper.getIMEI(HomeActivity.this), 1);
        } else {
            application.setMarketid(userInfo.getMarketid());
            application.setMarketname(userInfo.getMarketname());
            application.setCompanyno(userInfo.getCompanyno());
            application.setTid(userInfo.getTid());

            application.setSeller(userInfo.getSeller());
            application.setSellerid(userInfo.getSellerid());
            application.setKey(userInfo.getKey());
            application.setMchid(userInfo.getMchid());
            jumpActivity();
        }
    }

    private void jumpActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initAutoLogin() {
        String lastSerialNumber = AccountManager.getInstance().getLastSerialNumber();
        boolean serialNumberEmpty = TextUtils.isEmpty(lastSerialNumber);
        String pwd = "";
        if (!serialNumberEmpty) {
            pwd = AccountManager.getInstance().getPwdBySerialNumber(lastSerialNumber);
            cardNumberTv.setText(lastSerialNumber);
            pwdTv.setText(pwd);
        }
        if (reBoot || !autoLogin.isChecked() || serialNumberEmpty || TextUtils.isEmpty(pwd)) return;
        CustomDialog.Builder builder;
        builder = new CustomDialog.Builder(this);
        builder.setMessage("自动登录中...").setSingleButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAutoLogin = true;
            }
        });
        final CustomDialog singleButtonDialog = builder.createSingleButtonDialog();
        singleButtonDialog.show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                singleButtonDialog.dismiss();
                if (cancelAutoLogin) return;
                if (confirmBtn != null) confirmBtn.callOnClick();
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadStatus = true;

    }

//    @Override
//    public void onEventMainThread(BusEvent event) {
//        super.onEventMainThread(event);
//        if (event.getType() == BusEvent.NET_WORK_AVAILABLE) {
//            boolean available = event.getBooleanParam();
//            if (available) {
//                getScalesIdByMac(MacManager.getInstace(this).getMac());
//                getSettingData(MacManager.getInstace(this).getMac());
//            }
//        }
//    }


    @Override
    public void onClick(View v) {
        SoftKeyborad.Builder builder = new SoftKeyborad.Builder(HomeActivity.this);
        switch (v.getId()) {
            case R.id.home_login_tv:
//                openGpinter();
                break;

            case R.id.home_confirm_btn:
                startLogin();

//                String serialNumber = cardNumberTv.getText().toString();
//                AccountManager.getInstance().saveLastSerialNumber(serialNumber);
//                String password = pwdTv.getText().toString();
//                if (TextUtils.isEmpty(serialNumber) && getString(R.string.Administrators_pwd).equals(password)) {
//                    startDDMActivity(SettingsActivity.class, true);
//                }
//
//                if (NetworkUtil.isConnected(this)) {
//                    LinkedHashMap valueMap = (LinkedHashMap) SPUtils.readObject(this, KEY_DEFAULT_LOGIN_TYPE);
//                    String value = "";
//                    if (valueMap != null) {
//                        value = valueMap.get("val").toString();
//                    }
//                    loginType = SystemSettingManager.default_login_type();
//                    if (TextUtils.equals(loginType, "卖方卡") || TextUtils.equals(loginType, "3.0") || TextUtils.isEmpty(loginType)) {
//                        clientLogin(weightId + "", serialNumber, password);
//                    } else {
//                        staffMemberLogin(weightId + "", serialNumber, password);
//                    }
//                } else {
//                    if (!TextUtils.isEmpty(cardNumberTv.getText())) {
//                        User user = SQLite.select().from(User.class).where(User_Table.card_number.is(serialNumber)).querySingle();
//                        if (user != null) {
//                            if (TextUtils.equals(pwdTv.getText(), user.password)) {
//                                AccountManager.getInstance().setAdminToken(user.user_token);
//                                startDDMActivity(MainActivity.class, false);
//                                finish();
//                            } else {
//                                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(this, "没有该卡号", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }


                break;
            case R.id.home_card_number_tv:
                if (!ButtonUtils.isFastDoubleClick(R.id.home_card_number_tv)) {
                    builder.create(new SoftKeyborad.OnConfirmedListener() {
                        @Override
                        public void onConfirmed(String result) {
                            cardNumberTv.setText(result);
                            if (AccountManager.getInstance().getPwdBySerialNumber(result) != null) {
                                pwdTv.setText(AccountManager.getInstance().getPwdBySerialNumber(result));
                                savePwdCtv.setChecked(true);
                            } else {
                                savePwdCtv.setChecked(false);
                            }
                        }
                    }).show();
                }
                break;
            case R.id.home_pwd_tv:
                if (!ButtonUtils.isFastDoubleClick(R.id.home_pwd_tv)) {
                    builder.create(new SoftKeyborad.OnConfirmedListener() {
                        @Override
                        public void onConfirmed(String result) {
                            pwdTv.setText(result);
                        }
                    }).show();
                }
                break;
            case R.id.home_save_pwd_ctv:
                savePwdCtv.setChecked(!savePwdCtv.isChecked());
                AccountManager.getInstance().saveRememberPwdState(savePwdCtv.isChecked());
                break;

            case R.id.ivLog:
                //进入日志界面
                Intent intent = new Intent(this, LogActivity.class);
                startActivity(intent);
                break;
            case R.id.home_login_auto:
                autoLogin.setChecked(!autoLogin.isChecked());
                SPUtils.put(this, AUTO_LOGIN, autoLogin.isChecked());
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResponse(VolleyError volleyError, int flag) {
        MyToast.toastShort(context, "网络请求失败");

        switch (flag) {
            case 1:
                break;
            case 2:
                jumpActivity();
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
            case 1:
                if (resultInfo != null) {
                    UserInfo userInfo = JSON.parseObject(resultInfo.getData(), UserInfo.class);
                    if (userInfo != null) {
                        userInfo.setId(1);
                        boolean isSuccess = userInfoDao.updateOrInsert(userInfo);
                        MyLog.blue("测试" + isSuccess);
//                        jumpActivity();

                        SharedPreferences sp = PreferenceUtils.getSp(context);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt(MARKET_ID, userInfo.getMarketid());
                        editor.putInt(TID, userInfo.getTid());
                        editor.putInt(SELLER_ID, userInfo.getSellerid());
                        editor.putString(SELLER, userInfo.getSeller());
                        editor.putString(KEY, userInfo.getKey());
                        editor.putString(MCHID, userInfo.getMchid());
                        editor.apply();
                        Message message = handler.obtainMessage();
                        message.arg1 = userInfo.getTid();
                        message.what = NOTIFY_INITDAT;
                        handler.sendMessage(message);
//                UpdateManager.getNewVersion(HomeActivity.this);
                    }
                } else {
                    MyToast.toastShort(context, "未获取到秤的配置信息");
                }

                break;
            case 2:
                if (resultInfo != null) {
                    if (resultInfo.getStatus() == 0) {
                        List<Goods> goodsList = JSON.parseArray(resultInfo.getData(), Goods.class);
                        if (goodsList != null && goodsList.size() > 0) {

                            goodsDao.insert(goodsList);
                        }
                    }
                }
                successFlag++;
                handler.sendEmptyMessage(NOTIFY_SUCCESS);

//                jumpActivity();
                break;
            case 3:
                if (resultInfo != null) {
                    if (resultInfo.getStatus() == 0) {
                        List<GoodsType> goodsList = JSON.parseArray(resultInfo.getData(), GoodsType.class);
                        if (goodsList != null && goodsList.size() > 0) {

                            goodsTypeDao.insert(goodsList);
                        }
                    }
                }
                successFlag++;
                handler.sendEmptyMessage(NOTIFY_SUCCESS);

//                jumpActivity();
                break;
            case 4:
                if (resultInfo != null) {
                    if (resultInfo.getStatus() == 0) {
                        List<AllGoods> goodsList = JSON.parseArray(resultInfo.getData(), AllGoods.class);
                        if (goodsList != null && goodsList.size() > 0) {

                            allGoodsDao.insert(goodsList);
                        }
                    }
                }
                successFlag++;
                handler.sendEmptyMessage(NOTIFY_SUCCESS);
                break;
        }
    }

    private int successFlag = 0;

    private void initGoods(int tid) {
        String url = "http://119.23.43.64/api/smartsz/getquick?terid=" + tid;
        sysApplication.volleyGet(url, this, 2);
    }

    private void initGoodsType() {
        String url = "http://119.23.43.64/api/smartsz/getproducttype";
        sysApplication.volleyGet(url, this, 3);
    }

    private void initAllGoods() {
        String url = "http://119.23.43.64/api/smartsz/getproducts";
        sysApplication.volleyGet(url, this, 4);
    }
}
