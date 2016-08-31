package com.usbhosttest.activeobjects;


import android.os.Looper;
import android.util.Log;

import com.usbhosttest.utils.ThreadUtil;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractActiveObject {

    public enum ActiveObjectState{
        WaitingToStart,
        Started,
        WaitingToStop,
        Stopped,
    }

    public AbstractActiveObject(){
        super();
        this.activeObjectState = ActiveObjectState.Stopped;
    }

    private Lock lockObj = new ReentrantLock();

    private ActiveObjectState activeObjectState;
    public ActiveObjectState getActiveObjectState(){ return this.activeObjectState; }

    private Thread myThread;

    public synchronized void start(){

        switch (this.getActiveObjectState()){
            case WaitingToStart:
                Log.w("" + this.hashCode(), "Already waiting to start");
                return;
            case Started:
                Log.w(""+this.hashCode(),"Already started");
                return;
        }

        try{
            lockObj.lock();

            this.activeObjectState = ActiveObjectState.WaitingToStart;
            onWillStart();

            executeThread();

            while(this.activeObjectState != ActiveObjectState.Started){
                ThreadUtil.sleep(5);
            }

        } finally {
            lockObj.unlock();
        }
    }

    private void executeThread(){

        final AbstractActiveObject aao = this;

        myThread = new Thread(){
            public void run() {
                activeObjectState = ActiveObjectState.Started;
                onDidStart();
                while(aao.getActiveObjectState() != ActiveObjectState.WaitingToStop) {
                    onExecute();
                }
                aao.activeObjectState = ActiveObjectState.Stopped;
                onDidStop();
            }
        };
        myThread.start();
    }


    public synchronized void stop(){
        switch (this.getActiveObjectState()){
            case WaitingToStop:
                Log.w(""+this.hashCode(),"Already waiting to stop");
                return;
            case Stopped:
                Log.w(""+this.hashCode(),"Already stopped");
                return;
        }

        try{
            lockObj.lock();

            this.activeObjectState = ActiveObjectState.WaitingToStop;
            onWillStop();

            if (Thread.currentThread() != this.myThread) {
                while (this.activeObjectState != ActiveObjectState.Stopped) {
                    ThreadUtil.sleep(5);
                }
            }

        } finally {
            lockObj.unlock();
        }
    }


    protected abstract void onWillStart();
    protected abstract void onDidStart();

    protected abstract void onExecute();

    protected abstract void onWillStop();
    protected abstract void onDidStop();

}
