package cis501.submission;

import cis501.IOOOLoadStoreQueue;
import cis501.LoadHandle;
import cis501.StoreHandle;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.*;

//========================================================================================
class Load implements LoadHandle{
    /* Things known at construction time. */
    Load(int birthday, int bytesRead){
        this.birthday = birthday;
        this.bytesRead = bytesRead;
        this.addresses = new LinkedList<>();
        this.storeBirthdays = new LinkedList<>();
    }

    public int birthday;
    public List<Long> addresses;
    public List<Integer> storeBirthdays;
    public boolean hasExecuted = false;
    public int bytesRead = -1;
}
//========================================================================================
class Store implements StoreHandle{
    /* Things known at construction time. */
    Store(int birthday, int bytesWritten){
        this.birthday = birthday;
        this.bytesWritten = bytesWritten;
        this.addresses = new LinkedList<>();
        this.values = new LinkedList<>();
    }

    public int birthday;
    public List<Long> addresses;
    public List<Long> values;
    public boolean hasExecuted = false;
    public int bytesWritten = -1;
}
//========================================================================================
public class OOOLoadStoreQueue implements IOOOLoadStoreQueue {

    final int loadQCapacity;
    final int storeQCapacity;
    LinkedList<Load> loadQueue;
    LinkedList<Store> storeQueue;
    int birthCounter = 0;
    //====================================================================================
    public OOOLoadStoreQueue(int loadQCapacity, int storeQCapacity) {
        this.loadQCapacity = loadQCapacity;
        this.storeQCapacity = storeQCapacity;

        loadQueue = new LinkedList<>();
        storeQueue = new LinkedList<>();
    }
    //====================================================================================
    @Override
    public boolean roomForLoad() {
        return loadQueue.size() < loadQCapacity;
    }
    //====================================================================================
    @Override
    public boolean roomForStore() {
        return storeQueue.size() < loadQCapacity;
    }
    //====================================================================================
    @Override
    public void commitOldest() {
        boolean loadEmpty = loadQueue.isEmpty();
        boolean storeEmpty = storeQueue.isEmpty();

        if(loadEmpty && storeEmpty) return;

        Load l = (! loadEmpty) ? loadQueue.get(0) : null;
        Store s = (! storeEmpty) ? storeQueue.get(0) : null;

        // Both non-empty. Compare and commit oldest.
        if(! loadEmpty && ! storeEmpty)
            if(l.birthday > s.birthday) storeQueue.remove(0); else loadQueue.remove(0);

        // Only one of them empty...
        if(storeEmpty) storeQueue.remove(0); else loadQueue.remove(0);

        return;
    }
    //====================================================================================
    /**
     * Create a new load, add it to the end of the loadQueue and return a handle
     * to the user.
     */
    @Override
    public LoadHandle dispatchLoad(int size) {
        Load l = new Load(birthCounter, size);
        birthCounter++;
        loadQueue.add(l);
        return l;
    }
    //====================================================================================
    /**
     * Create a new store, add it to the end of the storeQueue and return a handle
     * to the user.
     */
    @Override
    public StoreHandle dispatchStore(int size) {
        Store s = new Store(birthCounter, size);
        birthCounter++;
        storeQueue.add(s);
        return s;
    }
    //====================================================================================
    /**
     * Execute our load, maybe out of program order. Maybe we will bypass.
     * Get all older stores with matching address, find the youngest,
     * this value will be our value.
     */
    @Override
    public long executeLoad(LoadHandle handle, long address) {
        Load l = (Load) handle;
        l.hasExecuted = true;
        l.addresses = computeAddresses(address, l.bytesRead);

        // List to hold all the values we read from each byte.
        List<Long> values = new LinkedList<>();
        /* After loop executes we will hold our number in values, one byte
           per entry in values. */
        for(Long addr : l.addresses){
            // Get yougest, oldest store that matches current addr.
            List<Store> stores = storeQueue.stream().
                filter(s -> s.hasExecuted).
                filter(s -> s.birthday < l.birthday).
                filter(s -> s.addresses.contains(addr)).
                collect(Collectors.toList());

            // If list is empty, we insert zero. Othewise, value at that byte.
            // Similarly, no birthday for this byte entry.
            if(stores.isEmpty()){
                values.add(0L);
                l.storeBirthdays.add(-1);
            }else{
                Store s = stores.get(stores.size() - 1);
                l.storeBirthdays.add(s.birthday);
                // Where is this byte at?
                values.add(s.values.get(s.addresses.indexOf(addr)));
            }
        }

        return numberFromByteList(values);
    }
    //====================================================================================
    /** Execute our store, maybe out of program order. Get all younger loads with
     * matching addresses. If their storeBirthday older than ours we squash them.
     * They recieved the wrong value.
    */
    @Override
    public Collection<? extends LoadHandle>
        executeStore(StoreHandle handle, long address, long value) {
        Store s = (Store) handle;
        // Update our store based on information.
        s.addresses = computeAddresses(address, s.bytesWritten);
        s.hasExecuted = true;
        s.values = byteListFromNumber(value);

        // We may double counted some loads below. Using a set will take care of this.
        Set<Load> squash = new HashSet<>();

        for(Long addr : s.addresses){
            // Concat new list to our squash set.
            Set<Load> thisSet = loadQueue.stream().
                filter(l -> l.hasExecuted).
                filter(l -> l.birthday > s.birthday).
                filter(l -> l.addresses.contains(addr)).
                filter(l -> l.storeBirthdays.get(l.addresses.indexOf(addr)) < s.birthday).
                collect(Collectors.toSet());
            squash.addAll(thisSet);
        }

        return squash;
    }
    //====================================================================================
    /**
     * Given a list where each entry represents the nth byte of a number. Return the
     * number. List[0] is the highest order bytes.
     */
    long numberFromByteList(List<Long> values){
        long num = 0;
        for(Long v : values)
            num = (num << 8) + v; // We have bytes shift 8 times.

        return num;
    }
    //====================================================================================
    /**
     * Given a number. Convert to an array where each entry represents a byte.
     */
    List<Long> byteListFromNumber(long num){
        List values = new LinkedList<>();

        for(long i = num; i != 0; i /= 256)
            values.add(i % 256);

        Collections.reverse(values);
        return values;
    }
    //====================================================================================
    /**
     * Given an starting address and the number of bytes that will be used, produces a
     * list containing all the addresses that will be used.
     */
    List<Long> computeAddresses(long addr, long bytes){
        List<Long> addrs = new LinkedList<>();
        for(long i = addr; i < addr + bytes; i++)
            addrs.add(i);

        return addrs;
    }
}
//========================================================================================
