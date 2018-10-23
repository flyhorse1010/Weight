package com.axecom.iweight.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.my.entity.Goods;
import com.axecom.iweight.my.entity.ResultInfo;
import com.axecom.iweight.my.entity.UserInfo;
import com.axecom.iweight.my.entity.dao.GoodsDao;
import com.luofx.listener.VolleyListener;
import com.luofx.utils.PreferenceUtils;
import com.luofx.utils.common.MyToast;
import com.luofx.utils.log.MyLog;

import org.json.JSONObject;

import java.util.List;

public class TestActivity extends AppCompatActivity implements View.OnClickListener,VolleyListener {

   private  SysApplication sysApplication;

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        sysApplication = (SysApplication) getApplication();
        context=this;

        findViewById(R.id.btnTest).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnTest:
                initGoods(sysApplication.getTid());

                break;

        }
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
}
