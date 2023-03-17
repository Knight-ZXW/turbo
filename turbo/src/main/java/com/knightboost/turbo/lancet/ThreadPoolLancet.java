package com.knightboost.turbo.lancet;

import com.knightboost.lancet.api.This;
import com.knightboost.lancet.api.annotations.Group;

import java.util.concurrent.ThreadPoolExecutor;

@Group("turbo")
public class ThreadPoolLancet {

    public void execute(){
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) This.get();

    }
}
