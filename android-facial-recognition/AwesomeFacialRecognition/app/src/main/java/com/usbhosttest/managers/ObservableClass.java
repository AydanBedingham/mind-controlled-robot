package com.usbhosttest.managers;

import java.util.List;

public interface ObservableClass <T> {

    void addListener(T listener);

    void removeListener(T listener);

    List<T> getListeners();

}
