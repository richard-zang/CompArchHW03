package cis501.submission;

import cis501.IOOOLoadStoreQueue;
import cis501.LoadHandle;
import cis501.StoreHandle;

import java.util.Collection;

public class OOOLoadStoreQueue implements IOOOLoadStoreQueue {

    public OOOLoadStoreQueue(int loadQCapacity, int storeQCapacity) {
    }

    @Override
    public boolean roomForLoad() {
        return false;
    }

    @Override
    public boolean roomForStore() {
        return false;
    }

    @Override
    public void commitOldest() {

    }

    @Override
    public LoadHandle dispatchLoad(int size) {
        return null;
    }

    @Override
    public StoreHandle dispatchStore(int size) {
        return null;
    }

    @Override
    public long executeLoad(LoadHandle handle, long address) {
        return 0;
    }

    @Override
    public Collection<? extends LoadHandle> executeStore(StoreHandle handle, long address, long value) {
        return null;
    }
}
