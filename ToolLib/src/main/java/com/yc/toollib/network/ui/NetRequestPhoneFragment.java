package com.yc.toollib.network.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.yc.toollib.BuildConfig;
import com.yc.toollib.R;
import com.yc.toollib.network.data.IDataPoolHandleImpl;
import com.yc.toollib.network.data.NetworkFeedBean;
import com.yc.toollib.network.ping.PingView;
import com.yc.toollib.network.utils.NetDeviceUtils;
import com.yc.toollib.network.utils.NetWorkUtils;
import com.yc.toollib.network.utils.NetworkTool;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NetRequestPhoneFragment extends Fragment {

    private Activity activity;
    private TextView tvPhoneContent;
    private TextView tvContentInfo;
    private TextView tvWebInfo;
    private PingView tvNetInfo;
    private List<NetworkFeedBean> mNetworkFeedList;
    private static final int MESSAGE = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE){
                String content = (String) msg.obj;
                tvWebInfo.setText(content);
            }
        }
    };


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_phone_info, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFindViewById(view);
        initData();
    }


    private void initFindViewById(View view) {
        tvPhoneContent = view.findViewById(R.id.tv_phone_content);
        tvContentInfo = view.findViewById(R.id.tv_content_info);
        tvWebInfo = view.findViewById(R.id.tv_web_info);
        tvNetInfo = view.findViewById(R.id.tv_net_info);
    }

    private void initData() {
        initListData();
        //手机设备信息
        setPhoneInfo();

        //本机信息
        //比如mac地址，子网掩码，ip，wifi名称
        setLocationInfo();

        //服务端信息
        //也就是host，域名ip，域名mac，域名是否正常，是ipv4还是ipv6
        setNetInfo();

        //网络诊断，也就是ping一下
        setPingInfo();
    }

    private void initListData() {
        mNetworkFeedList = new ArrayList<>();
        HashMap<String, NetworkFeedBean> networkFeedMap = IDataPoolHandleImpl.getInstance().getNetworkFeedMap();
        if (networkFeedMap != null) {
            Collection<NetworkFeedBean> values = networkFeedMap.values();
            mNetworkFeedList.addAll(values);
            try {
                Collections.sort(mNetworkFeedList, new Comparator<NetworkFeedBean>() {
                    @Override
                    public int compare(NetworkFeedBean networkFeedModel1, NetworkFeedBean networkFeedModel2) {
                        return (int) (networkFeedModel2.getCreateTime() - networkFeedModel1.getCreateTime());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPhoneInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        //版本信息
        String versionName = "";
        String versionCode = "";
        try {
            PackageManager pm = application.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = String.valueOf(pi.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("软件App报名:").append(NetworkTool.getInstance().getApplication().getPackageName());
        sb.append("\n是否是DEBUG版本:").append(BuildConfig.BUILD_TYPE);
        sb.append("\n是否root:").append(NetDeviceUtils.isDeviceRooted());
        sb.append("\n系统硬件商:").append(NetDeviceUtils.getManufacturer());
        sb.append("\n设备的品牌:").append(NetDeviceUtils.getBrand());
        sb.append("\n手机的型号:").append(NetDeviceUtils.getModel());
        sb.append("\n设备版本号:").append(NetDeviceUtils.getId());
        sb.append("\nCPU的类型:").append(NetDeviceUtils.getCpuType());
        sb.append("\n系统的版本:").append(NetDeviceUtils.getSDKVersionName());
        sb.append("\n系统版本值:").append(NetDeviceUtils.getSDKVersionCode());
        if (versionName!=null && versionName.length()>0){
            sb.append("\n当前的版本:").append(versionName).append("—").append(versionCode);
        }
        tvPhoneContent.setText(sb.toString());
        tvPhoneContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetWorkUtils.copyToClipBoard(activity,sb.toString());
            }
        });
    }

    private void setLocationInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        StringBuilder sb = new StringBuilder();
        sb.append("wifi信号强度:").append(NetDeviceUtils.getWifiState(application));
        sb.append("\nAndroidID:").append(NetDeviceUtils.getAndroidID(application));
        sb.append("\nMac地址:").append(NetDeviceUtils.getMacAddress(application));
        sb.append("\nWifi名称:").append(NetDeviceUtils.getWifiName(application));
        int wifiIp = NetDeviceUtils.getWifiIp(application);
        String ip = NetDeviceUtils.intToIp(wifiIp);
        sb.append("\nWifi的Ip地址:").append(ip);
        DhcpInfo dhcpInfo = NetDeviceUtils.getDhcpInfo(application);
        if (dhcpInfo!=null){
            //sb.append("\nipAddress：").append(NetDeviceUtils.intToIp(dhcpInfo.ipAddress));
            sb.append("\n子网掩码地址：").append(NetDeviceUtils.intToIp(dhcpInfo.netmask));
            sb.append("\n网关地址：").append(NetDeviceUtils.intToIp(dhcpInfo.gateway));
            sb.append("\nserverAddress：").append(NetDeviceUtils.intToIp(dhcpInfo.serverAddress));
            sb.append("\nDns1：").append(NetDeviceUtils.intToIp(dhcpInfo.dns1));
            sb.append("\nDns2：").append(NetDeviceUtils.intToIp(dhcpInfo.dns2));
        }
        tvContentInfo.setText(sb.toString());
    }

    private void setNetInfo() {
        if (mNetworkFeedList.size()>0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String curl = mNetworkFeedList.get(0).getCURL();
                    String host = Uri.parse(curl).getHost();
                    StringBuilder sb = new StringBuilder();
                    sb.append("域名ip地址:").append(NetDeviceUtils.getHostIP(host));
                    sb.append("\n域名host名称:").append(NetDeviceUtils.getHostName(host));
                    sb.append("\n待完善:").append("获取服务端ip，mac，是ipv4还是ipv6等信息");
                    String string = sb.toString();
                    Message message = new Message();
                    message.what = MESSAGE;
                    message.obj = string;
                    handler.sendMessage(message);
                }
            }).start();
        }
    }

    private void setPingInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        tvNetInfo.setDeviceId(NetDeviceUtils.getAndroidID(application));
        tvNetInfo.setUserId(application.getPackageName());
        //版本信息
        String versionName = "";
        try {
            PackageManager pm = application.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            if (pi != null) {
                versionName = pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvNetInfo.setVersionName(versionName);
        if (mNetworkFeedList.size()>0){
            String curl = mNetworkFeedList.get(0).getCURL();
            String host = Uri.parse(curl).getHost();
            tvNetInfo.pingHost(host);
        }
    }

}
