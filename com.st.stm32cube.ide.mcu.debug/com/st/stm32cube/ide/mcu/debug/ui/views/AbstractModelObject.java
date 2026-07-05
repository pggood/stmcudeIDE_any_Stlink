package com.st.stm32cube.ide.mcu.debug.ui.views;

public abstract class AbstractModelObject {
   public static final String _0X0 = "0x0";
   public static final String NULL = "null";
   public static final String STAR = "*";
   public static final String SPACE = " ";
   public static final String EMPTY_PARANTHESES = "()";
   public static final String ZERO_STRING = "0";
   public static final String EMPTY_STRING = "";
   public static final String LESS_THAN = "<";
   public static final String GREATER_THAN = ">";
   private boolean fDirty = false;
   private boolean fInitialized = false;

   public boolean isDirty() {
      return this.fDirty;
   }

   public void setDirty(boolean dirty) {
      this.fDirty = dirty;
   }

   public void setInitialized(boolean initialized) {
      this.fInitialized = initialized;
   }

   public boolean isInitialized() {
      return this.fInitialized;
   }
}
