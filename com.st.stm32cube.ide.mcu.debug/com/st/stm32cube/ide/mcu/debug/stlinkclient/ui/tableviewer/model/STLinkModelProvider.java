package com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.model;

import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.debug.UiMessages;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ManagerSTLinkClient;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.STLinkTcpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;

public enum STLinkModelProvider {
   INSTANCE;

   private Collection<STLinkModel> fStlinks = new ArrayList();
   private static final String NAME = "NAME";
   private static final String BLINK = "BLINKABLE";

   public Collection<STLinkModel> getSTLinks() {
      return this.fStlinks;
   }

   public int getUsb_index(String serialCode) {
      IDialogSettings settings = MCUDebugPlugin.getSTLinkDialogSettings();
      IDialogSettings idSection = settings.getSection(serialCode);
      return idSection != null ? idSection.getInt(UiMessages.STLinkClient_Index) : -1;
   }

   public boolean updateSTLinks() {
      STLinkTcpClient client = ManagerSTLinkClient.openClient();
      if (client != null && client.refresh()) {
         IDialogSettings settings = MCUDebugPlugin.getSTLinkDialogSettings();
         Collection<STLinkModel> STLinksTmp = new ArrayList();
         STLinkModel STLinkModelTmp = null;
         STLinkTcpClient.DeviceInfo[] dev_connected = client.get_devices_connected();
         ManagerSTLinkClient.closeClient(client);
         if (dev_connected != null) {
            for(int i = 0; i < dev_connected.length; ++i) {
               String serialCode = dev_connected[i].getSerialId();
               IDialogSettings idSection = settings.getSection(serialCode);
               boolean blinkable = dev_connected[i].isBlinkable();
               String name;
               if (idSection == null) {
                  IDialogSettings idSection = new DialogSettings(serialCode);
                  name = serialCode;
                  idSection.put("NAME", serialCode);
                  idSection.put(UiMessages.STLinkClient_Index, i);
                  idSection.put("BLINKABLE", blinkable);
                  idSection.put(UiMessages.STLinkClient_Usb_Key, dev_connected[i].getDeviceId());
                  settings.addSection(idSection);
               } else {
                  name = idSection.get("NAME");
                  idSection.put(UiMessages.STLinkClient_Index, i);
                  idSection.put("BLINKABLE", blinkable);
               }

               STLinkModelTmp = new STLinkModel(dev_connected[i].getDeviceId(), serialCode, name, blinkable);
               STLinksTmp.add(STLinkModelTmp);
               boolean present = false;
               Iterator var13 = this.fStlinks.iterator();

               while(var13.hasNext()) {
                  STLinkModel current = (STLinkModel)var13.next();
                  if (current.equals(STLinkModelTmp)) {
                     current.setBlinkable(blinkable);
                     present = true;
                     break;
                  }
               }

               if (!present) {
                  this.fStlinks.add(STLinkModelTmp);
               }
            }

            MCUDebugPlugin.saveSTLinkDialogSettings(settings);
         }

         this.fStlinks.retainAll(STLinksTmp);
         return true;
      } else {
         return false;
      }
   }
}
