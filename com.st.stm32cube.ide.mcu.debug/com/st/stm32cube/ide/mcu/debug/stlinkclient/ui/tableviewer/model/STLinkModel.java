package com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.model;

public class STLinkModel {
   private String fUsb_key;
   private String fID;
   private String fName;
   private Boolean fBlinkable;

   public STLinkModel(String usb_key, String id, String name, boolean blinkable) {
      this.fUsb_key = usb_key;
      this.fID = id;
      this.fName = name;
      this.fBlinkable = blinkable;
   }

   public boolean equals(Object elem) {
      if (elem instanceof STLinkModel) {
         return this.fID.equals(((STLinkModel)elem).getID());
      } else {
         return false;
      }
   }

   public String getID() {
      return this.fID;
   }

   public void setID(String fID) {
      this.fID = fID;
   }

   public String getName() {
      return this.fName;
   }

   public void setName(String fName) {
      this.fName = fName;
   }

   public boolean isBlinkable() {
      return this.fBlinkable;
   }

   public void setBlinkable(boolean value) {
      this.fBlinkable = value;
   }

   public String getUsb_key() {
      return this.fUsb_key;
   }
}
