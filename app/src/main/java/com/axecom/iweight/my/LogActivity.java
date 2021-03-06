package com.axecom.iweight.my;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.axecom.iweight.R;
import com.axecom.iweight.my.adapter.LogRVAdapter;
import com.axecom.iweight.my.entity.LogBean;
import com.luofx.entity.dao.BaseDao;
import com.luofx.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogActivity extends Activity {

    private LogRVAdapter adapter;

    private void initView() {
        initRecyclerView();
    }

    /**
     * 初始化控件
     */
    private void initRecyclerView() {
        RecyclerView rvLog = findViewById(R.id.rvLog);
        LinearLayoutManager connectedLayoutManager = new LinearLayoutManager(context);
        connectedLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvLog.setLayoutManager(connectedLayoutManager);
        // list 大小是否固定 ，则不用刷新
        rvLog.setHasFixedSize(true);
        adapter = new LogRVAdapter();
        rvLog.setAdapter(adapter);
    }

    /**
     *  初始
     */
    private void initData(){
//        LogBean logBean=new LogBean();
//        logBean.setLocation(this.getPackageCodePath());
//        logBean.setTime(DateUtils.getYY_TO_ss(new Date()));
//        logBean.setType("Info");
//        logBean.setMessage("异常测试");
//
//        LogBean logBean2=new LogBean();
//        logBean2.setLocation(this.getPackageCodePath());
//        logBean2.setTime(DateUtils.getYY_TO_ss(new Date()));
//        logBean2.setType("error");
//        logBean2.setMessage("异常测试2");
//        List<LogBean > list=new ArrayList<>();
//        list.add(logBean2);
//        list.add(logBean);

        BaseDao<LogBean> baseDao=new BaseDao<>(context,LogBean.class);
        List<LogBean > list= baseDao.queryAll();

        adapter.setData(list);
    }


    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        context = this;
        initView();
        initData();
    }
}
