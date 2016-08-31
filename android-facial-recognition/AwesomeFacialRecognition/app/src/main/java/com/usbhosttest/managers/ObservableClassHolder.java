package com.usbhosttest.managers;

import java.util.ArrayList;
import java.util.List;

public class ObservableClassHolder<T> implements ObservableClass<T> {

    private List<T> listeners;

    public ObservableClassHolder(){
        this.listeners = new ArrayList<T>();
    }

    @Override
    public void addListener(T listener) {
        if (!this.listeners.contains(listener)){
            this.listeners.add(listener);
        }
    }

    @Override
    public void removeListener(T listener) {
        if (this.listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public List<T> getListeners() {
        return new ArrayList(this.listeners);
    }
}
