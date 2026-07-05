package com.st.stm32cube.ide.mcu.debug.stlink;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
   private static final String BUNDLE_NAME = "com.st.stm32cube.ide.mcu.debug.stlink.messages";
   public static String StLinkDebugHardware_Error_SelectionRequired;
   public static String RunStLinkUpgrade_Error_LaunchingSTLinkUpgradeTool;
   public static String RunStLinkUpgrade_Error_FailedToLocateSTLinkUpgradeTool;
   public static String StLinkDebugHardware_Browse;
   public static String StLinkDebugHardware_Enable;
   public static String StLinkDebugHardware_Error_DLLError;
   public static String StLinkDebugHardware_Error_FailedResetTarget;
   public static String StLinkDebugHardware_Error_FailedToConnect;
   public static String StLinkDebugHardware_Error_FirmwareUpgradeRequired;
   public static String StLinkDebugHardware_Error_FirmwareUpgradeRequiredQuestion;
   public static String StLinkDebugHardware_Error_InitializingDevice;
   public static String StLinkDebugHardware_Error_NoDeviceFound;
   public static String StLinkDebugHardware_Error_TargetNotHalted;
   public static String StLinkDebugHardware_Error_TargetUnderReset;
   public static String StLinkDebugHardware_Error_Unknown;
   public static String StLinkDebugHardware_Error_UnknownMCU;
   public static String StLinkDebugHardware_Error_UnknownVendor;
   public static String StLinkDebugHardware_Error_USBCommsErrorReconnect;
   public static String StLinkDebugHardware_Interface;
   public static String StLinkDebugHardware_LogToFile;
   public static String StLinkDebugHardware_Misc;
   public static String StLinkDebugHardware_PortNumber;
   public static String StLinkDebugHardware_SWV;
   public static String StLinkDebugHardware_ExternalLoader;
   public static String StLinkDebugHardware_ExternalLoader_Select;
   public static String StLinkDebugHardware_EnableSharedSTLink;
   public static String StLinkDebugHardware_VerifyFlashDownload;
   public static String StLinkDebugHardware_EnableLiveExpressions;
   public static String StLinkDebugHardware_btnCheckBoxSerialNumber_text;
   public static String StLinkDebugHardware_Error_SerialNotFound;
   public static String StLinkDebugHardware_btnScanButton_text;
   public static String StLinkDebugHardware_EnableMaxHaltDelay;
   public static String StLinkDebugHardware_ExtLoadInit;
   public static String StLinkDebugHardware_LowPowerDebug;
   public static String StLinkDebugHardware_LowPowerDebug_NoConfig;
   public static String StLinkDebugHardware_LowPowerDebug_Enable;
   public static String StLinkDebubHardware_LowPowerDebug_Disable;
   public static String StLinkDebugHardware_WatchdogCtrs;
   public static String StLinkDebugHardware_WatchdogCtrs_NoConfig;
   public static String StLinkDebugHardware_WatchdogCtrs_Enable;
   public static String StLinkDebugHardware_WatchdogCtrs_Disable;
   public static String StLinkDebugHardware_McuSettings;
   public static String StLinkDebugHardware_FrequencyAuto;

   static {
      NLS.initializeMessages("com.st.stm32cube.ide.mcu.debug.stlink.messages", Messages.class);
   }

   private Messages() {
   }
}
