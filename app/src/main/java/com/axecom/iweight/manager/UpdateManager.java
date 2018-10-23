package com.axecom.iweight.manager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.bean.VersionBean;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.activity.MainActivity;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.axecom.iweight.ui.view.CustomDialog;
import com.axecom.iweight.utils.CommonUtils;
import com.google.gson.Gson;
import com.luofx.utils.PreferenceUtils;


import java.io.File;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.shangtongyin.tools.serialport.IConstants_ST.MARKET_ID;


/**
 * Created by Administrator on 2016-9-11.
 */
public class UpdateManager {
   public interface UpdateResult {
        void onResult(boolean hasUpdate);
    }
    public static void getNewVersion(final Activity context, final UpdateResult updateResult) {
        String id = PreferenceUtils.getInt(context, MARKET_ID, -1) + "";
        if("-1".equals(id))return;
        RetrofitFactory.getInstance().API()
                .getVersion(id)
                .compose(((BaseActivity) context).<BaseEntity<VersionBean>>setThread())
                .subscribe(new Observer<BaseEntity<VersionBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseEntity<VersionBean> versionBeanBaseEntity) {
                        if (versionBeanBaseEntity.isSuccess()) {
                            VersionBean version = versionBeanBaseEntity.getData();
                            String versionName = CommonUtils.getVersionName(context);
                            boolean noUpdate = version == null || TextUtils.isEmpty(version.version) || version.version.compareTo(versionName) <= 0;
                            updateResult.onResult(!noUpdate);
                            if (noUpdate){
                                return;
                            }
                            DownloadBuilder builder = AllenVersionChecker
                                    .getInstance()
                                    .downloadOnly(
                                            UIData.create()
                                                    .setDownloadUrl(version.downloadurl)
                                                    .setTitle("更新")
                                                    .setContent(version.description)

                                    );
                            builder.setForceRedownload(true);
//                                    .setSilentDownload(true);
                            builder.excuteMission(context);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
