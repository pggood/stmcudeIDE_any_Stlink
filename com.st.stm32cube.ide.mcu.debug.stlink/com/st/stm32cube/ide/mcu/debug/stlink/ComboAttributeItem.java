package com.st.stm32cube.ide.mcu.debug.stlink;

import java.util.Iterator;
import java.util.List;

public class ComboAttributeItem {
   private String attributeId;
   private String displayName;
   // $FF: synthetic field
   private static volatile int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$ComboAttributeItem$ComboAttributeItemSearchKey;

   public ComboAttributeItem(String displayName, String attributeId) {
      this.displayName = displayName;
      this.attributeId = attributeId;
   }

   public String getAttributeId() {
      return this.attributeId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public static String[] getDisplayNames(List<ComboAttributeItem> list) {
      return (String[])list.stream().map((s) -> {
         return s.getDisplayName();
      }).toArray((var0) -> {
         return new String[var0];
      });
   }

   public static String[] getAttributeIds(List<ComboAttributeItem> list) {
      return (String[])list.stream().map((s) -> {
         return s.getAttributeId();
      }).toArray((var0) -> {
         return new String[var0];
      });
   }

   public static ComboAttributeItem find(String key, List<ComboAttributeItem> list, ComboAttributeItem.ComboAttributeItemSearchKey keyType) {
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         ComboAttributeItem item = (ComboAttributeItem)var4.next();
         switch($SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$ComboAttributeItem$ComboAttributeItemSearchKey()[keyType.ordinal()]) {
         case 1:
            if (item.getDisplayName().equals(key)) {
               return item;
            }
            break;
         case 2:
            if (item.getAttributeId().equals(key)) {
               return item;
            }
         }
      }

      return null;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$ComboAttributeItem$ComboAttributeItemSearchKey() {
      int[] var10000 = $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$ComboAttributeItem$ComboAttributeItemSearchKey;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ComboAttributeItem.ComboAttributeItemSearchKey.values().length];

         try {
            var0[ComboAttributeItem.ComboAttributeItemSearchKey.ATTRIBUTE.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ComboAttributeItem.ComboAttributeItemSearchKey.DISPLAY_NAME.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$st$stm32cube$ide$mcu$debug$stlink$ComboAttributeItem$ComboAttributeItemSearchKey = var0;
         return var0;
      }
   }

   public static enum ComboAttributeItemSearchKey {
      DISPLAY_NAME,
      ATTRIBUTE;
   }
}
