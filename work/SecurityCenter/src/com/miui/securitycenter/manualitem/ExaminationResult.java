
package com.miui.securitycenter.manualitem;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExaminationResult implements IJSONable {

    protected String mJsonStr;

    private static final String SUCCESS_STATUS = "Success";

    private long mVersion;
    private boolean isSuccess = false;
    private int totalCount;
    private List<ExaminationDataItem> mExaminationDataList;

    private static final String VERSION = "version";
    private static final String ITEMS = "items";
    private static final String TOTALCOUNT = "totalCount";

    public ExaminationResult(String jsonStr) {
        isSuccess = parseJson(jsonStr);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public List<ExaminationDataItem> getmExaminationDataList() {
        return mExaminationDataList;
    }

    @Override
    public boolean parseJson(String jsonStr) {
        // TODO Auto-generated method stub
        mJsonStr = jsonStr;

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObj == null) {
            return false;
        }
        if (jsonObj.has("error_code")) {
            return false;
        }

        mVersion = jsonObj.optLong(VERSION);
        JSONArray jsonArr = jsonObj.optJSONArray(ITEMS);
        totalCount = jsonObj.optInt(TOTALCOUNT);

        if (jsonArr == null) {
            return false;
        }

        if (jsonArr.length() != totalCount) {
            return false;
        }
        mExaminationDataList = new ArrayList<ExaminationDataItem>();
        for (int i = 0; i < jsonArr.length(); i++) {
            try {
                ExaminationDataItem item = new ExaminationDataItem(jsonArr.getJSONObject(i));
                mExaminationDataList.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public String toJson() {
        // TODO Auto-generated method stub
        return mJsonStr;
    }

    public static class ExaminationDataItem {
        private int mId;
        private String mCategory;
        private int mScore;
        private int mOrder;
        private boolean mChecked;

        private static final String ID = "id";
        private static final String CATEGORY = "category";
        private static final String SCORE = "score";
        private static final String ORDER = "order";
        private static final String CHECKED = "checked";

        public ExaminationDataItem(JSONObject jsonObj) {
            if (jsonObj != null) {
                mId = jsonObj.optInt(ID);
                mCategory = jsonObj.optString(CATEGORY);
                mScore = jsonObj.optInt(SCORE);
                mOrder = jsonObj.optInt(ORDER);
                mChecked = jsonObj.optBoolean(CHECKED);
            }
        }

        public int getId() {
            return mId;
        }

        public String getCategory() {
            return mCategory;
        }

        public boolean isChecked() {
            return mChecked;
        }

        public int getScore() {
            return mScore;
        }

        public int getOrder() {
            return mOrder;
        }
    }

}
