package cis501.submission;

import cis501.IOOORegisterRenamer;
import cis501.Insn;
import cis501.CondCodes;
import cis501.PhysReg;

import java.util.Map;

import java.util.*;
import java.lang.*;

public class OOORegisterRenamer implements IOOORegisterRenamer {
    ArrayDeque<PhysReg> freeList;
    HashMap<Integer, PhysReg> mapTable;
    int NUM_PHYS_REGS;

    public OOORegisterRenamer(int pregs) {
        freeList = new ArrayDeque<PhysReg>();
        mapTable = new HashMap<Integer, PhysReg>(this.NUM_ARCH_REGS);
        NUM_PHYS_REGS = pregs;
        for(int i = 0; i < NUM_PHYS_REGS; i++){
            PhysReg phys_reg = new PhysReg(i);
            if (i < this.NUM_ARCH_REGS){
                mapTable.put(i, phys_reg);
            }
            else {
                freeList.add(phys_reg);
            }
        }
    }

    @Override
    public int availablePhysRegs() {
        return freeList.size();
    }

    @Override
    public PhysReg allocateReg(int ar) {
        if(availablePhysRegs() > 0){
            PhysReg allocated_reg = freeList.remove();
            mapTable.put(ar, allocated_reg);
            return allocated_reg;
        }
        else {
            return null;
        }
    }

    @Override
    public void freeReg(PhysReg pr) {
        freeList.add(pr);
    }

    @Override
    public PhysReg a2p(int ar) {
        return mapTable.get(ar);
    }

    @Override
    public void rename(Insn i, Map<Short, PhysReg> inputs, Map<Short, PhysReg> outputs) {
        if(i.srcReg1 != -1){
            inputs.put(i.srcReg1, this.a2p(i.srcReg1));
        }
        if(i.srcReg2 != -1){
            inputs.put(i.srcReg2, this.a2p(i.srcReg2));
        }
        if(i.dstReg != -1){
            this.freeReg(this.a2p(i.dstReg));
            outputs.put(i.dstReg, this.allocateReg(i.dstReg));
        }
        if(i.condCode != null){
            switch(i.condCode){
                case ReadCC:
                    inputs.put(this.COND_CODE_ARCH_REG, this.a2p(this.COND_CODE_ARCH_REG));
                    break;
                case WriteCC:
                    this.freeReg(this.a2p(this.COND_CODE_ARCH_REG));
                    outputs.put(this.COND_CODE_ARCH_REG, this.allocateReg(this.COND_CODE_ARCH_REG));
                    break;
                case ReadWriteCC:
                    inputs.put(this.COND_CODE_ARCH_REG, this.a2p(this.COND_CODE_ARCH_REG));
                    this.freeReg(this.a2p(this.COND_CODE_ARCH_REG));
                    outputs.put(this.COND_CODE_ARCH_REG, this.allocateReg(this.COND_CODE_ARCH_REG));
                    break;
            }
        }
    }

}
