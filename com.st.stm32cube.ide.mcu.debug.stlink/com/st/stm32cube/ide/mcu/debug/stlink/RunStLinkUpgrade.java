package com.st.stm32cube.ide.mcu.debug.stlink;

import com.st.stm32cube.ide.mcu.externaltools.MCUExternalToolsPlugin;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class RunStLinkUpgrade extends AbstractHandler {
   private final List<String> SUPPORTED_OS = Arrays.asList("win32", "linux", "macosx");

   public Object execute(ExecutionEvent event) throws ExecutionException {
      String os = Platform.getOS();

      String gdbServerPath;
      try {
         gdbServerPath = MCUExternalToolsPlugin.getGdbServerLocation("com.st.stm32cube.ide.mcu.externaltools.stlinkgdbserver.EXECUTABLE");
      } catch (InterruptedException | URISyntaxException | IOException | IllegalArgumentException var7) {
         Activator.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug.stlink", Messages.RunStLinkUpgrade_Error_FailedToLocateSTLinkUpgradeTool, var7));
         throw new ExecutionException(Messages.RunStLinkUpgrade_Error_FailedToLocateSTLinkUpgradeTool, var7.getCause());
      }

      if (os != null && this.SUPPORTED_OS.contains(os)) {
         ProcessBuilder procBuilder = new ProcessBuilder(new String[]{System.getProperty("java.home") + "/bin/java", "-jar", "STLinkUpgrade.jar"});
         procBuilder.directory(new File(gdbServerPath));

         try {
            procBuilder.start();
         } catch (Exception var6) {
            Activator.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug.stlink", Messages.RunStLinkUpgrade_Error_LaunchingSTLinkUpgradeTool, var6));
         }

         return null;
      } else {
         Activator.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug.stlink", Messages.RunStLinkUpgrade_Error_LaunchingSTLinkUpgradeTool));
         return null;
      }
   }
}
