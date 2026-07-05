package com.st.stm32cube.ide.mcu.debug.stlinkfwutil;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.common.utils.ThreadHelper;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ManagerSTLinkClient;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.STLinkTcpClient;
import com.st.stm32cube.ide.mcu.externaltools.MCUExternalToolsPlugin;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

public class StLinkFwUtil {
   private static String TOOL_NAME = "ST-LINK firmware verification";
   private static String CONFIRM_UPGRADE_REQUIRED_MSG = "In order to use the attached ST-LINK with this version of STM32CubeIDE an update of the ST-LINK firmware is required. Proceed with update?";
   private static String CONFIRM_UPGRADE_AVAILABLE_MSG = "This version of STM32CubeIDE provides a newer firmware version of the attached ST-LINK. Proceed with update?";
   private static String SERIAL_REQUIRED_MSG = "Multiple ST-LINKs detected! Please either specify which ST-LINK serial number in the debug configuration or connect only one ST-LINK.";
   private static String NO_SERIAL_FOUND_MSG = "No ST-LINK detected! Please connect ST-LINK and restart the debug session.";
   private static String SERIAL_NOT_FOUND_MSG = "No ST-LINK with the specified serial number detected! Please connect ST-LINK and restart the debug session.";
   private static String CHECK_LATEST_TOGGLE_MSG = "Always check for latest version.";
   private static String UPGRADE_FAILURE_MSG = "Failed to start ST-LINK Upgrade";
   private static String NO_STLINK_SERVER_FOUND_MSG = "ST-Link Server is required to launch the debug session.\n\nPlease download it on www.st.com";
   private static String UNSUPPORTED_STLINK_MAJOR_MSG = "Unsupported ST-LINK.\nMinimum requirement: ST-LINK V2.";
   private static Properties fLatestVersionsProperties = null;

   // ============================================================
   // MODIFIED: Main validate() method - bypasses vendor checks
   // ============================================================
   public static boolean validate(String serial) {
      // MODIFICATION: Always return true for third-party dongle support
      // This bypasses all ST-LINK vendor and firmware verification
      
      // Log that validation is bypassed
      MCULoggerPlugin.logWarningMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
          "ST-LINK vendor validation BYPASSED - Third-party dongle support enabled");
      
      // Optionally check if there's a connected device (any device)
      // but don't enforce vendor checks
      try {
         StLinkFwUtil.StLinkInfo stLink = lookupStLink(serial);
         if (stLink == null) {
            // No device found at all - still return true to allow debugging
            // but show a warning
            ThreadHelper.runInUiThreadSync(() -> {
               MessageDialog.openWarning(
                   Display.getDefault().getActiveShell(), 
                   TOOL_NAME, 
                   "No ST-LINK detected, but continuing anyway.\n" +
                   "Please ensure your debug probe is properly connected."
               );
            });
            return true; // Still return true to allow debugging attempts
         }
         
         // If a device was found, log its info (but don't validate)
         MCULoggerPlugin.logInfoMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
             "ST-LINK detected: " + stLink.getSerial());
         
