
package com.miui.common;


public interface BindableGroupView<D> {

    public void fillData(D data, int groupPos);

    public void setEventHandler(EventHandler handler);

    public void setExpanded(boolean expanded);
}
