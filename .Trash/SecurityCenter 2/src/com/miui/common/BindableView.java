
package com.miui.common;


public interface BindableView<D> {
    public void fillData(D data);

    public void setEventHandler(EventHandler handler);
}
