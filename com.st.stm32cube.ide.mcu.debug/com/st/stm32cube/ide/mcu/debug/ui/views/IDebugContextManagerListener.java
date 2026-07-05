package com.st.stm32cube.ide.mcu.debug.ui.views;

public interface IDebugContextManagerListener {
   void handleSession(String sessionIdentifier);

   void handleSuspend();

   void handleResume();

   void handleExited();
}
