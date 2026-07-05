package com.st.stm32cube.ide.mcu.debug.debugauth;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.mcu.externaltools.internal.registry.MCUExternalToolsRegistry;
import java.io.File;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DebugAuthPermission {
   public static LinkedHashMap<String, String> getPermissions(String dieId) {
      LinkedHashMap<String, String> permissions = new LinkedHashMap();
      if (dieId != null) {
         try {
            String locationCubeProg = MCUExternalToolsRegistry.getInstance().getCubeProgrammerLocation();
            if (locationCubeProg != null) {
               String filePathBin = locationCubeProg + File.separator + "bin" + File.separator + "DebugAuthPermissions.xml";
               String filePathLib = locationCubeProg + File.separator + "lib" + File.separator + "DebugAuthPermissions.xml";
               File fileWin = new File(filePathBin);
               File fileOs = new File(filePathLib);
               File fileToParse = fileWin.exists() ? fileWin : (fileOs.exists() ? fileOs : null);
               if (fileToParse == null) {
                  throw new CoreException(new Status(4, "com.st.stm32cube.ide.mcu.debug", "Cannot find DebugAuthPermissions.xml"));
               }

               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
               DocumentBuilder builder = factory.newDocumentBuilder();
               Document document = builder.parse(fileToParse);
               document.getDocumentElement().normalize();
               Element root = document.getDocumentElement();
               NodeList nodes = root.getElementsByTagName("*");

               for(int i = 0; i < nodes.getLength(); ++i) {
                  Node node = nodes.item(i);
                  if (node.getNodeType() == 1) {
                     Element element = (Element)node;
                     String elementDieId = element.toString();
                     if (elementDieId.contains(dieId)) {
                        NodeList permissionsListP = element.getElementsByTagName("Permission");
                        NodeList permissionsListF = element.getElementsByTagName("Feature");
                        addPermissions(permissionsListP, permissions);
                        addPermissions(permissionsListF, permissions);
                     }
                  }
               }
            }
         } catch (Exception var19) {
            MCULoggerPlugin.logException("com.st.stm32cube.ide.mcu.debug", var19, "Error parsing DebugAuthPermissions.xml");
         }
      }

      return permissions;
   }

   private static void addPermissions(NodeList permissionsList, LinkedHashMap<String, String> permissions) {
      for(int j = 0; j < permissionsList.getLength(); ++j) {
         Element permissionElement = (Element)permissionsList.item(j);
         String permissionValue = permissionElement.getTextContent();
         String[] parts = permissionValue.split(",");
         if (parts.length >= 2) {
            permissions.put(parts[0], parts[1]);
         }
      }

   }
}
