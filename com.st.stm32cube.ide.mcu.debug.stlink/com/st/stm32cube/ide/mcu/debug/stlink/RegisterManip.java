package com.st.stm32cube.ide.mcu.debug.stlink;

public class RegisterManip {
   private long address;
   private long value;
   private RegisterManip.RegisterManipOp op;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$RegisterManip$RegisterManipOp;

   public RegisterManip(long address, long mask, RegisterManip.RegisterManipOp op) {
      this.address = address;
      this.value = mask;
      this.op = op;
   }

   public long getAddress() {
      return this.address;
   }

   public long getValue() {
      return this.value;
   }

   public RegisterManip.RegisterManipOp getOp() {
      return this.op;
   }

   public static String getGdbCommand(RegisterManip regManip) {
      String command = "";
      switch($SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$RegisterManip$RegisterManipOp()[regManip.getOp().ordinal()]) {
      case 1:
         command = "set *(unsigned int *)0x" + Long.toHexString(regManip.getAddress()) + "=0x" + Long.toHexString(regManip.getValue());
         break;
      case 2:
         command = "set *(unsigned int *)0x" + Long.toHexString(regManip.getAddress()) + "|=0x" + Long.toHexString(regManip.getValue());
         break;
      case 3:
         command = "set *(unsigned int *)0x" + Long.toHexString(regManip.getAddress()) + "&=~0x" + Long.toHexString(regManip.getValue());
      }

      return command;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$RegisterManip$RegisterManipOp() {
      int[] var10000 = $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$RegisterManip$RegisterManipOp;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[RegisterManip.RegisterManipOp.values().length];

         try {
            var0[RegisterManip.RegisterManipOp.BF_CLEAR.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[RegisterManip.RegisterManipOp.BF_SET.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[RegisterManip.RegisterManipOp.WRITE.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$RegisterManip$RegisterManipOp = var0;
         return var0;
      }
   }

   public static enum RegisterManipOp {
      WRITE,
      BF_SET,
      BF_CLEAR;
   }
}
