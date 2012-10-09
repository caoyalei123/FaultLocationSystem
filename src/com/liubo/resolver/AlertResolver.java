package com.liubo.resolver;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.SmsMessage;

import com.liubo.db.TowerDao;
import com.liubo.modal.Alert;
import com.liubo.util.StringUtils;

public class AlertResolver {

	public static Alert resolve(SmsMessage smsMessage){
		if(smsMessage != null){
			String address = smsMessage.getOriginatingAddress();
			if(address.length() > 11){
				address = address.substring(address.length()- 11);
			}
			String messageBody = smsMessage.getDisplayMessageBody().trim();
			return resolve(messageBody,address);
		}
		return null;
	}
	
	public static Alert resolve(String messageBody,String address){
		if(messageBody.startsWith("<") && messageBody.endsWith(">")){
			String bodyInfo[] = messageBody.split(",");
			if(bodyInfo.length != 3){
				return null;
			}
			String substationNum = parseItem(bodyInfo[0]);
			String circuitNum = parseItem(bodyInfo[1]);
			String towerNum = parseItem(bodyInfo[2]);
			if(StringUtils.isNotEmpty(substationNum) && StringUtils.isNotEmpty(circuitNum) && StringUtils.isNotEmpty(towerNum))
			{
				Alert alert = new Alert();
				alert.setSubstationNum(substationNum);
				alert.setCircuitNum(circuitNum);
				alert.setTowerNum(towerNum);
				alert.setAddressNum(address);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String time = df.format(new Date());
				alert.setDate(time);
				alert.setOK(false);
				return alert;
			}
		}
		return null;
	}
	
	/**
	 * 判断是否符合格式<**编号>
	 * @param msgItem
	 * @return
	 */
	private static String parseItem(String msgItem){
		if(msgItem.startsWith("<") && msgItem.endsWith(">")){
			msgItem = msgItem.substring(1, msgItem.length()-1) ;
			return msgItem;
		}
		return null;
	}
}
