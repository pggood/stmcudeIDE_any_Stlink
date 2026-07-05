package com.st.stm32cube.ide.mcu.debug.stlink;

import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.productdb.core.ITargetObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class DeviceScript {
   private String fKey;
   private Set<DeviceScript.Feature> fFeatures;

   public DeviceScript(String key, DeviceScript.Feature... features) {
      this.fKey = key;
      this.fFeatures = new HashSet();
      this.fFeatures.addAll(Arrays.asList(features));
   }

   public String getKey() {
      return this.fKey;
   }

   public boolean isSupported(DeviceScript.Feature feature) {
      return this.fFeatures.contains(feature);
   }

   public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
      return Collections.emptyList();
   }

   public List<RegisterManip> createInitScript(ILaunchConfiguration launchConfiguration) throws CoreException {
      return Collections.emptyList();
   }

   public List<RegisterManip> createShutdownScript(ILaunchConfiguration launchConfiguration) throws CoreException {
      return Collections.emptyList();
   }

   public static DeviceScript find(String key, List<DeviceScript> deviceScripts) {
      Iterator var3 = deviceScripts.iterator();

      while(var3.hasNext()) {
         DeviceScript script = (DeviceScript)var3.next();
         if (key.equalsIgnoreCase(script.getKey())) {
            return script;
         }
      }

      return null;
   }

   public static DeviceScript find(ILaunchConfiguration configuration, List<DeviceScript> configs) {
      DeviceScript devConf = null;
      ITargetObject target = MCUDebugPlugin.getTargetFromConfig(configuration);
      if (target != null) {
         try {
            String key = target.getMcu().getName();
            String multiCorePref = "";
            if (target.isMultiCpu()) {
               String apId = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", "0");
               multiCorePref = multiCorePref + "_" + apId;
            }

            devConf = find(key + multiCorePref, configs);
            if (devConf == null) {
               key = target.getMcu().getSerie().getName();
               devConf = find(key + multiCorePref, configs);
            }
         } catch (Exception var7) {
         }
      }

      return devConf;
   }

   public static enum Feature {
      LOW_POWER_DEBUG,
      DISABLE_WATCHDOG,
      CTI_CONFIG;
   }

   protected static enum TriStateBoolean {
      NO_CONF,
      ENABLE,
      DISABLE;

      public static DeviceScript.TriStateBoolean fromAttribute(ILaunchConfiguration config, String attributeName, String defaultAttributeValue) throws CoreException {
         String value = config.getAttribute(attributeName, defaultAttributeValue);
         switch(value.hashCode()) {
         case -1298848381:
            if (value.equals("enable")) {
               return ENABLE;
            }
            break;
         case 3387192:
            if (value.equals("none")) {
               return NO_CONF;
            }
            break;
         case 3569038:
            if (value.equals("true")) {
               return ENABLE;
            }
            break;
         case 97196323:
            if (value.equals("false")) {
               return DISABLE;
            }
            break;
         case 1671308008:
            if (value.equals("disable")) {
               return DISABLE;
            }
         }

         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.debug.stlink", "Could not configure " + attributeName + ", unknown value " + value));
      }
   }
}