         // Always return true - bypass all checks
         return true;
         
      } catch (Exception e) {
         // If any error occurs, still return true to allow debugging
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug.stlink", e, 
             "Error in ST-LINK validation (bypassed)");
         return true;
      }
   }

   // ============================================================
   // ORIGINAL validate() method - commented out for reference
   // ============================================================
   /*
   public static boolean validate(String serial) {
      boolean validFw = true;
      StLinkFwUtil.StLinkInfo stLink = null;
      stLink = lookupStLink(serial);
      if (stLink != null) {
         if (checkStLinkSupported(stLink)) {
            boolean[] dialogReply;
            if (checkFwUpdateRequired(stLink)) {
               dialogReply = new boolean[]{true};
               ThreadHelper.runInUiThreadSync(() -> {
                  dialogReply[0] = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), TOOL_NAME, CONFIRM_UPGRADE_REQUIRED_MSG);
               });
               if (dialogReply[0]) {
                  validFw = false;
                  if (!launchUpgradeUI(false)) {
                     ThreadHelper.runInUiThreadSync(() -> {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), TOOL_NAME, UPGRADE_FAILURE_MSG);
                     });
                  }
               } else {
                  validFw = false;
               }
            } else if (StLinkPreferenceUtil.checkFirmwareCheckEnabled() && checkFwUpdateAvailable(stLink)) {
               dialogReply = new boolean[1];
               ThreadHelper.runInUiThreadSync(() -> {
                  MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(Display.getDefault().getActiveShell(), TOOL_NAME, CONFIRM_UPGRADE_AVAILABLE_MSG, CHECK_LATEST_TOGGLE_MSG, true, (IPreferenceStore)null, (String)null);
                  StLinkPreferenceUtil.setFirmwareCheckEnabled(dialog.getToggleState());
                  if (2 == dialog.getReturnCode()) {
                     dialogReply[0] = true;
                  }

               });
               if (dialogReply[0]) {
                  if (launchUpgradeUI(false)) {
                     validFw = false;
                  } else {
                     ThreadHelper.runInUiThreadSync(() -> {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), TOOL_NAME, UPGRADE_FAILURE_MSG);
                     });
                  }
               }
            }
         } else {
            validFw = false;
         }
      } else {
         validFw = false;
      }

      return validFw;
   }
   */

   // ============================================================
   // MODIFIED: lookupStLink() - more tolerant of third-party devices
   // ============================================================
   private static StLinkFwUtil.StLinkInfo lookupStLink(String serial) {
      StLinkFwUtil.StLinkInfo ret = null;
      
      // MODIFIED: Don't require ST-Link Server for third-party dongles
      // Some third-party dongles might work without it
      if (!ManagerSTLinkClient.isSTLServerInstalled()) {
         MCULoggerPlugin.logWarningMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
             "ST-Link Server not found - trying to continue anyway for third-party dongle support");
         
         // For third-party dongles, we might want to create a dummy StLinkInfo
         // to satisfy the rest of the code
         if (serial != null && !serial.isEmpty()) {
            // Create a minimal StLinkInfo with the provided serial
            StLinkFwUtil.StLinkFwInfo dummyFwInfo = new StLinkFwUtil.StLinkFwInfo(2, 30);
            ret = new StLinkFwUtil.StLinkInfo(serial, dummyFwInfo, (short)0, (short)0);
            MCULoggerPlugin.logInfoMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
                "Using dummy ST-LINK info for serial: " + serial);
         } else {
            // Create a generic dummy StLinkInfo
            StLinkFwUtil.StLinkFwInfo dummyFwInfo = new StLinkFwUtil.StLinkFwInfo(2, 30);
            ret = new StLinkFwUtil.StLinkInfo("THIRD_PARTY_DONGLE", dummyFwInfo, (short)0, (short)0);
            MCULoggerPlugin.logInfoMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
                "Using generic dummy ST-LINK info for third-party dongle");
         }
         return ret;
      }
      
      // Original code - try to detect real ST-LINK
      try {
         STLinkTcpClient client = ManagerSTLinkClient.openClient();
         client.refresh();
         List<StLinkFwUtil.StLinkInfo> connected = client.get_connected();
         ManagerSTLinkClient.closeClient(client);
         
         if (connected.size() > 0) {
            if (serial != null && !serial.isEmpty()) {
               Iterator var5 = connected.iterator();

               while(var5.hasNext()) {
                  StLinkFwUtil.StLinkInfo item = (StLinkFwUtil.StLinkInfo)var5.next();
                  if (serial.equals(item.getSerial())) {
                     ret = item;
                     break;
                  }
               }

               if (ret == null) {
                  // MODIFIED: Show warning instead of error for third-party
                  ThreadHelper.runInUiThreadSync(() -> {
                     MessageDialog.openWarning(
                         Display.getDefault().getActiveShell(), 
                         TOOL_NAME, 
                         SERIAL_NOT_FOUND_MSG + " S/N: " + serial + 
                         "\n\nUsing third-party dongle mode."
                     );
                  });
                  
                  // Create dummy info for third-party
                  StLinkFwUtil.StLinkFwInfo dummyFwInfo = new StLinkFwUtil.StLinkFwInfo(2, 30);
                  ret = new StLinkFwUtil.StLinkInfo(serial, dummyFwInfo, (short)0, (short)0);
               }
            } else if (connected.size() == 1) {
               ret = (StLinkFwUtil.StLinkInfo)connected.get(0);
            } else {
               // MODIFIED: Multiple ST-LINKs detected - use first one instead of error
               ThreadHelper.runInUiThreadSync(() -> {
                  MessageDialog.openWarning(
                      Display.getDefault().getActiveShell(), 
                      TOOL_NAME, 
                      SERIAL_REQUIRED_MSG + "\n\nUsing the first detected ST-LINK."
                  );
               });
               ret = (StLinkFwUtil.StLinkInfo)connected.get(0);
            }
         } else {
            // MODIFIED: No device found - create dummy for third-party
            ThreadHelper.runInUiThreadSync(() -> {
               MessageDialog.openWarning(
                   Display.getDefault().getActiveShell(), 
                   TOOL_NAME, 
                   "No ST-LINK detected, but continuing in third-party dongle mode."
               );
            });
            
            StLinkFwUtil.StLinkFwInfo dummyFwInfo = new StLinkFwUtil.StLinkFwInfo(2, 30);
            ret = new StLinkFwUtil.StLinkInfo("THIRD_PARTY_DONGLE", dummyFwInfo, (short)0, (short)0);
         }
      } catch (Exception e) {
         // MODIFIED: On any error, create dummy info
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug.stlink", e, 
             "Error looking up ST-LINK - using third-party dongle mode");
         
         StLinkFwUtil.StLinkFwInfo dummyFwInfo = new StLinkFwUtil.StLinkFwInfo(2, 30);
         ret = new StLinkFwUtil.StLinkInfo("THIRD_PARTY_DONGLE", dummyFwInfo, (short)0, (short)0);
      }

      return ret;
   }

   // ============================================================
   // MODIFIED: checkStLinkSupported() - always returns true
   // ============================================================
   private static boolean checkStLinkSupported(StLinkFwUtil.StLinkInfo stLink) {
      // MODIFIED: Always return true for third-party support
      // This bypasses the V1/V2 hardware check
      
      // Log a warning but continue
      MCULoggerPlugin.logWarningMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
          "ST-LINK hardware check bypassed - third-party dongle support enabled");
      
      // Return true regardless of hardware version
      return true;
      
      /* ORIGINAL CODE:
      boolean ret = true;
      StLinkFwUtil.StLinkFwInfo fwInfo = stLink.getFirmwareInfo();
      if (stLink.isStLinkV1() || fwInfo != null && fwInfo.major != -1 && fwInfo.major < 2) {
         ThreadHelper.runInUiThreadSync(() -> {
            MessageDialog.openError(Display.getDefault().getActiveShell(), TOOL_NAME, UNSUPPORTED_STLINK_MAJOR_MSG);
         });
         ret = false;
      }
      return ret;
      */
   }

   // ============================================================
   // MODIFIED: checkFwUpdateRequired() - always returns false
   // ============================================================
   private static boolean checkFwUpdateRequired(StLinkFwUtil.StLinkInfo stLink) {
      // MODIFIED: Always return false - don't force firmware updates
      MCULoggerPlugin.logInfoMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
          "Firmware update check bypassed for third-party dongle");
      return false;
   }

   // ============================================================
   // MODIFIED: checkFwUpdateAvailable() - always returns false
   // ============================================================
   private static boolean checkFwUpdateAvailable(StLinkFwUtil.StLinkInfo stLink) {
      // MODIFIED: Always return false - don't suggest firmware updates
      MCULoggerPlugin.logInfoMessage("com.st.stm32cube.ide.mcu.debug.stlink", 
          "Firmware update availability check bypassed for third-party dongle");
      return false;
   }

   // ============================================================
   // ORIGINAL: These methods remain unchanged
   // ============================================================
   private static Process execUpgradeTool(List<String> params, boolean suppressOutput) {
      Process ret = null;

      try {
         String toolLoc = MCUExternalToolsPlugin.getGdbServerLocation("com.st.stm32cube.ide.mcu.externaltools.stlinkgdbserver.EXECUTABLE");
         if (toolLoc != null) {
            String binary = "STLinkUpgrade.jar";
            List<String> command = new ArrayList();
            command.add(System.getProperty("java.home") + "/bin/java");
            command.add("-jar");
            command.add(binary);
            command.addAll(params);
            ProcessBuilder procBuilder = new ProcessBuilder((String[])command.toArray(new String[command.size()]));
            if (suppressOutput) {
               procBuilder.inheritIO();
            }

            procBuilder.directory(new File(toolLoc));
            ret = procBuilder.start();
         } else {
            MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", STLinkUpgrade not installed");
         }
      } catch (Exception var7) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var7, TOOL_NAME);
      }

      return ret;
   }

   private static boolean launchUpgradeUI(boolean waitClose) {
      boolean ret = true;
      List<String> params = new ArrayList();
      Process proc = execUpgradeTool(params, true);
      if (proc != null) {
         if (waitClose) {
            try {
               proc.waitFor();
            } catch (InterruptedException var5) {
               MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", failed wait on STLinkUpgrade");
               ret = false;
            }
         }
      } else {
         MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", failed to start STLinkUpgrade");
         ret = false;
      }

      return ret;
   }

   private static Properties getLatestFwInfo() {
       if (fLatestVersionsProperties == null) {
           fLatestVersionsProperties = new Properties();
           List<String> params = new ArrayList();
           params.add("-displayLastJtagVer");
           Process proc = execUpgradeTool(params, false);
           if (proc != null) {
               try {
                   InputStream is = proc.getInputStream();
                   try {
                       fLatestVersionsProperties.load(is);
                       proc.waitFor();
                   } catch (Exception e) {
                       MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", failed reading properties: " + e.getMessage());
                   } finally {
                       if (is != null) {
                           try {
                               is.close();
                           } catch (Exception e) {
                               // Ignore
                           }
                       }
                   }
               } catch (Exception e) {
                   MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", failed reading properties");
               }
           } else {
               MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.debug", TOOL_NAME + ", failed to start STLinkUpgrade");
           }
   
           if (fLatestVersionsProperties.size() < 3) {
               fLatestVersionsProperties.put(getPropertyKey(1), "13");
               fLatestVersionsProperties.put(getPropertyKey(2), "37");
               fLatestVersionsProperties.put(getPropertyKey(3), "7");
           }
       }
   
       return fLatestVersionsProperties;
   }

   private static Map<Integer, StLinkFwUtil.StLinkFwInfo> getRequiredFwInfo() {
      Map<Integer, StLinkFwUtil.StLinkFwInfo> ret = new HashMap();
      ret.put(2, new StLinkFwUtil.StLinkFwInfo(2, 28));
      return ret;
   }

   private static String getPropertyKey(int ver) {
      return String.format("FIRMWARE_JTAG_STLINK_V%d_LAST_VERSION", ver);
   }

   // ============================================================
   // Inner classes remain unchanged
   // ============================================================
   public static class StLinkFwInfo {
      public static final int FIRMWARE_MAJOR_VER_STLINKV1 = 1;
      public static final int FIRMWARE_MAJOR_VER_STLINKV2 = 2;
      public static final int FIRMWARE_MAJOR_VER_STLINKV3 = 3;
      public static final int FIRMWARE_MAJOR_VER_REQUIRED = 2;
      public static final int FIRMWARE_JTAG_VER_REQUIRED_STLINKV2 = 28;
      public int major;
      public int jtag;

      public StLinkFwInfo(int major, int jtag) {
         this.major = major;
         this.jtag = jtag;
      }
   }

   public static class StLinkInfo {
      private String serial;
      private StLinkFwUtil.StLinkFwInfo fwInfo;
      private short vendorId;
      private short productId;

      public StLinkInfo(String serial, StLinkFwUtil.StLinkFwInfo fwInfo, short vendor_id, short product_id) {
         this.serial = serial;
         this.fwInfo = fwInfo;
         this.vendorId = vendor_id;
         this.productId = product_id;
      }

      public String getSerial() {
         return this.serial;
      }

      public StLinkFwUtil.StLinkFwInfo getFirmwareInfo() {
         return this.fwInfo;
      }

      public boolean isStLinkV1() {
         return this.vendorId == 1155 && this.productId == 14148;
      }
   }
}