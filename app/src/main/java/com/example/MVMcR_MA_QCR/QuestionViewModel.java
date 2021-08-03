package com.example.MVMcR_MA_QCR;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class QuestionViewModel extends ViewModel {
public ArrayList<Questions_main> list=new ArrayList<>();
public ArrayList<String> pnames;
public float count=0;
public float partcount=0;
public float devidedparts=0;
public String partname="";

//    public ArrayList<Questions_main> getList() {
//        return list;
//    }
//
//    public void setList(ArrayList<Questions_main> list) {
//        this.list = list;
//    }
}
