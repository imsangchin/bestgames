package com.wandoujia.tools.blr.task;


public abstract class BaseTask {
    
    public abstract void preExecute();
    
    public abstract boolean isSatisfied();
    
    public abstract void execute();

    public abstract void postExecute();
    
}
