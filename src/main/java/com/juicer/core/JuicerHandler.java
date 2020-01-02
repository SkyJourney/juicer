package com.juicer.core;

public interface JuicerHandler extends HrefSupplier, DomParser{

    boolean hasParent();

    String getParent();

}
