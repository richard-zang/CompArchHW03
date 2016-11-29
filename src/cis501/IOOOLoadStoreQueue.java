package cis501;


import java.util.Collection;

public interface IOOOLoadStoreQueue {

    /** @return whether the LSQ has room for one more store or not */
    boolean roomForLoad();

    /**
     * @return whether the LSQ has room for one more store or not
     */
    boolean roomForStore();

    /**
     * Commit the oldest completed memory instruction from the LSQ. If the LSQ is empty or the
     * oldest insn has not completed, this does nothing.
     */
    void commitOldest();

    /**
     * Dispatch a load instruction
     *
     * @param size the number of bytes the load reads. One of 1, 2, 4 or 8 bytes.
     */
    LoadHandle dispatchLoad(int size);

    /**
     * Dispatch a store instruction
     *
     * @param size the number of bytes the store writes. One of 1, 2, 4 or 8 bytes.
     */
    StoreHandle dispatchStore(int size);

    /**
     * Execute a load instruction
     *
     * @param handle  identifier for the load that is executing
     * @param address The data address of the load, always an aligned address.
     * @return the value returned by the load. For multi-byte loads, this value will be in
     * big-endian byte order. E.g., if memory addresses 0-3 contain values 0x11, 0x22, 0x33,
     * 0x44, respectively, then a 4-byte load from address 0x0 will return 0x11223344
     */
    long executeLoad(LoadHandle handle, long address);

    /**
     * Execute a store instruction
     *
     * @param handle  identifier for the store that is executing
     * @param address The data address of the store, always an aligned address.
     * @param value   The value being written. For multi-byte stores, this value should be in
     *                big-endian byte order. E.g., if you want to write to memory addresses 0-3
     *                with values 0x11, 0x22, 0x33, 0x44 respectively, then this parameter should be
     *                0x11223344
     * @return the loads that are now known to be mis-speculated based on this store instruction
     */
    Collection<? extends LoadHandle> executeStore(StoreHandle handle, long address, long value);

}
