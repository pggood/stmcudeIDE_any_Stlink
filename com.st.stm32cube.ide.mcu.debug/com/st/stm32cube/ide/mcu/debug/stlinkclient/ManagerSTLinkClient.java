package com.st.stm32cube.ide.mcu.debug.stlinkclient;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;

public class ManagerSTLinkClient {
   public static STLinkTcpClient openClient() {
      MCULoggerPlugin.debugLog(MCUDebugPlugin.getUniqueIdentifier(), MCUDebugPlugin.isDebuggingStatic(), "com.st.stm32cube.ide.mcu.debug/debug/stlink_client", "Create client");
      STLinkTcpClient client = new STLinkTcpClient();
      return client;
   }

   public static boolean isSTLServerInstalled() {
      return STLinkTcpClient.getSTLinkServerLocation() != null;
   }

   public static void closeClient(STLinkTcpClient client) {
      if (client != null) {
         client.closeClient();
         MCULoggerPlugin.debugLog(MCUDebugPlugin.getUniqueIdentifier(), MCUDebugPlugin.isDebuggingStatic(), "com.st.stm32cube.ide.mcu.debug/debug/stlink_client", "Close client");
      }

   }
}
