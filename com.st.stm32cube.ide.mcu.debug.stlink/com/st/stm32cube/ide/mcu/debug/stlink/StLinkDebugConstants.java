package com.st.stm32cube.ide.mcu.debug.stlink;

import com.st.stm32cube.common.ecosystemintegration.core.CpuCoreEnum;
import com.st.stm32cube.ide.mcu.debug.launch.device.ResetStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;

public class StLinkDebugConstants {
   public static final String ATTR_HW_BREAK = "com.st.stm32cube.ide.mcu.debug.stlink.hw_break";
   public static final String ATTR_SWD_MODE = "com.st.stm32cube.ide.mcu.debug.stlink.swd_mode";
   public static final String ATTR_ENABLE_SERVER_LOGGING = "com.st.stm32cube.ide.mcu.debug.stlink.enable_logging";
   public static final String ATTR_SERVER_LOG_FILE = "com.st.stm32cube.ide.mcu.debug.stlink.log_file";
   public static final String ATTR_ENABLE_EXTERNAL_LOADER = "com.st.stm32cube.ide.mcu.debug.stlink.enable_external_loader";
   public static final String ATTR_EXTERNAL_LOADER = "com.st.stm32cube.ide.mcu.debug.stlink.external_loader";
   public static final String ATTR_EXTERNAL_LOADER_INIT = "com.st.stm32cube.ide.mcu.debug.stlink.external_loader_init";
   public static final String ATTR_ENABLE_SHARED_STLINK = "com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink";
   public static final String ATTR_CHECK_SERIAL_NUMBER = "com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number";
   public static final String ATTR_TXT_SERIAL_NUMBER = "com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number";
   public static final String ATTR_RESET_STRATEGY = "com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy";
   public static final String ATTR_HALT_ALL_ON_RESET = "com.st.stm32cube.ide.mcu.debug.stlink.halt_all_on_reset";
   public static final String ATTR_CTI_ALLOW_HALT = "com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt";
   public static final String ATTR_CTI_SIGNAL_HALT = "com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt";
   public static final String ATTR_ENABLE_MAX_HALT_DELAY = "com.st.stm32cube.ide.mcu.debug.stlink.enable_max_halt_delay";
   public static final String ATTR_MAX_HALT_DELAY = "com.st.stm32cube.ide.mcu.debug.stlink.max_halt_delay";
   public static final String ATTR_LOW_POWER_DEBUG = "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug";
   public static final String ATTR_WATCHDOG_CONFIG = "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config";
   public static final String ATTR_FREQUENCY = "com.st.stm32cube.ide.mcu.debug.stlink.frequency";
   public static final String ATTR_RESTART_CONFIGURATIONS = "com.st.stm32cube.ide.mcu.debug.stlinkrestart_configurations";
   public static final String ATTR_RTOS_ENABLED = "com.st.stm32cube.ide.mcu.debug.stlinkenable_rtos";
   public static final String RESET_STRATEGY_ATTACH = "no_reset";
   public static final String RESET_STRATEGY_SYSTEM_RESET = "system_reset";
   public static final String RESET_STRATEGY_CORE_RESET = "core_reset";
   public static final String RESET_STRATEGY_CONNECT_UNDER_RESET = "connect_under_reset";
   public static final String RESET_STRATEGY_HARDWARE_RESET = "hardware_reset";
   public static final String DEFAULT_RESET_STRATEGY = "connect_under_reset";
   public static final String LOW_POWER_DEBUG_NONE = "none";
   public static final String LOW_POWER_DEBUG_ENABLE = "enable";
   public static final String LOW_POWER_DEBUG_DISABLE = "disable";
   public static final String DEFAULT_LOW_POWER_DEBUG = "enable";
   public static final String WATCHDOG_CONFIG_NONE = "none";
   public static final String WATCHDOG_CONFIG_ENABLE = "enable";
   public static final String WATCHDOG_CONFIG_DISABLE = "disable";
   public static final String DEFAULT_WATCHDOG_CONFIG = "none";
   public static final String FREQUENCY_OPTION_AUTO = "0";
   public static final String DEFAULT_PORT = "61234";
   public static final String DEFAULT_SWV_PORT = "61235";
   public static final String DEFAULT_SWV_TRACE_HCLK = "16000000";
   public static final boolean DEFAULT_WAIT_FOR_SYNC = true;
   public static final boolean DEFAULT_CHECK_SERIAL_NUMBER = false;
   public static final String DEFAULT_TXT_SERIAL_NUMBER = "";
   public static final boolean DEFAULT_ENABLE_LIVE_EXPR = true;
   public static final boolean DEFAULT_HALT_ALL_ON_RESET = false;
   public static final boolean DEFAULT_CTI_ALLOW_HALT = false;
   public static final boolean DEFAULT_CTI_SIGNAL_HALT = false;
   public static final boolean DEFAULT_ENABLE_MAX_HALT_DELAY = false;
   public static final String DEFAULT_MAX_HALT_DELAY = "2";
   public static final boolean DEFAULT_EXTERNAL_LOADER_INIT = false;
   public static final String DEFAULT_FREQUENCY = "0";
   public static final boolean DEFAULT_RTOS_ENABLED = false;
   public static List<CpuCoreEnum> supportedCores;
   public static List<ResetStrategy> resetStrategies;
   public static final List<ComboAttributeItem> frequencyOptions;
   public static final List<ComboAttributeItem> lowPowerCfgs;
   public static final List<ComboAttributeItem> watchdogCfgs;
   private static final List<DeviceScript> deviceScripts;

