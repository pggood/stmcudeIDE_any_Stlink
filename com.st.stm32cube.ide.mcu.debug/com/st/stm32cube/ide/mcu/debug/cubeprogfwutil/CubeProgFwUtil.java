package com.st.stm32cube.ide.mcu.debug.cubeprogfwutil;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.common.utils.ThreadHelper;
import com.st.stm32cube.ide.mcu.externaltools.MCUExternalToolsPlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;

public class CubeProgFwUtil {
   private static final String DEBUG_AUTHENTICATION = "Debug authentication";

   public static boolean execCubeProgrammerDebugAuth(String debugAuthKeyPath, String debugAuthCertifPath, String permission, String serialNo, String pwd, boolean isDebugAuthWithPassword) {
      try {
         String cubeProgExePath = getCubeProgrammerExeLocation();
         String[] command;
         if (isDebugAuthWithPassword) {
            if (!checkPwdExist(pwd)) {
               return false;
            } else {
               command = new String[]{cubeProgExePath, "-c", "port=swd", "pwd=" + pwd, "debugauth=1"};
               return runProcess(command);
            }
         } else if (!checkCertifAndKeyExist(debugAuthKeyPath, debugAuthCertifPath, permission)) {
            return false;
         } else {
            command = new String[]{cubeProgExePath, "-c", "port=swd", "per=" + permission, "key=" + debugAuthKeyPath, "cert=" + debugAuthCertifPath, "debugauth=1"};
            return runProcess(command);
         }
      } catch (Exception var8) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var8, "Failed to execute STM32CubeProgrammer debug authentication");
         return false;
      }
   }

   private static boolean runProcess(String[] command) throws IOException, InterruptedException {
      ProcessBuilder procBuilder = new ProcessBuilder(command);

      try {
         procBuilder.directory(new File(MCUExternalToolsPlugin.getCubeProgrammerLocation()));
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var15) {
         var15.printStackTrace();
      }

      procBuilder.redirectErrorStream(true);
      Process process = procBuilder.start();
      IOConsole console = getConsole();
      console.activate();
      IOConsoleOutputStream consoleStream = console.newOutputStream();
      Throwable var5 = null;
      Object var6 = null;

      try {
         BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

         try {
            for(String line = buffer.readLine(); line != null; line = buffer.readLine()) {
               consoleStream.write(line + "\n");
            }
         } finally {
            if (buffer != null) {
               buffer.close();
            }

         }
      } catch (Throwable var17) {
         if (var5 == null) {
            var5 = var17;
         } else if (var5 != var17) {
            var5.addSuppressed(var17);
         }

         throw var5;
      }

      int processExitValue = process.waitFor();
      if (processExitValue != 0) {
         ThreadHelper.runInUiThreadAsync(() -> {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Debug authentication", "Debug authentication failed, please check the Options Bytes settings, and enable trustzone if the used MCU supports TZ");
         });
      }

      return processExitValue == 0;
   }

   private static String getCubeProgrammerExeLocation() throws CoreException {
      try {
         String location = MCUExternalToolsPlugin.getCubeProgrammerLocation();
         String binName = MCUExternalToolsPlugin.getBinaryName("com.st.stm32cube.ide.mcu.externaltools.cubeprogrammer.EXECUTABLE");
         return Paths.get(location, binName).toAbsolutePath().toString();
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var2) {
         throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.debug", "Failed to find STM32CubeProgrammer_CLI location", var2));
      }
   }

   private static IOConsole getConsole() {
      String consoleName = "STM32CubeProgrammer console output";
      ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
      IConsoleManager consoleManager = consolePlugin.getConsoleManager();
      Stream var10000 = Arrays.stream(consoleManager.getConsoles());
      IOConsole.class.getClass();
      var10000 = var10000.filter(IOConsole.class::isInstance).filter((x) -> {
         return consoleName.equals(x.getName());
      });
      IOConsole.class.getClass();
      IOConsole console = (IOConsole)var10000.map(IOConsole.class::cast).findFirst().orElse((Object)null);
      if (console == null) {
         console = new MessageConsole(consoleName, (ImageDescriptor)null);
         consoleManager.addConsoles(new IConsole[]{(IConsole)console});
      } else {
         ((IOConsole)console).clearConsole();
      }

      return (IOConsole)console;
   }

   private static boolean checkCertifAndKeyExist(String debugAuthKeyPath, String debugAuthCertifPath, String permission) {
      if (!debugAuthKeyPath.isEmpty() && !debugAuthCertifPath.isEmpty()) {
         File keyFile = new File(debugAuthKeyPath);
         File certificateFile = new File(debugAuthCertifPath);
         StringBuilder errorMessage = new StringBuilder();
         if (!keyFile.exists()) {
            errorMessage.append("Key file does not exist: " + debugAuthKeyPath + "\n");
         }

         if (!certificateFile.exists()) {
            errorMessage.append("Certificate file does not exist: " + debugAuthCertifPath + "\n");
         }

         if (permission == null) {
            errorMessage.append("Permission is not selected \n");
         }

         if (errorMessage.length() > 0) {
            ThreadHelper.runInUiThreadAsync(() -> {
               MessageDialog.openError(Display.getCurrent().getActiveShell(), "Debug authentication", errorMessage.toString());
            });
            return false;
         } else {
            return true;
         }
      } else {
         ThreadHelper.runInUiThreadAsync(() -> {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Debug authentication", "Missing key and/or certificate for debug authentication");
         });
         return false;
      }
   }

   private static boolean checkPwdExist(String pwd) {
      if (!pwd.isEmpty()) {
         File pwdFile = new File(pwd);
         StringBuilder errorMessage = new StringBuilder();
         if (!pwdFile.exists()) {
            errorMessage.append("Password file does not exist: " + pwd + "\n");
         }

         if (errorMessage.length() > 0) {
            ThreadHelper.runInUiThreadAsync(() -> {
               MessageDialog.openError(Display.getCurrent().getActiveShell(), "Debug authentication", errorMessage.toString());
            });
            return false;
         } else {
            return true;
         }
      } else {
         ThreadHelper.runInUiThreadAsync(() -> {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Debug authentication", "Missing password for debug authentication");
         });
         return false;
      }
   }

   public static boolean download(String imageFileName, String downloadAddress, List<String> externalLoadersPaths) {
      try {
         String cubeProgExePath = getCubeProgrammerExeLocation();
         List<String> command = new ArrayList();
         command.add(cubeProgExePath);
         command.add("--connect");
         command.add("port=swd");
         if (externalLoadersPaths != null && externalLoadersPaths.size() > 0) {
            command.add("--extload");

            for(int i = 0; i < externalLoadersPaths.size(); ++i) {
               command.add((String)externalLoadersPaths.get(i));
            }
         }

         command.add("--download");
         command.add(imageFileName);
         command.add(downloadAddress);
         command.add("--verify");
         ProcessBuilder procBuilder = new ProcessBuilder(command);
         procBuilder.directory(new File(MCUExternalToolsPlugin.getCubeProgrammerLocation()));
         procBuilder.redirectErrorStream(true);
         Process process = procBuilder.start();
         IOConsole console = getConsole();
         console.activate();
         IOConsoleOutputStream consoleStream = console.newOutputStream();
         Throwable var9 = null;
         Object var10 = null;

         try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));

            try {
               for(String line = buffer.readLine(); line != null; line = buffer.readLine()) {
                  consoleStream.write(line + "\n");
               }
            } finally {
               if (buffer != null) {
                  buffer.close();
               }

            }
         } catch (Throwable var20) {
            if (var9 == null) {
               var9 = var20;
            } else if (var9 != var20) {
               var9.addSuppressed(var20);
            }

            throw var9;
         }

         int processExitValue = process.waitFor();
         if (processExitValue != 0) {
            ThreadHelper.runInUiThreadAsync(() -> {
               MessageDialog.openError(Display.getCurrent().getActiveShell(), "STM32CubeProgrammer programming", "STM32CubeProgrammer download failed");
            });
         }

         return processExitValue == 0;
      } catch (Exception var21) {
         MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var21, "Failed to execute STM32CubeProgrammer");
         return false;
      }
   }
}
