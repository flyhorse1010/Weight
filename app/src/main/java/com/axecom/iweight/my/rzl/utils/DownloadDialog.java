package com.axecom.iweight.my.rzl.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.axecom.iweight.R;

import java.text.DecimalFormat;
//版本更新对话框，显示版本控制信息和下载进度，下载完成自动安装
public class DownloadDialog extends Dialog {

    public DownloadDialog(@NonNull Context context) {
        super(context);
        canDismiss=false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_apk);

        tvDate=findViewById(R.id.tvDownload_Apk_Date);
        tvVersion=findViewById(R.id.tvDownload_Apk_Version);
        tvDescription=findViewById(R.id.tvDownload_Apk_Description);
        tvProgress=findViewById(R.id.tvDownload_Apk_Progress);
        pbProgress=findViewById(R.id.pbDownload_Apk_Progress);
        Log.i("rzl","tvVersion is null ? " + (tvVersion==null));
        tvDate.setText("更新日期:" + this.getDate());
        tvVersion.setText("版本号:" + this.getVersion());
        tvDescription.setText("更新内容:" + this.getDescription());
        tvProgress.setText("更新进度:" + this.getDownloadedBytes() + "/" + this.getTotalBytes());
        pbProgress.setMax(0);
    }

    private TextView tvDate;
    private TextView tvVersion;
    private TextView tvDescription;
    private TextView tvProgress;
    private ProgressBar pbProgress;

    private int downloadedBytes;//已下载量
    private int totalBytes;//总下载量
    private String description;//更新描述
    private float version;//最新版本号
    private String date;//更新日期
    private boolean canDismiss;//是否可退出（必须等下载完毕之后才能退出）
    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(int downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
      //  tvDescription.setText("更新内容:" + description);
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
     //   tvVersion.setText("版本号:"+version);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
       // tvDate.setText("更新日期:" + date);
    }

    public void setProgress(int _downloadedBytes,int _totalBytes){
        this.setDownloadedBytes(_downloadedBytes);
        this.setTotalBytes(_totalBytes);
        if(tvProgress!=null){
            DecimalFormat df=new DecimalFormat("#0.00");
            String progress="0%";
            if(_totalBytes>0){
                pbProgress.setMax(_totalBytes);
                pbProgress.setProgress(_downloadedBytes);
                progress= df.format(100.0f*_downloadedBytes/_totalBytes) + "%";
            }
            if(_downloadedBytes==_totalBytes){
                tvProgress.setText("更新完成");
                canDismiss=true;
                dismiss();
            }else{
                tvProgress.setText("更新进度:" + progress);
            }
        }
    }

    @Override
    public void dismiss() {
        if(this.canDismiss){
            super.dismiss();
        }else{
            Log.i("rzl","you should wait for download complete before dismissing dialog");
        }
    }
}