   static {
      supportedCores = Arrays.asList(CpuCoreEnum.Cortex_M0, CpuCoreEnum.Cortex_M0plus, CpuCoreEnum.Cortex_M1, CpuCoreEnum.Cortex_M3, CpuCoreEnum.Cortex_M4, CpuCoreEnum.Cortex_M7, CpuCoreEnum.Cortex_M23, CpuCoreEnum.Cortex_M33, CpuCoreEnum.Cortex_M55, CpuCoreEnum.Cortex_M85, CpuCoreEnum.Cortex_R4, CpuCoreEnum.Cortex_R5, CpuCoreEnum.Cortex_R7);
      resetStrategies = Arrays.asList(new ResetStrategy("Connect under reset", "connect_under_reset", Collections.emptyList(), Arrays.asList("-k")), new ResetStrategy("Software system reset", "system_reset", Arrays.asList("monitor reset" + System.lineSeparator()), Arrays.asList("-g")), new ResetStrategy("Hardware reset", "hardware_reset", Arrays.asList("monitor reset hardware" + System.lineSeparator()), Arrays.asList("-g")), new ResetStrategy("Core reset", "core_reset", Arrays.asList("monitor reset core" + System.lineSeparator()), Arrays.asList("-g")), new ResetStrategy("None", "no_reset", Collections.emptyList(), Arrays.asList("-g")));
      frequencyOptions = Arrays.asList(new ComboAttributeItem(Messages.StLinkDebugHardware_FrequencyAuto, "0"), new ComboAttributeItem("140", "140"), new ComboAttributeItem("1000", "1000"), new ComboAttributeItem("8000", "8000"), new ComboAttributeItem("21000", "21000"), new ComboAttributeItem("24000", "24000"));
      lowPowerCfgs = Arrays.asList(new ComboAttributeItem(Messages.StLinkDebugHardware_LowPowerDebug_NoConfig, "none"), new ComboAttributeItem(Messages.StLinkDebugHardware_LowPowerDebug_Enable, "enable"), new ComboAttributeItem(Messages.StLinkDebubHardware_LowPowerDebug_Disable, "disable"));
      watchdogCfgs = Arrays.asList(new ComboAttributeItem(Messages.StLinkDebugHardware_WatchdogCtrs_NoConfig, "none"), new ComboAttributeItem(Messages.StLinkDebugHardware_WatchdogCtrs_Enable, "enable"), new ComboAttributeItem(Messages.StLinkDebugHardware_WatchdogCtrs_Disable, "disable"));
      deviceScripts = Arrays.asList(new DeviceScript("stm32c0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1073877052L, 134217728L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073877052L, 134217728L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1073877016L, 4194304L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073877016L, 4194304L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f1", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366724L, 768L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366724L, 768L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f2", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f3", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f4", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32f7", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32h5", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(1140998148L, 6L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(1140998148L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1140998152L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1140998152L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32n6", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1140854788L, 1048576L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1140854788L, 7L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1140854788L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1140854800L, 2048L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1140854812L, 262144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1140854800L, 2048L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1140854812L, 262144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1140854788L, 1048576L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32h7", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508020L, 64L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508024L, 64L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508028L, 2048L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508032L, 2048L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508052L, 786432L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508056L, 786432L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508020L, 64L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508024L, 64L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508028L, 2048L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508032L, 2048L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508052L, 786432L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508056L, 786432L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32h7_0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG, DeviceScript.Feature.CTI_CONFIG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            boolean ctiAllowHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
            boolean ctiSignalHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
            boolean ctiUsed = ctiAllowHalt || ctiSignalHalt;
            if (ctiUsed || DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508020L, 64L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508028L, 2048L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508052L, 786432L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508020L, 64L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508028L, 2048L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508052L, 786432L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (ctiUsed) {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  if (ctiAllowHalt) {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }

                  if (ctiSignalHalt) {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }
               } else {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (!ctiUsed && DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }

         public List<RegisterManip> createInitScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370832L, 1L, RegisterManip.RegisterManipOp.BF_SET));
            return regConfigs;
         }
      }, new DeviceScript("stm32h7_3", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG, DeviceScript.Feature.CTI_CONFIG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            boolean ctiAllowHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
            boolean ctiSignalHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
            boolean ctiUsed = ctiAllowHalt || ctiSignalHalt;
            if (ctiUsed || DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1543507972L, 63L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508024L, 64L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508032L, 2048L, RegisterManip.RegisterManipOp.BF_SET));
                  regConfigs.add(new RegisterManip(1543508056L, 786432L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543508024L, 64L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508032L, 2048L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  regConfigs.add(new RegisterManip(1543508056L, 786432L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (ctiUsed) {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  if (ctiAllowHalt) {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }

                  if (ctiSignalHalt) {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }
               } else {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (!ctiUsed && DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1543507972L, 6291456L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }

         public List<RegisterManip> createInitScript(ILaunchConfiguration launchConfiguration) {
            List<RegisterManip> regConfigs = new ArrayList();
            regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370832L, 1L, RegisterManip.RegisterManipOp.BF_SET));
            return regConfigs;
         }
      }, new DeviceScript("stm32g0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1073877052L, 134217728L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073877052L, 134217728L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32g4", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32l0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1073877044L, 4194304L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073877044L, 4194304L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32l1", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32l4", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32l4plus", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366728L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32l5", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32u0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(1073877072L, 1L, RegisterManip.RegisterManipOp.BF_SET));
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(1073829892L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073829896L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE != enLowPwrDbg && DeviceScript.TriStateBoolean.ENABLE != stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(1073877072L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32u3", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32u5", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32wb", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366780L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758366780L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32wba", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
               regConfigs.add(new RegisterManip(3758374916L, 6L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
            } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
               regConfigs.add(new RegisterManip(3758374920L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
            }

            return regConfigs;
         }
      }, new DeviceScript("stm32wl_0", new DeviceScript.Feature[]{DeviceScript.Feature.LOW_POWER_DEBUG, DeviceScript.Feature.DISABLE_WATCHDOG, DeviceScript.Feature.CTI_CONFIG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean enLowPwrDbg = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            boolean ctiAllowHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
            boolean ctiSignalHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
            boolean ctiUsed = ctiAllowHalt || ctiSignalHalt;
            if (ctiUsed || DeviceScript.TriStateBoolean.NO_CONF != enLowPwrDbg || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               if (DeviceScript.TriStateBoolean.ENABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == enLowPwrDbg) {
                  regConfigs.add(new RegisterManip(3758366724L, 7L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(3758366780L, 6144L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(3758366780L, 6144L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (ctiUsed) {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  if (ctiAllowHalt) {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }

                  if (ctiSignalHalt) {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }
               } else {
                  regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }

         public List<RegisterManip> createInitScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            regConfigs.add(new RegisterManip(3758370816L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370976L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370848L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(3758370832L, 1L, RegisterManip.RegisterManipOp.BF_SET));
            return regConfigs;
         }
      }, new DeviceScript("stm32wl_1", new DeviceScript.Feature[]{DeviceScript.Feature.DISABLE_WATCHDOG, DeviceScript.Feature.CTI_CONFIG}) {
         public List<RegisterManip> createDebugScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            DeviceScript.TriStateBoolean stopWatchdogCtrs = DeviceScript.TriStateBoolean.fromAttribute(launchConfiguration, "com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            boolean ctiAllowHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
            boolean ctiSignalHalt = launchConfiguration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
            boolean ctiUsed = ctiAllowHalt || ctiSignalHalt;
            if (ctiUsed || DeviceScript.TriStateBoolean.NO_CONF != stopWatchdogCtrs) {
               if (DeviceScript.TriStateBoolean.ENABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(3758366780L, 4096L, RegisterManip.RegisterManipOp.BF_SET));
               } else if (DeviceScript.TriStateBoolean.DISABLE == stopWatchdogCtrs) {
                  regConfigs.add(new RegisterManip(3758366780L, 4096L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }

               if (ctiUsed) {
                  regConfigs.add(new RegisterManip(4026535936L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  if (ctiAllowHalt) {
                     regConfigs.add(new RegisterManip(4026536096L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(4026536096L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }

                  if (ctiSignalHalt) {
                     regConfigs.add(new RegisterManip(4026535968L, 1L, RegisterManip.RegisterManipOp.BF_SET));
                  } else {
                     regConfigs.add(new RegisterManip(4026535968L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
                  }
               } else {
                  regConfigs.add(new RegisterManip(4026535936L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
               }
            }

            return regConfigs;
         }

         public List<RegisterManip> createInitScript(ILaunchConfiguration launchConfiguration) throws CoreException {
            List<RegisterManip> regConfigs = new ArrayList();
            regConfigs.add(new RegisterManip(4026535936L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(4026536096L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(4026535968L, 1L, RegisterManip.RegisterManipOp.BF_CLEAR));
            regConfigs.add(new RegisterManip(4026535952L, 1L, RegisterManip.RegisterManipOp.BF_SET));
            return regConfigs;
         }
      });
   }

   public static String getBinaryName() {
      String os = Platform.getOS();
      String ret = "";
      if (os != null) {
         if (os.equals("win32")) {
            ret = "ST-LINK_gdbserver.exe";
         } else if (os.equals("linux")) {
            ret = "ST-LINK_gdbserver";
         } else if (os.equals("macosx")) {
            ret = "ST-LINK_gdbserver";
         }
      }

      return ret;
   }

   public static List<DeviceScript> getDeviceScripts() {
      return deviceScripts;
   }
}
