package com.example.MVMcR_MA_QCR;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    private static MySingleton mInstance;
    private static  RequestQueue requestQueue;
    private static Context context;

    private MySingleton(Context ctx) {
        context = ctx;
        requestQueue=getRequestQueue();
    }

    public static synchronized MySingleton getInstance(Context context){
        if(mInstance==null){
            mInstance=new MySingleton(context);

        }
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue==null){
            requestQueue= Volley.newRequestQueue(context);
        }
        return requestQueue;
    }
    public <T>void addToRequestQue(Request<T> request){
        requestQueue.add(request);
    }
}
