package com.liubo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class MessageUtil {

	/**
	 * 显示信息
	 * @param context
	 * @param title
	 * @param message
	 */
	public static void showMessage(Context context ,String title ,String message)
	{
		AlertDialog.Builder multiDia=new AlertDialog.Builder(context);
        multiDia.setTitle(title);
        multiDia.setMessage(message);
        multiDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        multiDia.show();
	}
	
}
