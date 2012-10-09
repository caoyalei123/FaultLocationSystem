package com.liubo;

import java.util.ArrayList;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

class SMSItem {
	public static final String ID = "_id";
	public static final String THREAD = "thread_id";
	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String BODY = "body";
	public static final String SUBJECT = "subject";

	public String mAddress;
	public String mBody;
	public String mSubject;
	public long mID;
	public long mThreadID;
	public long mDate;
	public long mRead;
	public long mPerson;

	private static int mIdIdx;
	private static int mThreadIdx;
	private static int mAddrIdx;
	private static int mPersonIdx;
	private static int mDateIdx;
	private static int mReadIdx;
	private static int mBodyIdx;
	private static int mSubjectIdx;
	
	
	public SMSItem(Cursor cur) {
		mID = cur.getLong(mIdIdx);
		mThreadID = cur.getLong(mThreadIdx);
		mAddress = cur.getString(mAddrIdx);
		mPerson = cur.getLong(mPersonIdx);
		mDate = cur.getLong(mDateIdx);
		mRead = cur.getLong(mReadIdx);
		mBody = cur.getString(mBodyIdx);
		mSubject = cur.getString(mSubjectIdx);
	}
	
	public static void initIdx(Cursor cur) {
		mIdIdx = cur.getColumnIndex( ID );
		mThreadIdx = cur.getColumnIndex( THREAD );
		mAddrIdx = cur.getColumnIndex( ADDRESS );
		mPersonIdx = cur.getColumnIndex( PERSON );
		mDateIdx = cur.getColumnIndex( DATE );
		mReadIdx = cur.getColumnIndex( READ );
		mBodyIdx = cur.getColumnIndex( BODY );
		mSubjectIdx = cur.getColumnIndex( SUBJECT );
	}
	
	public String toString() {
		String ret = ID + ":" + String.valueOf(mID) + " \n" +
			THREAD + ":" + String.valueOf(mThreadID) + " \n" +   
			ADDRESS + ":" + mAddress + " \n" + 
			PERSON + ":" + String.valueOf(mPerson) + " \n" + 
			DATE + ":" + String.valueOf(mDate) + " \n" +
			READ + ":" + String.valueOf(mRead) + " \n" + 
			SUBJECT + ":" + mSubject + " \n" + 
			BODY + ":" + mBody; 
		return ret;
	}
}

class ContactItem {
	public String mName;
}

public class SMSReader {
	public Uri SMS_INBOX = Uri.parse("content://sms/inbox");
	private ArrayList<SMSItem> mSmsList = new ArrayList<SMSItem>();
	
	public SMSReader() {
		
	}
	
	SMSItem get(int idx) {
		return mSmsList.get(idx);
	}
	
	int count() {
		return mSmsList.size();
	}
	
	int read(Activity activity) {
		Cursor cur = activity.managedQuery(SMS_INBOX, null, null, null, null);
		if( cur != null && 
			cur.moveToFirst()) {
			SMSItem.initIdx(cur);
			do {
				SMSItem item = new SMSItem(cur);
				mSmsList.add(item);
			} while(cur.moveToNext());
		}
		return count();
	}
	
	ContactItem getContact(Activity activity, final SMSItem sms) {
		if(sms.mPerson == 0) return null;
		Cursor cur = activity.managedQuery(ContactsContract.Contacts.CONTENT_URI, 
				new String[] {PhoneLookup.DISPLAY_NAME}, 
				" _id=?", 
				new String[] {String.valueOf(sms.mPerson)}, null);
		if(cur != null &&
			cur.moveToFirst()) {
			int idx = cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			ContactItem item = new ContactItem();
			item.mName = cur.getString(idx);
			return item;
		}
		return null;
	}
}
