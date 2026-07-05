package com.st.stm32cube.ide.mcu.debug;

import com.st.stm32cube.common.ecosystemintegration.core.CpuCoreEnum;
import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.common.utils.ProjectHelper;
import com.st.stm32cube.ide.mcu.ide.core.TargetHelper;
import com.st.stm32cube.ide.mcu.productdb.core.Board;
import com.st.stm32cube.ide.mcu.productdb.core.Core;
import com.st.stm32cube.ide.mcu.productdb.core.ITargetObject;
import com.st.stm32cube.ide.mcu.productdb.core.Mcu;
import com.st.stm32cube.ide.mcu.toolchain.ToolChainHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MCUDebugPlugin extends AbstractUIPlugin {
   public static final String PLUGIN_ID = "com.st.stm32cube.ide.mcu.debug";
   public static final String DEBUG_PLUGIN_ID = "com.st.stm32cube.ide.mcu.debug/debug";
   public static final String DEBUG_STLINK_CLIENT = "com.st.stm32cube.ide.mcu.debug/debug/stlink_client";
   private static final String EMPTY_STRING = "";
   private static MCUDebugPlugin plugin;
   private static BundleContext fBundleContext;
   private static final Path STLINKTCP_PREFERENCES_XML;

   static {
      STLINKTCP_PREFERENCES_XML = new Path(System.getProperty("user.home") + File.separatorChar + ".stm32cubeide" + File.separator + "stlinktcp_preferences.xml");
   }

   public void start(BundleContext context) throws Exception {
      fBundleContext = context;
      super.start(context);
      plugin = this;
   }

   public void stop(BundleContext context) throws Exception {
      plugin = null;
      super.stop(context);
      fBundleContext = null;
   }

   public static MCUDebugPlugin getDefault() {
      return plugin;
   }

   public static IDialogSettings getSTLinkDialogSettings() {
      DialogSettings settings = new DialogSettings("stlinktcp");

      try {
         settings.load(STLINKTCP_PREFERENCES_XML.toOSString());
      } catch (IOException var4) {
         if (var4 instanceof FileNotFoundException) {
            IPath parent = STLINKTCP_PREFERENCES_XML.removeLastSegments(1);
            File file = new File(parent.toOSString());
            if (file != null) {
               file.mkdirs();
            }
         } else {
            logException(var4);
         }
      }

      return settings;
   }

   public static void saveSTLinkDialogSettings(IDialogSettings settings) {
      try {
         settings.save(STLINKTCP_PREFERENCES_XML.toOSString());
      } catch (IOException var2) {
         logException(var2);
      }

   }

   public static BundleContext getBundleContext() {
      return fBundleContext;
   }

   public static String getUniqueIdentifier() {
      return "com.st.stm32cube.ide.mcu.debug";
   }

   public static IProject getIProject(String projectName) {
      if (projectName != null && !projectName.isEmpty()) {
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
         return project;
      } else {
         return null;
      }
   }

   public static String formattedStr(String input) {
      return input.toLowerCase().replaceAll("\\W", "").replaceAll("_", "");
   }

   public static ImageDescriptor getImageDescriptor(String path) {
      return imageDescriptorFromPlugin("com.st.stm32cube.ide.mcu.debug", path);
   }

   public static void logException(Throwable e) {
      MCULoggerPlugin.logException(getUniqueIdentifier(), e);
   }

   public static IProject getProject(ILaunchConfiguration configuration) {
      try {
         String projectName = configuration.getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", "");
         if (projectName != null && !projectName.isEmpty()) {
            return getIProject(projectName);
         }
      } catch (CoreException var2) {
      }

      return null;
   }

   public static Boolean isMCUProject(ILaunchConfiguration configuration) {
      Boolean res = false;
      IProject project = getProject(configuration);

      try {
         if (project == null || project.isOpen() && project.hasNature("com.st.stm32cube.ide.mcu.MCUProjectNature")) {
            res = true;
         }
      } catch (CoreException var4) {
      }

      return res;
   }

   public static Boolean isDebugDeviceLessProject(ILaunchConfiguration configuration) {
      Boolean res = false;
      IProject project = getProject(configuration);
      if (project != null) {
         try {
            if (project.isOpen() && project.hasNature("com.st.stm32cube.ide.mcu.MCUNoDebugDeviceProjectNature")) {
               res = true;
            }
         } catch (CoreException var4) {
         }
      }

      return res;
   }

   public static boolean isDebuggingStatic() {
      return getDefault().isDebugging();
   }

   public static IConfiguration getConfigFromDebugLaunch(ILaunchConfiguration launchConfiguration) {
      IConfiguration configuration = null;

      try {
         IProject project = null;
         project = getProjectRef(launchConfiguration);
         if (project == null) {
            return null;
         }

         ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, false);
         if (desc == null) {
            return null;
         }

         String configID = launchConfiguration.getAttribute("org.eclipse.cdt.launch.PROJECT_BUILD_CONFIG_ID_ATTR", "");
         if (configID != null) {
            ICConfigurationDescription cconfigDesc = desc.getConfigurationById(configID);
            if (cconfigDesc != null) {
               configuration = getCfg(cconfigDesc);
            }
         } else {
            String progName = launchConfiguration.getAttribute("org.eclipse.cdt.launch.PROGRAM_NAME", "");
            progName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(progName);
            ICConfigurationDescription cconfigDesc = LaunchUtils.getBuildConfigByProgramPath(project, progName);
            if (cconfigDesc != null) {
               configuration = getCfg(cconfigDesc);
            }
         }

         if (configuration != null && desc.getConfigurationById(configuration.getId()) == null) {
            configuration = null;
         }

         if (configuration == null) {
            configuration = getCfg(desc.getActiveConfiguration());
         }
      } catch (CoreException var7) {
         logException(var7);
      }

      return configuration;
   }

   public static IConfiguration getCfg(ICConfigurationDescription cfgd) {
      if (cfgd instanceof ICMultiConfigDescription) {
         ICConfigurationDescription[] cfds = (ICConfigurationDescription[])((ICMultiConfigDescription)cfgd).getItems();

         for(int i = 0; i < cfds.length; ++i) {
            if (cfds[i] != null) {
               return ManagedBuildManager.getConfigurationForDescription(cfds[i]);
            }
         }
      }

      return ManagedBuildManager.getConfigurationForDescription(cfgd);
   }

   public static IProject getProjectRef(ILaunchConfiguration launchConfiguration) {
      if (launchConfiguration == null) {
         return getProjectRef();
      } else {
         IProject project = null;

         try {
            ICProject cProject = CDebugUtils.getCProject(launchConfiguration);
            if (cProject != null) {
               project = cProject.getProject();
            }

            return project == null ? null : project;
         } catch (CoreException var4) {
            return getProjectRef();
         }
      }
   }

   public static IProject getProjectRef() {
      IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
      if (resource == null) {
         return null;
      } else if (resource instanceof IProject) {
         return (IProject)resource;
      } else if (resource instanceof ICProject) {
         return ((ICProject)resource).getProject();
      } else if (resource instanceof IFile) {
         return resource.getProject();
      } else {
         return resource instanceof IFolder ? resource.getProject() : null;
      }
   }

   public static Optional<CpuCoreEnum> getCpuCoreFromConfig(ILaunchConfiguration launchConfig) {
      IConfiguration buildConfig = getConfigFromDebugLaunch(launchConfig);
      return buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig) ? TargetHelper.getCpuCoreFromConfig(buildConfig) : Optional.empty();
   }

   public static Optional<Core> getCoreFromConfig(ILaunchConfiguration launchConfig) {
      IConfiguration buildConfig = getConfigFromDebugLaunch(launchConfig);
      return buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig) ? TargetHelper.getCoreFromConfig(buildConfig) : Optional.empty();
   }

   public static Optional<Mcu> getMcuFromConfig(ILaunchConfiguration launchConfig) {
      Mcu mcu = null;
      IConfiguration buildConfig = getConfigFromDebugLaunch(launchConfig);
      if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
         try {
            mcu = TargetHelper.getMcu(buildConfig);
         } catch (CoreException var4) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var4);
         }
      }

      return Optional.ofNullable(mcu);
   }

   public static Optional<Board> getBoardFromConfig(ILaunchConfiguration launchConfig) {
      Board board = null;
      IConfiguration buildConfig = getConfigFromDebugLaunch(launchConfig);
      if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
         try {
            board = TargetHelper.getBoard(buildConfig);
         } catch (BuildException | CoreException var4) {
         }
      }

      return Optional.ofNullable(board);
   }

   public static ITargetObject getTargetFromConfig(ILaunchConfiguration launchConfig) {
      IConfiguration buildConfig = getConfigFromDebugLaunch(launchConfig);
      if (buildConfig != null && ToolChainHelper.isArmBareToolchainIntegration(buildConfig)) {
         try {
            Board board = TargetHelper.getBoard(buildConfig);
            if (board.isGeneric()) {
               return TargetHelper.getMcu(buildConfig);
            }

            return board;
         } catch (BuildException | CoreException var3) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var3);
         }
      }

      return null;
   }

   public static Optional<IFolder> getBuildDirectory(ILaunchConfiguration launchConfig) {
      try {
         String projectName = launchConfig.getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", "");
         String programPath = launchConfig.getAttribute("org.eclipse.cdt.launch.PROGRAM_NAME", "");
         if (programPath != null) {
            programPath = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programPath, true);
         }

         IProject project = (IProject)ProjectHelper.getProject(projectName).orElse((Object)null);
         if (project != null) {
            IContainer buildDirectory = project.getFile(programPath).getParent();
            if (buildDirectory != null && buildDirectory instanceof IFolder) {
               return Optional.of((IFolder)buildDirectory);
            }
         }
      } catch (CoreException var5) {
      }

      return Optional.empty();
   }

   public static boolean isDebugSupported(ILaunchConfiguration launchConfig) {
      boolean ret = false;
      ITargetObject target = getTargetFromConfig(launchConfig);
      if (target != null) {
         ret = true;
      }

      return ret;
   }
}
