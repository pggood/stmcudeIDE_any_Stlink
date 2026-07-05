package com.st.stm32cube.ide.mcu.debug.stlink;

import com.st.stm32cube.common.ecosystemintegration.core.CpuCoreEnum;
import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.common.utils.files.FileUtils;
import com.st.stm32cube.ide.common.utils.ThreadHelper;
import com.st.stm32cube.ide.mcu.analytics.core.MCUAnalytics;
import com.st.stm32cube.ide.mcu.analytics.core.MCUAnalyticsUtils;
import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.debug.cubeprogfwutil.CubeProgFwUtil;
import com.st.stm32cube.ide.mcu.debug.debugauth.DebugAuthPermission;
import com.st.stm32cube.ide.mcu.debug.launch.DebugFeatureEnum;
import com.st.stm32cube.ide.mcu.debug.launch.GDBServerCommandGroup;
import com.st.stm32cube.ide.mcu.debug.launch.RestartListInfo;
import com.st.stm32cube.ide.mcu.debug.launch.device.AbstractDebugHardware;
import com.st.stm32cube.ide.mcu.debug.launch.device.ClockSettingsWidget;
import com.st.stm32cube.ide.mcu.debug.launch.device.ResetStrategy;
import com.st.stm32cube.ide.mcu.debug.launch.device.RestartStrategy;
import com.st.stm32cube.ide.mcu.debug.launch.device.ResetStrategy.ResetStrategySearchKey;
import com.st.stm32cube.ide.mcu.debug.launch.export.HardwareDebugUtil;
import com.st.stm32cube.ide.mcu.debug.launch.ui.ClipBoardDialog;
import com.st.stm32cube.ide.mcu.debug.launch.ui.IDebuggerTab;
import com.st.stm32cube.ide.mcu.debug.launch.ui.debugauth.DebugAuthLaunchWidget;
import com.st.stm32cube.ide.mcu.debug.launch.ui.debugauth.DebugAuthPwdWidget;
import com.st.stm32cube.ide.mcu.debug.launch.ui.externalloaders.CubeProgExtLoader;
import com.st.stm32cube.ide.mcu.debug.launch.ui.externalloaders.ExtLoadersLaunchWidget;
import com.st.stm32cube.ide.mcu.debug.launch.utils.DebugSettingService;
import com.st.stm32cube.ide.mcu.debug.stlinkfwutil.StLinkFwUtil;
import com.st.stm32cube.ide.mcu.debug.swv.core.SwvInfo;
import com.st.stm32cube.ide.mcu.debug.swv.core.SwvInfo.SwvConfig;
import com.st.stm32cube.ide.mcu.externaltools.MCUExternalToolsPlugin;
import com.st.stm32cube.ide.mcu.ide.core.TargetHelper;
import com.st.stm32cube.ide.mcu.productdb.core.Core;
import com.st.stm32cube.ide.mcu.productdb.core.Cpu;
import com.st.stm32cube.ide.mcu.productdb.core.DebugInterfaceEnum;
import com.st.stm32cube.ide.mcu.productdb.core.ITargetObject;
import com.st.stm32cube.ide.mcu.productdb.core.Mcu;
import com.st.stm32cube.ide.mcu.rtosproxy.ui.RtosProxyLaunchWidget;
import com.st.stm32cube.ide.mcu.toolchain.ToolChainHelper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StLinkDebugHardware extends AbstractDebugHardware {
   private static final int SERVER_STARTUP_TIME_MS = 2000;
   private static final double SWV_MAX_DEVIATION = 0.03D;
   private static final String[] AP_ID_DEFAULT_LIST = new String[]{"0"};
   public static List<ResetStrategy> resetForRestartStrategies;
   public static ArrayList<RestartStrategy> restartStrategies;
   private Composite settingsComp;
   private Button jtagModeBtn;
   private Button swdModeBtn;
   private Button btnVerifyFlashDownload;
   private Button btnEnableLiveExpr;
   private Button btnEnableLogging;
   private Button btnLogFileBrowse;
   private Text txtLogFile;
   private Button btnEnableSharedSTLink;
   private Button btnEnableSwv;
   private Label lblSwvPortNumber;
   private Text txtSwvPort;
   private ClockSettingsWidget clockSettings;
   private Button btnCheckBoxSerialNumber;
   private Button btnScanStLinks;
   private Combo txtSerialNumber;
   private boolean fSwvSupported = false;
   private Combo comboResetStrategy;
   private Combo comboApId;
   private Combo comboFrequency;
   private Button btnHaltAllCores;
   private Button btnCtiAllowHalt;
   private Button btnCtiSignalHalt;
   private Button btnEnableMaxHaltTimeout;
   private Text txtMaxHaltTimeout;
   private Label lblLowPowerDbgCfg;
   private Combo lowPowerDbgCfg;
   private Label lblWatchdogCfg;
   private Combo watchdogCfg;
   private Group ctiGroup;
   private Button btnRtosEnabled;
   private RtosProxyLaunchWidget rtosProxySettings;
   private String gdbCmdLine;
   private ExtLoadersLaunchWidget fExternalLoadersSettings;
   private DebugAuthLaunchWidget fDebugAuth;
   private DebugAuthPwdWidget fDebugAuthPwd;
   private Group debugAuthGroup;
   private Group debugAuthPwdGroup;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$com$st$stm32cube$common$ecosystemintegration$core$CpuCoreEnum;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$StLinkDebugHardware$ExitCode;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$launch$DebugFeatureEnum;

   static {
      resetForRestartStrategies = Arrays.asList(ResetStrategy.find("system_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE), ResetStrategy.find("hardware_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE), ResetStrategy.find("core_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE), ResetStrategy.find("no_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE));
      restartStrategies = new ArrayList(Arrays.asList(new RestartStrategy("Reset", false, ((ResetStrategy)resetForRestartStrategies.get(0)).getDisplayName(), resetForRestartStrategies)));
   }

   public Composite getControl() {
      return this.settingsComp;
   }

   public void doRemote(String connection, Collection<String> commands) {
      String cmd = "target remote " + connection;
      this.addCmd(commands, cmd);
   }

   public void doSystemResetAndHalt(Collection<String> commands) {
      commands.add("monitor reset halt");
   }

   public String getDefaultPortNumber() {
      return "61234";
   }

   private CpuCoreEnum getCpuCoreNameFromConfiguration(ILaunchConfiguration launchConfig) {
      if (launchConfig != null) {
         IConfiguration buildConfig = MCUDebugPlugin.getConfigFromDebugLaunch(launchConfig);
         if (buildConfig != null) {
            return (CpuCoreEnum)TargetHelper.getCpuCoreFromConfig(buildConfig).orElse(null);
         }
      }

      return null;
   }

   private boolean isCoreResetCandidate(CpuCoreEnum coreEnum) {
      boolean retVal = false;
      if (coreEnum != null) {
         switch($SWITCH_TABLE$com$st$stm32cube$common$ecosystemintegration$core$CpuCoreEnum()[coreEnum.ordinal()]) {
         case 21:
         case 22:
         case 28:
         case 29:
         case 30:
            retVal = false;
            break;
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         default:
            retVal = true;
         }
      }

      return retVal;
   }

   public boolean preLaunchCheck(ILaunchConfiguration config) throws CoreException {
      boolean startServer = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.startServer", true);
      if (startServer) {
         String serialNo = "";
         if (config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number", false)) {
            serialNo = config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", "");
         }

         if (!StLinkFwUtil.validate(serialNo)) {
            return false;
         }

         if (!this.downloadBinaryFile(config)) {
            return false;
         }

         boolean authEnabled = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth_check_enable", false);
         boolean pwdEnabled = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth__pwd_enable", false);
         if (authEnabled || pwdEnabled) {
            String debugAuthPwdPath = "";
            String debugAuthKeyPath = "";
            String debugAuthCertifPath = "";
            String permReq = "";
            if (authEnabled) {
               debugAuthKeyPath = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth_key_path", "");
               debugAuthCertifPath = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth_certif_path", "");
               permReq = this.fDebugAuth.getSelectedKey();
               if (permReq == null) {
                  String permission = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth_permission", "");
                  Map<String, String> permissions = DebugAuthPermission.getPermissions(DebugAuthLaunchWidget.getDieId());
                  permReq = (String)permissions.entrySet().stream().filter((entry) -> {
                     return ((String)entry.getValue()).equals(permission);
                  }).map(Entry::getKey).findFirst().orElse(null);
               }
            }

            if (pwdEnabled) {
               debugAuthPwdPath = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.debug_auth_pwd_file", "");
            }

            if (!CubeProgFwUtil.execCubeProgrammerDebugAuth(debugAuthKeyPath, debugAuthCertifPath, permReq, serialNo, debugAuthPwdPath, pwdEnabled)) {
               return false;
            }
         }
      }

      if (config.getAttribute("org.eclipse.cdt.dsf.gdb.NON_STOP", false)) {
         try {
            ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
            wc.setAttribute("org.eclipse.cdt.dsf.gdb.NON_STOP", false);
            wc.doSave();
         } catch (CoreException var12) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var12, "Failed to update launch config (non-stop attribute).");
            throw var12;
         }
      }

      return true;
   }

   public void createControl(Composite parent, IDebuggerTab dt) {
      super.createControl(parent, dt);
      this.settingsComp = parent;
      this.createGdbCmdLineInfo(this.settingsComp);
      this.createGroupInterface(this.settingsComp);
      this.createGroupResetStrategy(this.settingsComp);
      this.createDebugAuthLaunchWidget(this.settingsComp);
      this.createDebugAuthPwdWidget(this.settingsComp);
      this.createGroupDeviceConfig(this.settingsComp);
      this.createGroupSwvRtosProxy(this.settingsComp);
      this.createGroupExtLoaders(this.settingsComp);
      this.createGroupMisc(this.settingsComp);
   }

   private void createGdbCmdLineInfo(Composite comp) {
      Button button = new Button(this.settingsComp, 0);
      button.setText("Show Command Line");
      button.setToolTipText("Show the GDB Server command line with all parameters");
      button.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            ClipBoardDialog gdbCmdClipBoardDialog = new ClipBoardDialog(StLinkDebugHardware.this.settingsComp.getShell(), StLinkDebugHardware.this.gdbCmdLine);
            gdbCmdClipBoardDialog.create();
            gdbCmdClipBoardDialog.open();
         }
      });
   }

   private void createGroupInterface(Composite compl) {
      this.settingsComp.setLayout(new GridLayout(1, false));
      Group group = new Group(compl, 0);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText(Messages.StLinkDebugHardware_Interface);
      group.setLayout(new GridLayout(5, false));
      this.swdModeBtn = new Button(group, 16);
      GridDataFactory.swtDefaults().applyTo(this.swdModeBtn);
      this.swdModeBtn.setText("SWD");
      this.swdModeBtn.setSelection(false);
      this.jtagModeBtn = new Button(group, 16);
      GridDataFactory.swtDefaults().applyTo(this.jtagModeBtn);
      this.jtagModeBtn.setText("JTAG");
      this.jtagModeBtn.setSelection(true);
      GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(new Label(group, 0));
      this.btnCheckBoxSerialNumber = new Button(group, 32);
      this.btnCheckBoxSerialNumber.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.txtSerialNumber.setEnabled(StLinkDebugHardware.this.btnCheckBoxSerialNumber.getSelection());
            StLinkDebugHardware.this.btnScanStLinks.setEnabled(StLinkDebugHardware.this.btnCheckBoxSerialNumber.getSelection());
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      GridDataFactory.swtDefaults().applyTo(this.btnCheckBoxSerialNumber);
      this.btnCheckBoxSerialNumber.setText(Messages.StLinkDebugHardware_btnCheckBoxSerialNumber_text);
      this.txtSerialNumber = new Combo(group, 0);
      GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(this.txtSerialNumber);
      this.txtSerialNumber.setEnabled(false);
      this.txtSerialNumber.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnScanStLinks = new Button(group, 0);
      this.btnScanStLinks.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            String curVal = StLinkDebugHardware.this.txtSerialNumber.getText();
            boolean useSTLinkServer = StLinkDebugHardware.this.btnEnableSharedSTLink.getSelection();
            ThreadHelper.runInBackground(() -> {
               List<String> scanRes = StLinkDebugHardware.this.getConnectedStLinks(useSTLinkServer);
               ThreadHelper.runInUiThreadAsync(() -> {
                  if (scanRes.contains(curVal)) {
                     StLinkDebugHardware.this.txtSerialNumber.setItems((String[])scanRes.toArray(new String[scanRes.size()]));
                     StLinkDebugHardware.this.txtSerialNumber.select(scanRes.indexOf(curVal));
                  } else {
                     if (!curVal.isEmpty()) {
                        scanRes.add(0, curVal);
                     }

                     StLinkDebugHardware.this.txtSerialNumber.setItems((String[])scanRes.toArray(new String[scanRes.size()]));
                     StLinkDebugHardware.this.txtSerialNumber.select(0);
                  }

                  if (!curVal.equals(StLinkDebugHardware.this.txtSerialNumber.getText())) {
                     StLinkDebugHardware.this.updateLaunchDialog();
                  }

               });
            });
         }
      });
      this.btnScanStLinks.setText(Messages.StLinkDebugHardware_btnScanButton_text);
      this.btnScanStLinks.setEnabled(false);
      GridDataFactory.fillDefaults().grab(true, true).span(1, 1).applyTo(new Label(group, 0));
      this.jtagModeBtn.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.swdModeEnabled(false);
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.swdModeBtn.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.swdModeEnabled(true);
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      Label freqLbl = new Label(group, 0);
      freqLbl.setText("Frequency (kHz):");
      this.comboFrequency = new Combo(group, 0);
      this.comboFrequency.setItems(ComboAttributeItem.getDisplayNames(StLinkDebugConstants.frequencyOptions));
      GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(this.comboFrequency);
      GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(new Label(group, 0));
      this.comboFrequency.addVerifyListener(new VerifyListener() {
         public void verifyText(VerifyEvent e) {
            e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
         }
      });
      this.comboFrequency.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      Label apIdLbl = new Label(group, 0);
      apIdLbl.setText("Access port:");
      this.comboApId = new Combo(group, 0);
      this.comboApId.setItems(AP_ID_DEFAULT_LIST);
      GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(this.comboApId);
      this.comboApId.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
   }

   private void createGroupSwvRtosProxy(Composite parent) {
      Composite baseComp = new Composite(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(baseComp);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(baseComp);
      this.createGroupSwv(baseComp);
      this.createGroupRtosProxy(baseComp);
   }

   private void createGroupExtLoaders(Composite parent) {
      Group group = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText("External loaders");
      this.fExternalLoadersSettings = new ExtLoadersLaunchWidget(group, 0);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.fExternalLoadersSettings);
      this.fExternalLoadersSettings.setAttributeChangedListener(new Runnable() {
         public void run() {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
   }

   private void createGroupSwv(Composite parent) {
      Group group = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText(Messages.StLinkDebugHardware_SWV);
      this.btnEnableSwv = new Button(group, 32);
      GridDataFactory.swtDefaults().span(2, 1).applyTo(this.btnEnableSwv);
      this.btnEnableSwv.setText(Messages.StLinkDebugHardware_Enable);
      this.clockSettings = new ClockSettingsWidget(group, 0, this.getDebuggerTab());
      GridDataFactory.swtDefaults().span(2, 1).applyTo(this.clockSettings);
      this.lblSwvPortNumber = new Label(group, 0);
      GridDataFactory.swtDefaults().applyTo(this.lblSwvPortNumber);
      this.lblSwvPortNumber.setText(Messages.StLinkDebugHardware_PortNumber);
      this.txtSwvPort = new Text(group, 2048);
      GridDataFactory.swtDefaults().hint(100, -1).applyTo(this.txtSwvPort);
      this.btnEnableSwv.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.lblSwvPortNumber.setEnabled(StLinkDebugHardware.this.btnEnableSwv.getSelection());
            StLinkDebugHardware.this.txtSwvPort.setEnabled(StLinkDebugHardware.this.btnEnableSwv.getSelection());
            StLinkDebugHardware.this.clockSettings.setEnabled(StLinkDebugHardware.this.btnEnableSwv.getSelection());
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.txtSwvPort.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.txtSwvPort.addVerifyListener(new VerifyListener() {
         public void verifyText(VerifyEvent e) {
            e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
         }
      });
   }

   private void createGroupDeviceConfig(Composite compl) {
      Group group = new Group(this.settingsComp, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText(Messages.StLinkDebugHardware_McuSettings);
      this.lblLowPowerDbgCfg = new Label(group, 0);
      this.lblLowPowerDbgCfg.setText(Messages.StLinkDebugHardware_LowPowerDebug);
      this.lowPowerDbgCfg = new Combo(group, 8);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(this.lowPowerDbgCfg);
      this.lowPowerDbgCfg.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.lowPowerDbgCfg.setItems(ComboAttributeItem.getDisplayNames(StLinkDebugConstants.lowPowerCfgs));
      this.lowPowerDbgCfg.setText("enable");
      this.lblWatchdogCfg = new Label(group, 0);
      this.lblWatchdogCfg.setText(Messages.StLinkDebugHardware_WatchdogCtrs);
      this.watchdogCfg = new Combo(group, 8);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(this.watchdogCfg);
      this.watchdogCfg.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.watchdogCfg.setItems(ComboAttributeItem.getDisplayNames(StLinkDebugConstants.watchdogCfgs));
      this.watchdogCfg.setText("none");
      this.ctiGroup = this.createGroupCti(group);
      GridDataFactory.swtDefaults().span(2, 1).applyTo(this.ctiGroup);
   }

   private void createGroupMisc(Composite compl) {
      Group group = new Group(compl, 0);
      GridLayoutFactory.swtDefaults().numColumns(4).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText(Messages.StLinkDebugHardware_Misc);
      this.btnVerifyFlashDownload = new Button(group, 32);
      GridDataFactory.swtDefaults().span(4, 1).applyTo(this.btnVerifyFlashDownload);
      this.btnVerifyFlashDownload.setText(Messages.StLinkDebugHardware_VerifyFlashDownload);
      this.btnEnableLiveExpr = new Button(group, 32);
      GridDataFactory.swtDefaults().span(4, 1).applyTo(this.btnEnableLiveExpr);
      this.btnEnableLiveExpr.setText(Messages.StLinkDebugHardware_EnableLiveExpressions);
      this.btnEnableLogging = new Button(group, 32);
      GridDataFactory.swtDefaults().applyTo(this.btnEnableLogging);
      this.btnEnableLogging.setText(Messages.StLinkDebugHardware_LogToFile + ":");
      this.btnEnableLogging.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.txtLogFile.setEnabled(StLinkDebugHardware.this.btnEnableLogging.getSelection());
            StLinkDebugHardware.this.btnLogFileBrowse.setEnabled(StLinkDebugHardware.this.btnEnableLogging.getSelection());
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.txtLogFile = new Text(group, 2052);
      GridDataFactory.swtDefaults().align(4, 16777216).grab(true, false).hint(200, -1).applyTo(this.txtLogFile);
      this.txtLogFile.setEnabled(false);
      this.txtLogFile.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnLogFileBrowse = new Button(group, 0);
      GridDataFactory.swtDefaults().applyTo(this.btnLogFileBrowse);
      GridDataFactory.swtDefaults().span(2, 1).applyTo(this.btnLogFileBrowse);
      this.btnLogFileBrowse.setText(Messages.StLinkDebugHardware_Browse);
      this.btnLogFileBrowse.setEnabled(false);
      this.btnLogFileBrowse.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 4096);
            String path = fileDialog.open();
            if (path != null) {
               StLinkDebugHardware.this.txtLogFile.setText(path);
               StLinkDebugHardware.this.updateLaunchDialog();
            }

         }
      });
      this.btnVerifyFlashDownload.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnEnableLiveExpr.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnEnableSharedSTLink = new Button(group, 32);
      GridDataFactory.swtDefaults().span(4, 1).applyTo(this.btnEnableSharedSTLink);
      this.btnEnableSharedSTLink.setText(Messages.StLinkDebugHardware_EnableSharedSTLink);
      this.btnEnableSharedSTLink.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnEnableMaxHaltTimeout = new Button(group, 32);
      GridDataFactory.swtDefaults().applyTo(this.btnEnableMaxHaltTimeout);
      this.btnEnableMaxHaltTimeout.setText(Messages.StLinkDebugHardware_EnableMaxHaltDelay + "(s):");
      this.btnEnableMaxHaltTimeout.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.txtMaxHaltTimeout.setEnabled(StLinkDebugHardware.this.btnEnableMaxHaltTimeout.getSelection());
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.txtMaxHaltTimeout = new Text(group, 2052);
      GridDataFactory.swtDefaults().align(1, 16777216).grab(false, false).hint(50, -1).applyTo(this.txtMaxHaltTimeout);
      this.txtMaxHaltTimeout.setEnabled(false);
      this.txtMaxHaltTimeout.addModifyListener(new ModifyListener() {
         public void modifyText(ModifyEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.txtMaxHaltTimeout.addVerifyListener(new VerifyListener() {
         public void verifyText(VerifyEvent e) {
            e.doit = Character.isDigit(e.character) || Character.isISOControl(e.character);
         }
      });
   }

   private void createDebugAuthLaunchWidget(Composite parent) {
      this.debugAuthGroup = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this.debugAuthGroup);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.debugAuthGroup);
      this.debugAuthGroup.setText("Debug Authentication");
      this.debugAuthGroup.setToolTipText("Debug authentication using Key/Certificat");
      this.fDebugAuth = new DebugAuthLaunchWidget(this.debugAuthGroup, 0);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.fDebugAuth);
      this.fDebugAuth.setAttributeChangedListener(() -> {
         this.updateLaunchDialog();
      });
   }

   private void createDebugAuthPwdWidget(Composite parent) {
      this.debugAuthPwdGroup = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this.debugAuthPwdGroup);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.debugAuthPwdGroup);
      this.debugAuthPwdGroup.setText("Debug Authentication");
      this.debugAuthPwdGroup.setToolTipText("Debug authentication using Password");
      this.fDebugAuthPwd = new DebugAuthPwdWidget(this.debugAuthPwdGroup, 0);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.fDebugAuthPwd);
      this.fDebugAuthPwd.setAttributeChangedListener(() -> {
         this.updateLaunchDialog();
      });
   }

   private void createGroupResetStrategy(Composite parent) {
      Group group = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText("Reset behaviour");
      Composite type = new Composite(group, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(type);
      Label resetLbl = new Label(type, 0);
      resetLbl.setText("Type:");
      this.comboResetStrategy = new Combo(type, 8);
      this.comboResetStrategy.setItems((String[])StLinkDebugConstants.resetStrategies.stream().map((s) -> {
         return s.getDisplayName();
      }).toArray((var0) -> {
         return new String[var0];
      }));
      this.comboResetStrategy.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.validateHaltAllCoresEnabled();
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnHaltAllCores = new Button(group, 32);
      this.btnHaltAllCores.setText("Halt all cores");
      this.btnHaltAllCores.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
   }

   private Group createGroupCti(Composite parent) {
      Group group = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText("Cross Trigger Interface (CTI)");
      this.btnCtiAllowHalt = new Button(group, 32);
      this.btnCtiAllowHalt.setText("Allow other cores to halt this core");
      this.btnCtiAllowHalt.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.btnCtiSignalHalt = new Button(group, 32);
      this.btnCtiSignalHalt.setText("Signal halt events to other cores");
      this.btnCtiSignalHalt.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      return group;
   }

   private void createGroupRtosProxy(Composite parent) {
      Group group = new Group(parent, 0);
      GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
      group.setText("RTOS Kernel Awareness");
      this.btnRtosEnabled = new Button(group, 32);
      this.btnRtosEnabled.setText("Enable RTOS Proxy");
      this.btnRtosEnabled.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            StLinkDebugHardware.this.rtosProxySettings.setEnabled(StLinkDebugHardware.this.btnRtosEnabled.getSelection());
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
      this.rtosProxySettings = new RtosProxyLaunchWidget(group, 0);
      GridDataFactory.fillDefaults().grab(true, false).applyTo(this.rtosProxySettings);
      this.rtosProxySettings.setAttributeChangedListener(new Runnable() {
         public void run() {
            StLinkDebugHardware.this.updateLaunchDialog();
         }
      });
   }

   protected void swdModeEnabled(boolean enabled) {
      if (enabled) {
         if (this.fSwvSupported) {
            this.btnEnableSwv.setEnabled(true);
            this.clockSettings.setEnabled(this.btnEnableSwv.getSelection());
            this.lblSwvPortNumber.setEnabled(this.btnEnableSwv.getSelection());
            this.txtSwvPort.setEnabled(this.btnEnableSwv.getSelection());
         } else {
            this.btnEnableSwv.setSelection(false);
            this.btnEnableSwv.setEnabled(false);
            this.clockSettings.setEnabled(false);
            this.lblSwvPortNumber.setEnabled(false);
            this.txtSwvPort.setEnabled(false);
         }
      } else {
         this.btnEnableSwv.setEnabled(false);
         this.clockSettings.setEnabled(false);
         this.lblSwvPortNumber.setEnabled(false);
         this.txtSwvPort.setEnabled(false);
      }

   }

   private boolean isHaltAllCoresDesired(String resetAttr) {
      switch(resetAttr.hashCode()) {
      case -346664129:
         if (resetAttr.equals("system_reset")) {
            return true;
         }
         break;
      case 909117779:
         if (resetAttr.equals("connect_under_reset")) {
            return true;
         }
      }

      return false;
   }

   private void validateHaltAllCoresEnabled() {
      ResetStrategy selectedResetStrategy = ResetStrategy.find(this.comboResetStrategy.getText(), StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.NAME);
      if (selectedResetStrategy != null) {
         if (this.comboApId.getItemCount() > 1 && this.isHaltAllCoresDesired(selectedResetStrategy.getLaunchAttribute()) && this.btnCheckBoxSerialNumber.isEnabled()) {
            this.btnHaltAllCores.setEnabled(true);
            this.btnHaltAllCores.setVisible(true);
         } else {
            this.btnHaltAllCores.setEnabled(false);
            this.btnHaltAllCores.setVisible(false);
         }
      }

   }

   public String getServerInstallLocation(ILaunchConfiguration config) throws CoreException {
      try {
         String location = MCUExternalToolsPlugin.getGdbServerLocation("com.st.stm32cube.ide.mcu.externaltools.stlinkgdbserver.EXECUTABLE");
         String binName = MCUExternalToolsPlugin.getBinaryName("com.st.stm32cube.ide.mcu.externaltools.stlinkgdbserver.EXECUTABLE");
         return Paths.get(location, binName).toAbsolutePath().toString();
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var4) {
         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Failed to find ST-LINK install location", var4));
      }
   }

   private List<String> getAvailableExternalLoaders() {
      ArrayList res = new ArrayList();

      String gdbServer;
      String workingDir;
      try {
         gdbServer = this.getServerInstallLocation((ILaunchConfiguration)null);
         workingDir = this.getServerWorkingDir((ILaunchConfiguration)null);
      } catch (CoreException var13) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var13, "Failed to get install location or working directory");
         return res;
      }

      try {
         String[] cmd = new String[]{gdbServer, "-cp", this.getCubeProgrammerPath(), "--ext-memory-loaders"};
         String output = String.join("", this.blockingReadWithTimeout(cmd, workingDir, 15L, TimeUnit.SECONDS));
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setIgnoringElementContentWhitespace(true);
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(new ByteArrayInputStream(output.getBytes()));
         NodeList nodeList = doc.getElementsByTagName("ext-memory-loader");
         if (nodeList.getLength() == 0) {
            MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.toolchain", "Failed to list external loaders " + Arrays.toString(cmd) + " in " + workingDir);
            MCULoggerPlugin.infoDialog("com.st.stm32cube.ide.mcu.toolchain", "Failure", "Failed to list any external loaders");
         } else {
            for(int i = 0; i < nodeList.getLength(); ++i) {
               Node child = nodeList.item(i);
               String ext_loader = String.format("%s, %s, %s, %s", child.getAttributes().getNamedItem("name").getNodeValue(), child.getAttributes().getNamedItem("start").getNodeValue(), child.getAttributes().getNamedItem("type").getNodeValue(), FileUtils.getFileName(child.getTextContent()));
               res.add(ext_loader);
            }
         }
      } catch (Exception var14) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var14, "Failed to parse external loaders");
         MCULoggerPlugin.infoDialog("com.st.stm32cube.ide.mcu.toolchain", "Failure", "Failed to list external loaders");
         res.clear();
      }

      Collections.sort(res);
      return res;
   }

   private List<String> getConnectedStLinks(boolean useSTLinkServer) {
      ArrayList res = new ArrayList();

      String gdbServer;
      String workingDir;
      try {
         gdbServer = this.getServerInstallLocation((ILaunchConfiguration)null);
         workingDir = this.getServerWorkingDir((ILaunchConfiguration)null);
      } catch (CoreException var11) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var11, "Failed to get install location or working directory");
         return res;
      }

      try {
         List<String> cmd = new ArrayList(Arrays.asList(gdbServer, "-cp", this.getCubeProgrammerPath(), "-q"));
         if (useSTLinkServer) {
            cmd.add("-t");
         }

         List<String> lines = this.blockingReadWithTimeout((String[])cmd.toArray(new String[0]), workingDir, 15L, TimeUnit.SECONDS);
         Pattern pat = Pattern.compile(".*ST-LINK:([a-fA-F0-9]*)$");
         Iterator var9 = lines.iterator();

         while(var9.hasNext()) {
            String line = (String)var9.next();
            Matcher matcher = pat.matcher(line);
            if (matcher.matches()) {
               res.add(matcher.group(1));
            }
         }
      } catch (Exception var12) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var12, "Failed to list debug probes");
         MCULoggerPlugin.infoDialog("com.st.stm32cube.ide.mcu.toolchain", "Failure", "Failed to list debug probes");
         res.clear();
      }

      Collections.sort(res);
      return res;
   }

   private List<String> blockingReadWithTimeout(final String[] command, final String workingDirectory, final long timeout, final TimeUnit timeunit) throws Exception {
      long endTime = System.currentTimeMillis() + timeunit.toMillis(timeout);
      List<String> res = new ArrayList();
      ProcessBuilder procBuilder = new ProcessBuilder(command);
      procBuilder.directory(new File(workingDirectory));
      Process process = procBuilder.start();
      try {
         try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while(process.isAlive() && System.currentTimeMillis() < endTime) {
               String line;
               while((line = br.readLine()) != null) {
                  res.add(line);
               }
               process.waitFor(10L, TimeUnit.MILLISECONDS);
            }
         }
      } catch (InterruptedException var23) {
         process.destroyForcibly();
         throw var23;
      }
      if (process.isAlive()) {
         String msg = String.format("Timeout after %d %s, killing ST-LINK GDB server with cmd %s in %s", timeout, timeunit, Arrays.asList(command), workingDirectory);
         com.st.stm32cube.ide.mcu.toolchain.Activator.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", msg));
         process.destroyForcibly();
         process.waitFor();
      }
      if (!StLinkDebugHardware.ExitCode.SUCCESS.equals(process.exitValue())) {
         throw new IOException("Process terminated with exit code " + process.exitValue());
      } else {
         return res;
      }
   }

   public List<String> getServerStartArguments(ILaunchConfiguration configuration) throws CoreException {
      List<String> srvArgs = new ArrayList();
      srvArgs.add("-p");
      srvArgs.add("" + configuration.getAttribute("org.eclipse.cdt.debug.gdbjtag.core.portNumber", Integer.parseInt(this.getDefaultPortNumber())));
      String resetAttr;
      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_logging", false)) {
         srvArgs.add("-l");
         srvArgs.add("31");
         srvArgs.add("-v");
         resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.log_file", "").trim();
         File file = new File(resetAttr);
         if (file.getParentFile() != null && file.getParentFile().isDirectory()) {
            srvArgs.add("-f");
            srvArgs.add(resetAttr);
         }
      } else {
         srvArgs.add("-l");
         srvArgs.add("1");
      }

      String pattern;
      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swd_mode", false)) {
         srvArgs.add("-d");
         boolean enableSwv = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_swv", false);
         if (enableSwv) {
            pattern = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_port", "61235").trim();
            srvArgs.add("-z");
            srvArgs.add(pattern);
         }
      }

      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink", false)) {
         srvArgs.add("-t");
      }

      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.verify_flash_download", true)) {
         srvArgs.add("-s");
      }

      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number", false)) {
         resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", "").trim();
         if (!resetAttr.isEmpty()) {
            srvArgs.add("-i");
            srvArgs.add(resetAttr);
         }
      }

      srvArgs.add("-cp");
      srvArgs.add(this.getCubeProgrammerPath());
      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_external_loader", false)) {
         resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader", "").trim();
         if (!resetAttr.isEmpty()) {
            srvArgs.add("-el");
            pattern = "^(.*), (0x.*), (.*), (.*)$";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(resetAttr);
            if (m.find()) {
               srvArgs.add(m.group(4));
            } else {
               srvArgs.add(resetAttr);
            }

            if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader_init", false)) {
               srvArgs.add("--external-init");
            }
         }
      } else {
         resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.cubeprog_external_loaders", "").trim();
         List<CubeProgExtLoader> externalLoaders = CubeProgExtLoader.fromJson(resetAttr);
         Iterator var17 = externalLoaders.iterator();

         while(var17.hasNext()) {
            CubeProgExtLoader loader = (CubeProgExtLoader)var17.next();
            if (loader.getEnabled()) {
               String loaderParam = loader.getInitialize() ? "-ei" : "-el";
               srvArgs.add(loaderParam);
               String loaderIdResolved = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(loader.getLoaderPath());
               srvArgs.add(loaderIdResolved);
            }
         }
      }

      srvArgs.add("-m");
      srvArgs.add(configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", "0"));
      resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", "connect_under_reset");
      ResetStrategy resetStrategy = ResetStrategy.find(resetAttr, StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      if (resetStrategy != null) {
         srvArgs.addAll(resetStrategy.getCmdOptions());
      }

      if (this.isHaltAllCoresDesired(resetAttr) && configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.halt_all_on_reset", false)) {
         srvArgs.add("--halt");
      }

      String frequencyAttr;
      int frequency;
      if (configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_max_halt_delay", false)) {
         frequencyAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.max_halt_delay", "").trim();

         try {
            frequency = Integer.parseInt(frequencyAttr);
            if (frequency >= 0) {
               srvArgs.add("--pend-halt-timeout");
               srvArgs.add("" + frequency);
            }
         } catch (NumberFormatException var10) {
            MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.toolchain", "Failed to set --pend-halt-timeout option: " + frequencyAttr);
         }
      }

      frequencyAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", "0").trim();
      if (!frequencyAttr.equals("0")) {
         try {
            frequency = Integer.parseInt(frequencyAttr);
            if (frequency > 0) {
               srvArgs.add("--frequency");
               srvArgs.add("" + frequency);
            }
         } catch (NumberFormatException var9) {
            MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.toolchain", "Failed to set --frequency option: " + frequencyAttr);
         }
      }

      return srvArgs;
   }

   public String getServerWorkingDir(ILaunchConfiguration config) throws CoreException {
      try {
         String installPath = MCUExternalToolsPlugin.getGdbServerLocation("com.st.stm32cube.ide.mcu.externaltools.stlinkgdbserver.EXECUTABLE");
         if (installPath != null && !installPath.isEmpty()) {
            return installPath;
         } else {
            throw new IllegalArgumentException("No file returned");
         }
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var3) {
         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Failed to find ST-LINK working directory", var3));
      }
   }

   public void initializeFrom(ILaunchConfiguration configuration) {
      boolean swdMode = true;
      boolean useSerialNum = false;
      String serialNumTxt = "";
      boolean verifyFlash = true;
      boolean enableLiveExpre = false;
      boolean enableLogging = false;
      String logFilePathString = "";
      boolean enableSwv = false;
      String swvPort = "";
      String swvTraceHCLK = "";
      boolean isSwoClockLimited = false;
      String maxSwoClock = "";
      boolean enableExternalLoader = false;
      boolean enableSharedSTLink = false;
      String externalLoaderString = "";
      String resetStrategy = "connect_under_reset";
      String apId = "0";
      boolean haltAllOnReset = false;
      boolean ctiAllowHalt = false;
      boolean ctiSignalHalt = false;
      boolean enableMaxHaltDelay = false;
      String maxHaltDelay = "2";
      boolean externalLoaderInit = false;
      String enableLowPowerDbg = "enable";
      String stopWatchdogCtrs = "none";
      String frequency = "0";
      boolean startServer = true;
      boolean rtosEnabled = false;
      boolean isDebugAuthSupported = false;
      boolean isDieid = false;

      try {
         swdMode = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swd_mode", true);
         useSerialNum = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number", false);
         serialNumTxt = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", "");
         verifyFlash = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.verify_flash_download", true);
         enableLogging = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_logging", false);
         logFilePathString = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.log_file", "");
         enableExternalLoader = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_external_loader", false);
         externalLoaderString = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader", "");
         externalLoaderInit = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader_init", false);
         enableSharedSTLink = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink", false);
         enableSwv = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_swv", false);
         swvPort = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_port", "61235");
         swvTraceHCLK = DebugSettingService.getCpuClock(configuration, "com.st.stm32cube.ide.mcu.debug.launch.swv_trace_hclk", "16000000");
         isSwoClockLimited = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.enabled", false);
         maxSwoClock = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.value", "");
         enableLiveExpre = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_live_expr", true);
         resetStrategy = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", "connect_under_reset");
         apId = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", this.getDefaultAccessPortId(configuration));
         isDebugAuthSupported = this.isDebugAuthSupported(configuration);
         isDieid = this.DieidExist(configuration);
         haltAllOnReset = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.halt_all_on_reset", false);
         ctiAllowHalt = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
         ctiSignalHalt = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
         enableMaxHaltDelay = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_max_halt_delay", false);
         maxHaltDelay = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.max_halt_delay", "2");
         enableLowPowerDbg = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
         stopWatchdogCtrs = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
         frequency = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", "0");
         startServer = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.startServer", true);
         rtosEnabled = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlinkenable_rtos", false);
      } catch (CoreException var46) {
         var46.printStackTrace();
      }

      this.getDebuggerTab().setPerformPageUpdate(false);
      this.swdModeBtn.setSelection(swdMode);
      this.jtagModeBtn.setSelection(!swdMode);
      this.btnEnableSwv.setSelection(enableSwv);
      this.fSwvSupported = HardwareDebugUtil.isSwvSupported((Core)MCUDebugPlugin.getCoreFromConfig(configuration).orElse(null));
      this.swdModeEnabled(swdMode);
      this.btnCheckBoxSerialNumber.setSelection(useSerialNum);
      this.txtSerialNumber.setText(serialNumTxt);
      this.btnVerifyFlashDownload.setSelection(verifyFlash);
      this.clockSettings.setCoreClock(swvTraceHCLK);
      this.clockSettings.setMaxSwoClock(maxSwoClock, isSwoClockLimited);
      this.txtSwvPort.setText(swvPort);
      this.btnEnableLiveExpr.setSelection(enableLiveExpre);
      this.btnEnableLogging.setSelection(enableLogging);
      this.txtLogFile.setText(logFilePathString);
      this.btnEnableSharedSTLink.setSelection(enableSharedSTLink);
      this.btnHaltAllCores.setSelection(haltAllOnReset);
      this.btnCtiAllowHalt.setSelection(ctiAllowHalt);
      this.btnCtiSignalHalt.setSelection(ctiSignalHalt);
      this.btnEnableMaxHaltTimeout.setSelection(enableMaxHaltDelay);
      this.txtMaxHaltTimeout.setText(maxHaltDelay);
      this.txtMaxHaltTimeout.setEnabled(enableMaxHaltDelay);
      String rstStratDspName = "";
      ResetStrategy tmpRstStrat = ResetStrategy.find(resetStrategy, StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      if (tmpRstStrat == null) {
         tmpRstStrat = ResetStrategy.find("connect_under_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      }

      rstStratDspName = tmpRstStrat != null ? tmpRstStrat.getDisplayName() : "";
      this.comboResetStrategy.setText(rstStratDspName);
      String lowPowerDspName = "";
      ComboAttributeItem lowPwrItem = ComboAttributeItem.find(enableLowPowerDbg, StLinkDebugConstants.lowPowerCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE);
      if (lowPwrItem == null) {
         lowPwrItem = ComboAttributeItem.find("enable", StLinkDebugConstants.lowPowerCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE);
      }

      lowPowerDspName = lowPwrItem != null ? lowPwrItem.getDisplayName() : "";
      this.lowPowerDbgCfg.setText(lowPowerDspName);
      String watchdogDspName = "";
      ComboAttributeItem watchdogItem = ComboAttributeItem.find(stopWatchdogCtrs, StLinkDebugConstants.watchdogCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE);
      if (watchdogItem == null) {
         watchdogItem = ComboAttributeItem.find("none", StLinkDebugConstants.watchdogCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE);
      }

      watchdogDspName = watchdogItem != null ? watchdogItem.getDisplayName() : "";
      this.watchdogCfg.setText(watchdogDspName);
      Collection<Cpu> allCpus = this.getCpus(configuration);
      if (allCpus != null && !allCpus.isEmpty()) {
         String[] apIds = (String[])allCpus.stream().flatMap((cpu) -> {
            return cpu.getCores().stream();
         }).map((core) -> {
            return this.getApIdComboString(core);
         }).sorted().toArray((var0) -> {
            return new String[var0];
         });
         this.comboApId.setItems(apIds);
      } else {
         this.comboApId.setItems(AP_ID_DEFAULT_LIST);
      }

      ITargetObject target = MCUDebugPlugin.getTargetFromConfig(configuration);
      if (target != null && target.isMultiCpu() && this.isHaltAllCoresDesired(resetStrategy) && startServer) {
         this.btnHaltAllCores.setEnabled(true);
         this.btnHaltAllCores.setVisible(true);
      } else {
         this.btnHaltAllCores.setEnabled(false);
         this.btnHaltAllCores.setVisible(false);
      }

      this.lowPowerDbgCfg.setEnabled(false);
      this.watchdogCfg.setEnabled(false);
      this.btnCtiAllowHalt.setEnabled(false);
      this.btnCtiSignalHalt.setEnabled(false);
      this.ctiGroup.setEnabled(false);
      this.ctiGroup.setVisible(false);
      ((GridData)this.ctiGroup.getLayoutData()).exclude = true;
      if (target != null) {
         DeviceScript devScript = DeviceScript.find(configuration, StLinkDebugConstants.getDeviceScripts());
         if (devScript != null) {
            if (devScript.isSupported(DeviceScript.Feature.LOW_POWER_DEBUG)) {
               this.lowPowerDbgCfg.setEnabled(true);
            }

            if (devScript.isSupported(DeviceScript.Feature.DISABLE_WATCHDOG)) {
               this.watchdogCfg.setEnabled(true);
            }

            if (devScript.isSupported(DeviceScript.Feature.CTI_CONFIG)) {
               this.btnCtiAllowHalt.setEnabled(true);
               this.btnCtiSignalHalt.setEnabled(true);
               this.ctiGroup.setEnabled(true);
               this.ctiGroup.setVisible(true);
               ((GridData)this.ctiGroup.getLayoutData()).exclude = false;
            }
         }
      }

      this.ctiGroup.getParent().layout();
      String[] var43;
      int var42 = (var43 = this.comboApId.getItems()).length;

      for(int var41 = 0; var41 < var42; ++var41) {
         String item = var43[var41];
         if (apId.equals(this.getApIdFromComboString(item))) {
            apId = item;
         }

         this.debugAuthGroup.setVisible(isDebugAuthSupported);
         ((GridData)this.debugAuthGroup.getLayoutData()).exclude = !isDebugAuthSupported;
         this.debugAuthPwdGroup.setVisible(isDieid);
         ((GridData)this.debugAuthPwdGroup.getLayoutData()).exclude = !isDieid;
      }

      this.comboApId.setText(apId);
      ComboAttributeItem frequencyItem = ComboAttributeItem.find(frequency, StLinkDebugConstants.frequencyOptions, ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE);
      if (frequencyItem == null) {
         this.comboFrequency.setText(frequency);
      } else {
         this.comboFrequency.setText(frequencyItem.getDisplayName());
      }

      this.btnRtosEnabled.setSelection(rtosEnabled);
      this.rtosProxySettings.initializeFrom(configuration);
      this.rtosProxySettings.setEnabled(rtosEnabled);
      this.fDebugAuth.initializeFrom(configuration);
      this.fDebugAuthPwd.initializeFrom(configuration);
      if (!externalLoaderString.isEmpty()) {
         List<CubeProgExtLoader> loaders = new ArrayList();
         CubeProgExtLoader loader = new CubeProgExtLoader();
         String pattern = "^(.*), (0x.*), (.*), (.*)$";
         Pattern r = Pattern.compile(pattern);
         Matcher m = r.matcher(externalLoaderString);
         if (m.find()) {
            loader.setLoaderPath(m.group(4));
         } else {
            loader.setLoaderPath(externalLoaderString);
         }

         loader.setEnabled(enableExternalLoader);
         loader.setInitialize(externalLoaderInit);
         loaders.add(loader);
         this.fExternalLoadersSettings.initializeFrom(loaders);
      } else {
         this.fExternalLoadersSettings.initializeFrom(configuration);
      }

      this.updateGdbCmdLine(configuration);
      this.getDebuggerTab().setPerformPageUpdate(true);
   }

   private void updateGdbCmdLine(ILaunchConfiguration config) {
      ArrayList command = new ArrayList();

      try {
         String serverInstallLocation = this.getServerInstallLocation((ILaunchConfiguration)null);
         List<String> serverStartArguments = this.getServerStartArguments(config);
         command.add(serverInstallLocation);
         command.addAll(serverStartArguments);
         this.gdbCmdLine = String.join(" ", command);
      } catch (CoreException var5) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var5);
      }

   }

   public void performApply(ILaunchConfigurationWorkingCopy configuration) {
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.remoteCommand", "target remote");
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.swd_mode", this.swdModeBtn.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.verify_flash_download", this.btnVerifyFlashDownload.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_live_expr", this.btnEnableLiveExpr.getSelection());
      configuration.setAttribute("org.eclipse.cdt.dsf.gdb.NON_STOP", false);
      configuration.setAttribute("org.eclipse.cdt.launch.DEBUGGER_START_MODE", "remote");
      configuration.setAttribute("process_factory_id", "com.st.stm32cube.ide.mcu.debug.launch.HardwareDebugProcessFactory");
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_logging", this.btnEnableLogging.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.log_file", this.txtLogFile.getText());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink", this.btnEnableSharedSTLink.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_swv", this.btnEnableSwv.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_port", this.txtSwvPort.getText());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_trace_hclk", this.clockSettings.getCoreClock());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.enabled", this.clockSettings.isSwoClockLimited());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.value", this.clockSettings.getMaxSwoClock());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number", this.btnCheckBoxSerialNumber.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", this.txtSerialNumber.getText());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_max_halt_delay", this.btnEnableMaxHaltTimeout.getSelection());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.max_halt_delay", this.txtMaxHaltTimeout.getText());
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", this.comboFrequency.getText());
      if (this.btnHaltAllCores.isEnabled()) {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.halt_all_on_reset", this.btnHaltAllCores.getSelection());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.halt_all_on_reset", false);
      }

      ResetStrategy resetStrategy = ResetStrategy.find(this.comboResetStrategy.getText(), StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.NAME);
      if (resetStrategy != null) {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", resetStrategy.getLaunchAttribute());
      }

      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", this.getApIdFromComboString(this.comboApId.getText()));
      if (this.btnCtiAllowHalt.isEnabled()) {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", this.btnCtiAllowHalt.getSelection());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_allow_halt", false);
      }

      if (this.btnCtiSignalHalt.isEnabled()) {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", this.btnCtiSignalHalt.getSelection());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.cti_signal_halt", false);
      }

      ComboAttributeItem tmpItem;
      if (this.lowPowerDbgCfg.isEnabled()) {
         tmpItem = ComboAttributeItem.find(this.lowPowerDbgCfg.getText(), StLinkDebugConstants.lowPowerCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.DISPLAY_NAME);
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", tmpItem.getAttributeId());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "none");
      }

      if (this.watchdogCfg.isEnabled()) {
         tmpItem = ComboAttributeItem.find(this.watchdogCfg.getText(), StLinkDebugConstants.watchdogCfgs, ComboAttributeItem.ComboAttributeItemSearchKey.DISPLAY_NAME);
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", tmpItem.getAttributeId());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
      }

      tmpItem = ComboAttributeItem.find(this.comboFrequency.getText(), StLinkDebugConstants.frequencyOptions, ComboAttributeItem.ComboAttributeItemSearchKey.DISPLAY_NAME);
      if (tmpItem == null) {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", this.comboFrequency.getText());
      } else {
         configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", tmpItem.getAttributeId());
      }

      this.fDebugAuth.performApply(configuration);
      this.fDebugAuthPwd.performApply(configuration);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlinkenable_rtos", this.btnRtosEnabled.getSelection());
      this.rtosProxySettings.performApply(configuration);
      this.fExternalLoadersSettings.performApply(configuration);

      try {
         if (configuration.hasAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader")) {
            configuration.removeAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader");
            configuration.removeAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader_init");
            configuration.removeAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_external_loader");
         }
      } catch (CoreException var4) {
      }

      this.updateGdbCmdLine(configuration);
   }

   public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
      String projName = this.getProjectName(configuration);
      boolean swdMode = true;
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.swd_mode", swdMode);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.remoteCommand", "target remote");
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.verify_flash_download", true);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_logging", false);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_check_serial_number", false);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", "");
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.external_loader", "");
      configuration.setAttribute("org.eclipse.cdt.debug.gdbjtag.core.portNumber", Integer.parseInt(this.getDefaultPortNumber()));
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", this.getDefaultAccessPortId(configuration));
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_live_expr", true);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_max_halt_delay", false);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.frequency", "0");
      boolean shared = false;
      IConfiguration buildConfig = MCUDebugPlugin.getConfigFromDebugLaunch(configuration);
      Mcu mcu;
      if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
         try {
            mcu = TargetHelper.getMcu((IProject)buildConfig.getOwner());
            if (mcu != null && mcu.isCortexMMultiCpu()) {
               shared = true;
            }
         } catch (ClassCastException | CoreException var11) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var11);
         }
      }

      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink", shared);
      if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
         try {
            mcu = TargetHelper.getMcu((IProject)buildConfig.getOwner());
            if (mcu != null && mcu.getName().startsWith("STM32MP")) {
               configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", "no_reset");
            }
         } catch (ClassCastException | CoreException var10) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var10);
         }
      }

      String path = "";
      if (!projName.isEmpty()) {
         IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
         if (proj != null) {
            IPath path2 = proj.getLocation();
            if (path2 != null) {
               String configName = "Debug";
               if (buildConfig != null) {
                  configName = buildConfig.getName();
               }

               path = path2.append(configName).append("st-link_gdbserver_log.txt").toOSString();
            }
         }
      }

      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.stlink.log_file", path);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_swv", false);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_port", "61235");
      DebugSettingService.setCpuClockAtt(configuration, "com.st.stm32cube.ide.mcu.debug.launch.swv_trace_hclk", "16000000");
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.enabled", false);
      configuration.setAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.value", "");
   }

   private String getApIdFromComboString(String comboString) {
      String[] parts = comboString.split(" ");
      return parts[0];
   }

   private String getApIdComboString(Core core) {
      return core.getAPNum() + " - " + core.getCpuCore();
   }

   private String getCubeProgrammerPath() {
      try {
         return MCUExternalToolsPlugin.getCubeProgrammerLocation();
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var2) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var2, "Failed to get install location for STM32CubeProgrammer");
         return "";
      }
   }

   private Collection<Cpu> getCpus(ILaunchConfiguration configuration) {
      try {
         IConfiguration buildConfig = MCUDebugPlugin.getConfigFromDebugLaunch(configuration);
         if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
            Mcu mcu = TargetHelper.getMcu(buildConfig);
            if (mcu != null) {
               return mcu.getCpus();
            }
         }
      } catch (CoreException var4) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var4, "Failed to fetch CPU instances for launch configuration");
      }

      return Collections.emptyList();
   }

   public boolean getAutoStartDefault() {
      return true;
   }

   public void connectionModeChanged(boolean autoStartServer) {
      this.btnCheckBoxSerialNumber.setEnabled(autoStartServer);
      this.txtSerialNumber.setEnabled(autoStartServer && this.btnCheckBoxSerialNumber.getSelection());
      this.btnScanStLinks.setEnabled(autoStartServer && this.btnCheckBoxSerialNumber.getSelection());
      this.btnVerifyFlashDownload.setEnabled(autoStartServer);
      this.fDebugAuth.setEnabled(autoStartServer);
      this.fDebugAuthPwd.setEnabled(autoStartServer);
      this.btnEnableLogging.setEnabled(autoStartServer);
      this.txtLogFile.setEnabled(autoStartServer && this.btnEnableLogging.getSelection());
      this.btnLogFileBrowse.setEnabled(autoStartServer && this.btnEnableLogging.getSelection());
      this.fExternalLoadersSettings.setEnabled(autoStartServer);
      this.btnEnableSharedSTLink.setEnabled(autoStartServer);
      this.btnEnableMaxHaltTimeout.setEnabled(autoStartServer);
      this.txtMaxHaltTimeout.setEnabled(autoStartServer && this.btnEnableMaxHaltTimeout.getSelection());
      this.comboFrequency.setEnabled(autoStartServer);
      ResetStrategy resetStrategy = ResetStrategy.find("connect_under_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      if (resetStrategy != null) {
         int connUnderResCmbIdx = this.comboResetStrategy.indexOf(resetStrategy.getDisplayName());
         if (autoStartServer) {
            if (-1 == connUnderResCmbIdx) {
               this.comboResetStrategy.add(resetStrategy.getDisplayName(), 0);
            }
         } else if (-1 != connUnderResCmbIdx) {
            if (this.comboResetStrategy.getSelectionIndex() == connUnderResCmbIdx) {
               this.comboResetStrategy.select(connUnderResCmbIdx + 1);
            }

            this.comboResetStrategy.remove(resetStrategy.getDisplayName());
         }
      }

      this.validateHaltAllCoresEnabled();
   }

   public String getServerPersistentArg() {
      return "-e";
   }

   private String getServerErrorMsg(int exitVal, ILaunchConfiguration config) {
      MCULoggerPlugin.logErrorMessage("com.st.stm32cube.ide.mcu.toolchain", NLS.bind("ST-LINK GDB server failed to start (exit code = {0})", Integer.toString(exitVal)));
      String reason;
      switch($SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$StLinkDebugHardware$ExitCode()[StLinkDebugHardware.ExitCode.valueOf(exitVal).ordinal()]) {
      case 2:
         reason = Messages.StLinkDebugHardware_Error_FailedToConnect;
         break;
      case 3:
         reason = Messages.StLinkDebugHardware_Error_DLLError;
         break;
      case 4:
         reason = Messages.StLinkDebugHardware_Error_USBCommsErrorReconnect;
         break;
      case 5:
         reason = Messages.StLinkDebugHardware_Error_NoDeviceFound;
         break;
      case 6:
         reason = Messages.StLinkDebugHardware_Error_UnknownMCU;
         break;
      case 7:
         reason = Messages.StLinkDebugHardware_Error_FirmwareUpgradeRequired;
         break;
      case 8:
         reason = Messages.StLinkDebugHardware_Error_FailedResetTarget;
         break;
      case 9:
         reason = Messages.StLinkDebugHardware_Error_TargetUnderReset;
         break;
      case 10:
         reason = Messages.StLinkDebugHardware_Error_TargetNotHalted;
         break;
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      default:
         reason = Messages.StLinkDebugHardware_Error_Unknown;
         break;
      case 16:
         reason = Messages.StLinkDebugHardware_Error_SelectionRequired;
         break;
      case 17:
         String serialFail = "";

         try {
            serialFail = config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.stlink_txt_serial_number", "");
         } catch (CoreException var6) {
            var6.printStackTrace();
         }

         reason = NLS.bind(Messages.StLinkDebugHardware_Error_SerialNotFound, new String[]{serialFail});
         break;
      case 18:
         reason = Messages.StLinkDebugHardware_Error_UnknownVendor;
      }

      return NLS.bind(Messages.StLinkDebugHardware_Error_InitializingDevice, new String[]{Integer.toString(exitVal), reason});
   }

   public void verifyServer(ILaunchConfiguration config, IProcess iProcess) throws CoreException {
      try {
         Thread.sleep(2000L);
      } catch (InterruptedException var5) {
         var5.printStackTrace();
      }

      if (iProcess.isTerminated()) {
         int exitVal = iProcess.getExitValue();
         if (StLinkDebugHardware.ExitCode.FIRMWARE_TO_OLD_FOR_SWD.equals(exitVal)) {
            ThreadHelper.runInUiThreadAsync(() -> {
               if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "ST-LINK", Messages.StLinkDebugHardware_Error_FirmwareUpgradeRequiredQuestion)) {
                  IHandlerService handlerService = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);

                  try {
                     handlerService.executeCommand("com.st.stm32cube.ide.mcu.debug.stlink.fwupgrade", (Event)null);
                  } catch (Exception var2) {
                  }
               }

            });
         }

         String errMsg = this.getServerErrorMsg(exitVal, config);
         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", errMsg));
      }
   }

   public Map<String, String> getServerStartEnvironment() {
      return Collections.emptyMap();
   }

   public boolean isDebugFeatureSupported(DebugFeatureEnum debugFeature) {
      switch($SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$launch$DebugFeatureEnum()[debugFeature.ordinal()]) {
      case 1:
         return true;
      default:
         return false;
      }
   }

   public void doReset(Collection<String> commands) {
      ResetStrategy defaultStrategy = ResetStrategy.find("connect_under_reset", StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      if (defaultStrategy != null) {
         commands.addAll(defaultStrategy.getGDBCommands());
      }

   }

   public void doReset(ILaunchConfiguration configuration, Collection<String> commands) throws CoreException {
      String resetAttr = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", "connect_under_reset");
      ResetStrategy resetStrategy = ResetStrategy.find(resetAttr, StLinkDebugConstants.resetStrategies, ResetStrategySearchKey.ATTRIBUTE);
      if (resetStrategy != null) {
         commands.addAll(resetStrategy.getGDBCommands());
      }

   }

   public void doRestartInit(ILaunchConfiguration configuration) throws CoreException {
      String attribute = configuration.getAttribute("com.st.stm32cube.ide.mcu.debug.stlinkrestart_configurations", "");
      ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
      RestartListInfo restartListInfo = null;
      if (attribute.isEmpty()) {
         restartListInfo = new RestartListInfo(restartStrategies);
      } else {
         restartListInfo = (RestartListInfo)RestartListInfo.parseRestartListInfo(configuration).orElse(null);
         if (restartListInfo != null) {
            ArrayList<RestartStrategy> internalItems = restartListInfo.getInternalItems();

            RestartStrategy restartStrategy;
            for(Iterator var7 = internalItems.iterator(); var7.hasNext(); restartStrategy.setResetStrategies(resetForRestartStrategies)) {
               restartStrategy = (RestartStrategy)var7.next();
               if (restartStrategy.getResetAttribute().equals("Reset")) {
                  restartStrategy.setResetAttribute("Software system reset");
               }
            }
         } else {
            restartListInfo = new RestartListInfo(restartStrategies);
         }
      }

      if (restartListInfo != null) {
         workingCopy.setAttribute("com.st.stm32cube.ide.mcu.debug.stlinkrestart_configurations", restartListInfo.asJSON());
         workingCopy.doSave();
      }

   }

   public void doSetInitCommands(ILaunchConfiguration configuration, List<GDBServerCommandGroup> commandList) throws CoreException {
      DeviceScript devScript = DeviceScript.find(configuration, StLinkDebugConstants.getDeviceScripts());
      if (devScript != null) {
         GDBServerCommandGroup comGroup = new GDBServerCommandGroup("Device init script");
         List<RegisterManip> regManips = devScript.createInitScript(configuration);
         Iterator var7 = regManips.iterator();

         while(var7.hasNext()) {
            RegisterManip regManip = (RegisterManip)var7.next();
            comGroup.addCommand(RegisterManip.getGdbCommand(regManip));
         }

         commandList.add(comGroup);
      }

   }

   public void doSetDebugCommands(ILaunchConfiguration configuration, List<GDBServerCommandGroup> commandList) throws CoreException {
      DeviceScript devScript = DeviceScript.find(configuration, StLinkDebugConstants.getDeviceScripts());
      if (devScript != null) {
         GDBServerCommandGroup comGroup = new GDBServerCommandGroup("Device debug script");
         List<RegisterManip> regManips = devScript.createDebugScript(configuration);
         Iterator var7 = regManips.iterator();

         while(var7.hasNext()) {
            RegisterManip regManip = (RegisterManip)var7.next();
            comGroup.addCommand(RegisterManip.getGdbCommand(regManip));
         }

         commandList.add(comGroup);
      }

   }

   public boolean isDebugTargetSupported(String boardName, String mcuName) {
      return !mcuName.startsWith("STM32MP");
   }

   public void logAnalytics(ILaunchConfiguration config) {
      if (config != null) {
         IProject project = MCUDebugPlugin.getProject(config);

         try {
            if (config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_live_expr", false)) {
               MCUAnalytics.registerEvent(project, "CubeIDE_dbg_enableLiveExpression");
            }
         } catch (CoreException var10) {
         }

         try {
            if (config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.enable_swv", false)) {
               MCUAnalytics.registerEvent(project, "CubeIDE_dbg_enableSWV");
            }
         } catch (CoreException var9) {
         }

         try {
            DebugInterfaceEnum debugInterface = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swd_mode", false) ? DebugInterfaceEnum.SWD : DebugInterfaceEnum.JTAG;
            MCUAnalytics.registerData(project, "CubeIDE_debugInterface", MCUAnalyticsUtils.format(debugInterface.toString()));
         } catch (CoreException var8) {
         }

         try {
            if (config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.enable_shared_stlink", false)) {
               MCUAnalytics.registerEvent(project, "CubeIDE_dbg_enableSharedSTLink");
            }
         } catch (CoreException var7) {
         }

         String resetMode;
         try {
            resetMode = config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.low_power_debug", "enable");
            MCUAnalytics.registerData(project, "CubeIDE_debugIfLowPowerMode", MCUAnalyticsUtils.format(resetMode));
         } catch (CoreException var6) {
         }

         try {
            resetMode = config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.watchdog_config", "none");
            MCUAnalytics.registerData(project, "CubeIDE_debugStopWDG_onHaltMode", MCUAnalyticsUtils.format(resetMode));
         } catch (CoreException var5) {
         }

         try {
            resetMode = config.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", (String)null);
            if (resetMode != null && !resetMode.isEmpty()) {
               MCUAnalytics.registerData(project, "CubeIDE_debugResetMode", MCUAnalyticsUtils.format(resetMode));
            }
         } catch (CoreException var4) {
         }
      }

   }

   public String getRestartConfigurationsAttributName() {
      return "com.st.stm32cube.ide.mcu.debug.stlinkrestart_configurations";
   }

   public void createSWVsession(ILaunchConfiguration config, IGDBControl commandControl, DsfSession session, RequestMonitor requestMonitor) throws CoreException {
      String hclkAttr = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.swv_trace_hclk", "72000000");
      boolean isSwoClockLimited = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.enabled", false);
      String maxSwoClockAttr = config.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.limit_swo_clock.value", "");
      if (isSwoClockLimited && maxSwoClockAttr.isEmpty()) {
         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Missing SWO clock limit"));
      } else {
         try {
            final int cpuClock = Integer.parseInt(hclkAttr);
            final Optional userMaxSwoClock;
            if (isSwoClockLimited) {
               userMaxSwoClock = Optional.of(Integer.parseInt(maxSwoClockAttr));
            } else {
               userMaxSwoClock = Optional.empty();
            }

            DataRequestMonitor<MIInfo> infoMonitor = new DataRequestMonitor<MIInfo>(session.getExecutor(), requestMonitor) {
               protected void handleCompleted() {
                  Iterator var2 = HardwareDebugUtil.toStringFromMonitorReply((MIInfo)this.getData()).iterator();

                  while(var2.hasNext()) {
                     String result = (String)var2.next();
                     if (result != null && result.startsWith("OK:")) {
                        SwvInfo swvInfo = HardwareDebugUtil.toSwvInfoFromStLink(result.substring(3));
                        if (swvInfo != null) {
                           SwvConfig swvConfig = swvInfo.getBestConfiguration(userMaxSwoClock, cpuClock, 0.03D);
                           if (swvConfig != null) {
                              StLinkDebugHardware.this.createSWVsession(config, commandControl, session, requestMonitor, swvConfig);
                              return;
                           }

                           requestMonitor.done(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Unable to find a valid SWO frequency."));
                           return;
                        }
                     }
                  }

                  requestMonitor.done(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Target does not support SWV"));
               }
            };
            commandControl.queueCommand(new CLICommand(commandControl.getContext(), "monitor swv info"), infoMonitor);
         } catch (NumberFormatException var11) {
            throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Bad value in configuration" + var11.getMessage()));
         }
      }
   }

   private void createSWVsession(ILaunchConfiguration config, IGDBControl commandControl, DsfSession session, RequestMonitor requestMonitor, SwvConfig swvConfig) {
      DataRequestMonitor<MIInfo> rm = new DataRequestMonitor<MIInfo>(session.getExecutor(), requestMonitor) {
         protected void handleCompleted() {
            Iterator var2 = HardwareDebugUtil.toStringFromMonitorReply((MIInfo)this.getData()).iterator();

            String result;
            do {
               if (!var2.hasNext()) {
                  requestMonitor.done(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", "Could not start SWV"));
                  return;
               }

               result = (String)var2.next();
            } while(result == null || !result.equals("OK"));

            try {
               HardwareDebugUtil.createSWVsession(config, session, swvConfig);
               requestMonitor.done();
            } catch (CoreException var4) {
               MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.toolchain", var4, "Unable to create SWV session");
               requestMonitor.done(new Status(4, "com.st.stm32cube.ide.mcu.toolchain", var4.getMessage()));
            }
         }
      };
      commandControl.queueCommand(new CLICommand(commandControl.getContext(), "monitor swv start " + Integer.toHexString(swvConfig.swoClock / 1000)), rm);
   }

   public Optional<String> getConfigurationErrorMsg(ILaunchConfiguration launchConfig) {
      String maxSwoClock;
      try {
         maxSwoClock = launchConfig.getAttribute("com.st.stm32cube.ide.mcu.debug.launch.access_port_id", "0");
         Collection<Cpu> cpus = this.getCpus(launchConfig);
         Iterator var5 = cpus.iterator();

         while(var5.hasNext()) {
            Cpu cpu = (Cpu)var5.next();
            Collection<Core> cores = cpu.getCores();
            Iterator var8 = cores.iterator();

            while(var8.hasNext()) {
               Core core = (Core)var8.next();
               if (maxSwoClock.equals(core.getAPNum()) && !StLinkDebugConstants.supportedCores.contains(core.getCpuCore())) {
                  return Optional.of(NLS.bind("Unsupported core for the selected debug probe: {0}", core.getCpuCore()));
               }
            }
         }
      } catch (Exception var11) {
      }

      try {
         maxSwoClock = launchConfig.getAttribute("com.st.stm32cube.ide.mcu.debug.stlink.reset_strategy", (String)null);
         if (maxSwoClock.equals("core_reset")) {
            CpuCoreEnum coreEnum = this.getCpuCoreNameFromConfiguration(launchConfig);
            if (coreEnum != null && !this.isCoreResetCandidate(coreEnum)) {
               return Optional.of(String.format("The %s does not support the reset mode: Core reset!", coreEnum));
            }
         }
      } catch (Exception var10) {
      }

      if (this.clockSettings.isSwoClockLimited()) {
         maxSwoClock = this.clockSettings.getMaxSwoClock();
         if (maxSwoClock.trim().isEmpty()) {
            return Optional.of("Missing SWO clock limit");
         }

         try {
            if (Integer.parseInt(maxSwoClock) < 1) {
               throw new NumberFormatException();
            }
         } catch (NumberFormatException var9) {
            return Optional.of("Invalid SWO clock limit");
         }
      }

      return super.getConfigurationErrorMsg();
   }

   private String getDefaultAccessPortId(ILaunchConfiguration configuration) {
      String apId = "0";
      IConfiguration buildConfig = MCUDebugPlugin.getConfigFromDebugLaunch(configuration);
      if (buildConfig != null) {
         Core core = (Core)TargetHelper.getCoreFromConfig(buildConfig).orElse(null);
         if (core != null) {
            apId = core.getAPNum();
         }
      }

      return apId;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$st$stm32cube$common$ecosystemintegration$core$CpuCoreEnum() {
      int[] var10000 = $SWITCH_TABLE$com$st$stm32cube$common$ecosystemintegration$core$CpuCoreEnum;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[CpuCoreEnum.values().length];

         try {
            var0[CpuCoreEnum.ARM7TDMI.ordinal()] = 1;
         } catch (NoSuchFieldError var33) {
         }

         try {
            var0[CpuCoreEnum.ARM7TDMI_S.ordinal()] = 2;
         } catch (NoSuchFieldError var32) {
         }

         try {
            var0[CpuCoreEnum.ARM9.ordinal()] = 3;
         } catch (NoSuchFieldError var31) {
         }

         try {
            var0[CpuCoreEnum.ARM920T.ordinal()] = 4;
         } catch (NoSuchFieldError var30) {
         }

         try {
            var0[CpuCoreEnum.ARM922T.ordinal()] = 5;
         } catch (NoSuchFieldError var29) {
         }

         try {
            var0[CpuCoreEnum.ARM926EJ_S.ordinal()] = 6;
         } catch (NoSuchFieldError var28) {
         }

         try {
            var0[CpuCoreEnum.ARM946E_S.ordinal()] = 7;
         } catch (NoSuchFieldError var27) {
         }

         try {
            var0[CpuCoreEnum.ARM966E_S.ordinal()] = 8;
         } catch (NoSuchFieldError var26) {
         }

         try {
            var0[CpuCoreEnum.ARM968E_S.ordinal()] = 9;
         } catch (NoSuchFieldError var25) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A12.ordinal()] = 14;
         } catch (NoSuchFieldError var24) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A15.ordinal()] = 15;
         } catch (NoSuchFieldError var23) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A17.ordinal()] = 16;
         } catch (NoSuchFieldError var22) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A35.ordinal()] = 17;
         } catch (NoSuchFieldError var21) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A5.ordinal()] = 10;
         } catch (NoSuchFieldError var20) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A53.ordinal()] = 18;
         } catch (NoSuchFieldError var19) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A57.ordinal()] = 19;
         } catch (NoSuchFieldError var18) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A7.ordinal()] = 11;
         } catch (NoSuchFieldError var17) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A72.ordinal()] = 20;
         } catch (NoSuchFieldError var16) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A8.ordinal()] = 12;
         } catch (NoSuchFieldError var15) {
         }

         try {
            var0[CpuCoreEnum.Cortex_A9.ordinal()] = 13;
         } catch (NoSuchFieldError var14) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M0.ordinal()] = 21;
         } catch (NoSuchFieldError var13) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M0plus.ordinal()] = 22;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M1.ordinal()] = 23;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M23.ordinal()] = 27;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M3.ordinal()] = 24;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M33.ordinal()] = 28;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M4.ordinal()] = 25;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M55.ordinal()] = 29;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M7.ordinal()] = 26;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[CpuCoreEnum.Cortex_M85.ordinal()] = 30;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[CpuCoreEnum.Cortex_R4.ordinal()] = 31;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[CpuCoreEnum.Cortex_R5.ordinal()] = 32;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[CpuCoreEnum.Cortex_R7.ordinal()] = 33;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$st$stm32cube$common$ecosystemintegration$core$CpuCoreEnum = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$StLinkDebugHardware$ExitCode() {
      int[] var10000 = $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$StLinkDebugHardware$ExitCode;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[StLinkDebugHardware.ExitCode.values().length];

         try {
            var0[StLinkDebugHardware.ExitCode.APP_RESET_ERR.ordinal()] = 12;
         } catch (NoSuchFieldError var19) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.CMD_ERR.ordinal()] = 11;
         } catch (NoSuchFieldError var18) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.CONNECT_ERR.ordinal()] = 2;
         } catch (NoSuchFieldError var17) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.DEVICE_UNKNOWN_VENDOR.ordinal()] = 18;
         } catch (NoSuchFieldError var16) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.DLL_ERR.ordinal()] = 3;
         } catch (NoSuchFieldError var15) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.FIRMWARE_TO_OLD_FOR_SWD.ordinal()] = 7;
         } catch (NoSuchFieldError var14) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.FORCE_HALT_ERR.ordinal()] = 15;
         } catch (NoSuchFieldError var13) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.GET_STATUS_ERR.ordinal()] = 14;
         } catch (NoSuchFieldError var12) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.HELD_UNDER_RESET.ordinal()] = 9;
         } catch (NoSuchFieldError var11) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.NOT_HALTED.ordinal()] = 10;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.NO_DEVICE.ordinal()] = 5;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.RESET_ERR.ordinal()] = 8;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.STLINK_SELECT_REQ.ordinal()] = 16;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.STLINK_SERIAL_NOT_FOUND.ordinal()] = 17;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.SUCCESS.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.UNKNOWN_ERR.ordinal()] = 19;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.UNKNOWN_MCU_TARGET.ordinal()] = 6;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.USB_COMM_ERR.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[StLinkDebugHardware.ExitCode.VERSION_ERR.ordinal()] = 13;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$StLinkDebugHardware$ExitCode = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$launch$DebugFeatureEnum() {
      int[] var10000 = $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$launch$DebugFeatureEnum;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[DebugFeatureEnum.values().length];

         try {
            var0[DebugFeatureEnum.SWV.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$launch$DebugFeatureEnum = var0;
         return var0;
      }
   }

   private static enum ExitCode {
      SUCCESS(0),
      CONNECT_ERR(1),
      DLL_ERR(2),
      USB_COMM_ERR(3),
      NO_DEVICE(4),
      UNKNOWN_MCU_TARGET(5),
      FIRMWARE_TO_OLD_FOR_SWD(6),
      RESET_ERR(7),
      HELD_UNDER_RESET(8),
      NOT_HALTED(9),
      CMD_ERR(10),
      APP_RESET_ERR(11),
      VERSION_ERR(12),
      GET_STATUS_ERR(13),
      FORCE_HALT_ERR(14),
      STLINK_SELECT_REQ(16),
      STLINK_SERIAL_NOT_FOUND(17),
      DEVICE_UNKNOWN_VENDOR(18),
      UNKNOWN_ERR(255);

      private final int fCode;

      private ExitCode(int code) {
         this.fCode = code;
      }

      public boolean equals(int code) {
         return this.fCode == code;
      }

      public static StLinkDebugHardware.ExitCode valueOf(int code) {
         StLinkDebugHardware.ExitCode[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            StLinkDebugHardware.ExitCode exitCode = var4[var2];
            if (exitCode.fCode == code) {
               return exitCode;
            }
         }

         return UNKNOWN_ERR;
      }
   }
}
