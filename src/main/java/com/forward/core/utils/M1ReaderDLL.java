package com.forward.core.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface M1ReaderDLL extends Library {
    String dllPath = "ccid_m1reader";
    M1ReaderDLL INSTANCE = Native.load(dllPath, M1ReaderDLL.class);

    int M1_ReadBlockBinary(int blockIndex, String pin, Pointer buff);

    int M1_WriteBlockBinary(int blockIndex, String pin, String buff);

    int M1_ReadBlock(int blockIndex, String pin, Pointer buff);

    int M1_WriteBlock(int blockIndex, String pin, String buff);

    int M1_ATS(Pointer ats);

    int M1_UID(Pointer uid);

    void M1_Beep(int times);
}
