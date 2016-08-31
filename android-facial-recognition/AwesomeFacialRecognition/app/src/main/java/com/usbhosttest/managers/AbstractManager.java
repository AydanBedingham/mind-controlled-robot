package com.usbhosttest.managers;

import com.usbhosttest.activeobjects.AbstractActiveObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

public abstract class AbstractManager<T> extends AbstractActiveObject implements ObservableClass<T> {

    //Observable
    private ObservableClassHolder<T> observableClassHolder = new ObservableClassHolder<T>();
    @Override public void addListener(T listener) { this.observableClassHolder.addListener(listener); }
    @Override public void removeListener(T listener) { this.observableClassHolder.removeListener(listener); }
    @Override public List<T> getListeners() { return this.observableClassHolder.getListeners(); }

    //Manager State
    public enum ManagerState{
        Initialising,
        Working,
        NotWorking
    }
    private ManagerState previousState;
    private ManagerState managerState;

    public synchronized void setManagerState(ManagerState managerState){
        this.previousState = this.managerState;
        this.managerState = managerState;
        if (this.managerState!=this.previousState){
            onManagerStateChanged(this.previousState, this.managerState);
        }
    }
    public ManagerState getManagerState(){ return this.managerState; }


    private Throwable error;
    public Throwable getError(){ return this.error; }
    public void setError(Throwable error) { this.error = error; }
    public void setError(String errorString) { this.setError(new Exception(errorString)); }
    public void clearError() { this.error = null; }


    public AbstractManager(){
        super();
        this.managerState = ManagerState.NotWorking;
    }

    @Override protected final void onWillStart() { /* No Implementation */ }
    @Override protected final void onWillStop() { /* No Implementation */ }

    protected abstract void onManagerStateChanged(ManagerState oldState, ManagerState newState);
}
