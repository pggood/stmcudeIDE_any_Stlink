package com.st.stm32cube.ide.mcu.debug.stlinkfwutil;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class StLinkPreferenceUtil {
   public static final String STLINK_PREFS = "com.st.stm32cube.ide.mcu.debug/debug.stlink_preferences";
   public static final String STLINK_PREFS_CHECK_LATEST_VERSION_ENABLED = "latest_version_enabled";
   public static final boolean STLINK_PREFS_CHECK_LATEST_VERSION_ENABLED_DEFAULT = true;

   public static boolean checkFirmwareCheckEnabled() {
      boolean ret = true;
      IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("com.st.stm32cube.ide.mcu.debug/debug.stlink_preferences");
      if (prefs != null) {
         ret = prefs.getBoolean("latest_version_enabled", true);
      }

      return ret;
   }

   public static void setFirmwareCheckEnabled(boolean enabled) {
      IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("com.st.stm32cube.ide.mcu.debug/debug.stlink_preferences");
      if (prefs != null) {
         prefs.putBoolean("latest_version_enabled", enabled);

         try {
            prefs.flush();
         } catch (BackingStoreException var3) {
         }
      }

   }
}
