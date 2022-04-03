package com.jingyu.rrddz;

import com.unity3d.player.*;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import org.json.JSONObject;

import java.security.Permission;
import java.util.List;

//umeng
import com.umeng.message.PushAgent;
//weixin
import com.wxsdk.my.WeChatController;

public class UnityPlayerActivity extends Activity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

    protected final int REQUEST_ALL_PERMISSION = 1000;

    /**
     * 从deep link中获取数据
     */
    private void getDataFromBrowser(Intent intent) {
        Uri data = intent.getData();
        try {
            String scheme = data.getScheme(); // "will"
            String host = data.getHost(); // "share"
            List<String> params = data.getPathSegments();
            //获取指定参数值
            String goodsId = data.getQueryParameter("goodsId");
            String str = "Scheme: " + scheme + "," + "host: " + host + "," + "params: " + goodsId;
			m_startData = str;
			Log.e("NMGNMG", m_startData);
        } catch (Exception e) {
            Log.e("NMGNMG", "ERRORERROR");
            e.printStackTrace();
        }
    }
	private String m_startData = "";
	private Vibrator m_vibrator;
	public String GetStartData()
	{
		String str = new String(m_startData);
		m_startData = "";
		return str;
	}
	public void RunVibrator(long tt)
	{
		m_vibrator.vibrate(tt);
	}
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_ALL_PERMISSION)
            PushAgent.getInstance(this).onAppStart();
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //判断是否安装了微信
    public boolean IsWXInstall() {
        final PackageManager packageManager = this.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if(pinfo != null) {
            for(int idx = 0; idx < pinfo.size(); ++idx) {
                String pn = pinfo.get(idx).packageName;
                if(pn.equals("com.tencent.mm"))
                    return true;
            }
        }
        return false;
    }
    // 默认是微信登录
	public void Login(String appId) {
        if(!IsWXInstall()) {
            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatError","NeedInstall");
            return;
        }

		// 先注册appid
		//WeChatController.GetInstance().RegisterToWeChat(this, appId);
		// 登录
		WeChatController.GetInstance().WeChatLogin();
	}

	public void RegisterToWeChat(String appId) {
		WeChatController.GetInstance().RegisterToWeChat(this, appId);
	}

	public void StartAc(String appId) {
		/*Toast.makeText(MainActivity.Instance, "////////////",
				Toast.LENGTH_SHORT).show();*/
		Toast.makeText(this, "////////////",
				Toast.LENGTH_SHORT).show();
	}

	public void WeChatLogin(String param) {
		WeChatController.GetInstance().WeChatLogin();
	}

	public void WeChat(String param) {
		try {
			JSONObject jsonObject = new JSONObject(param);
			int _type = jsonObject.getInt("type");
			WeChatController con = WeChatController.GetInstance();
			switch (_type) {
			case WeChatController.Type.WeiChatInterfaceType_IsWeiChatInstalled:
				break;
			case WeChatController.Type.WeiChatInterfaceType_RequestLogin:
				con.WeChatLogin();
				break;
			case WeChatController.Type.WeiChatInterfaceType_ShareUrl:
				con.ShareLinkUrl(jsonObject);
				break;
			case WeChatController.Type.WeiChatInterfaceType_ShareImage:
				con.ShareImage(jsonObject);
				break;
			case WeChatController.Type.WeiChatInterfaceType_ShareText:
				con.ShareText(jsonObject);
				break;
			case WeChatController.Type.WeiChatInterfaceType_ShareVideo:
				con.ShareVideo(jsonObject);
				break;
			case WeChatController.Type.WeiChatInterfaceType_ShareMusic:
				con.ShareMusic(jsonObject);
				break;
			}
		} catch (Exception e) {
            UnityPlayer.UnitySendMessage("SDK_callback", "Log","WeChat Exception: " + e.toString());
            //UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatError", e.toString());
		}

	}

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        if(Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[] {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_LOGS,
                    Manifest.permission.SET_DEBUG_APP,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_APN_SETTINGS
            };
            requestPermissions(mPermissionList, REQUEST_ALL_PERMISSION);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        WeChatController.GetInstance().SetMainActivity(this);
        //注册appid
        WeChatController.GetInstance().RegisterToWeChat(this, "wxf64ec3fb99c28771");

        Intent intent = getIntent();
        getDataFromBrowser(intent);
		
		m_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
