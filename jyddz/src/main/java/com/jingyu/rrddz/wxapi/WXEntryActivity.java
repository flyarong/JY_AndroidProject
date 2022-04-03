package com.jingyu.rrddz.wxapi;


import com.wxsdk.my.WeChatController;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

    @SuppressWarnings("unused")
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        Log.i("tag", ">>>>>>>>>OnCreate 1");
        api = WXAPIFactory.createWXAPI(this, WeChatController.APP_ID);
        Log.i("tag", ">>>>>>>>>OnCreate 2");
        api.handleIntent(getIntent(), this);
        Log.i("tag", ">>>>>>>>>OnCreate 3");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {   	
        //JSONObject json = new JSONObject();
        UnityPlayer.UnitySendMessage("SDK_callback", "Log",resp.transaction);
        try{
            switch (resp.transaction)
            {
                case WeChatController.Transaction.RequestLogin:
                    SendAuth.Resp auth = (SendAuth.Resp)resp;
                    //json.put("token", auth.token);
                    if(auth.errCode != 0) {
                        if(auth.errStr != null)
                            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatError", auth.errStr);
                        else
                            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatError", Integer.toString(resp.errCode));
                    } else
                        UnityPlayer.UnitySendMessage("SDK_callback", "OnLoginSuc",auth.token);

                    UnityPlayer.UnitySendMessage("SDK_callback", "Log","token" + resp.toString());
                    break;
                case WeChatController.Transaction.ShareImage:
                case WeChatController.Transaction.ShareMusic:
                case WeChatController.Transaction.ShareText:
                case WeChatController.Transaction.ShareUrl:
                case WeChatController.Transaction.ShareVideo:
                    if(resp.errCode != 0) {
                        if(resp.errStr != null)
                            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatShare", resp.errStr);
                        else
                            UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatShare", Integer.toString(resp.errCode));
                    } else
                        UnityPlayer.UnitySendMessage("SDK_callback", "OnWeChatShare","OK");
                    break;
            }
        }catch (Exception e)
        {

        }
        

        finish();///
    }

    private void goToGetMsg() {
//        Intent intent = new Intent(this, GetFromWXActivity.class);
//        intent.putExtras(getIntent());
//        startActivity(intent);
//        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

        StringBuffer msg = new StringBuffer(); // ��֯һ������ʾ����Ϣ����
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);

        finish();
    }
}