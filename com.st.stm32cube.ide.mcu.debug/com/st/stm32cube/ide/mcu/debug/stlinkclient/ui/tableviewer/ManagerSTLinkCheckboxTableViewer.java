package com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ManagerSTLinkClient;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.STLinkTcpClient;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.edit.STLinkNameEditingSupport;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.model.STLinkModel;
import com.st.stm32cube.ide.mcu.debug.stlinkclient.ui.tableviewer.model.STLinkModelProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

public class ManagerSTLinkCheckboxTableViewer {
   private static final Image LED_ON_PNG = MCUDebugPlugin.getImageDescriptor("icons/led_on.png").createImage();
   private static final Image LED_OFF_PNG = MCUDebugPlugin.getImageDescriptor("icons/led_off.png").createImage();
   private CheckboxTableViewer fViewer = null;
   private Composite fTableLabelComposite;
   private Button fRefreshButton;
   private Label fNoBoardLabel;
   private final ListenerList<ManagerSTLinkCheckboxTableViewer.STLinkEventListener> listeners = new ListenerList();

   public ManagerSTLinkCheckboxTableViewer(Composite parent, STLinkTcpClient stlinkClient) {
      this.createManagerSTLinksControl(parent);
   }

   public ManagerSTLinkCheckboxTableViewer(Composite parent) {
      this.createManagerSTLinksControl(parent);
   }

   private void createManagerSTLinksControl(Composite parent) {
      Composite managerComposite = new Composite(parent, 0);
      managerComposite.setLayout(new GridLayout(2, false));
      GridDataFactory.fillDefaults().applyTo(managerComposite);
      this.fTableLabelComposite = new Composite(managerComposite, 0);
      this.fTableLabelComposite.setLayout(new GridLayout(1, false));
      Composite refreshButtonComposite = new Composite(managerComposite, 0);
      GridLayoutFactory.swtDefaults().applyTo(refreshButtonComposite);
      GridDataFactory.fillDefaults().grab(true, true).applyTo(refreshButtonComposite);
      this.fRefreshButton = new Button(refreshButtonComposite, 0);
      GridDataFactory.fillDefaults().applyTo(this.fRefreshButton);
      this.fRefreshButton.setToolTipText("refresh the list of STLink");
      this.fRefreshButton.setText("Refresh");
   }

   private void createSTLinksRedLabelControl() {
      this.dispose();
      this.fNoBoardLabel = new Label(this.fTableLabelComposite, 0);
      this.fNoBoardLabel.setForeground(Display.getCurrent().getSystemColor(3));
      this.fNoBoardLabel.setText("No board connected");
   }

   private void createSTLinksTableViewerControl() {
      this.dispose();
      this.fViewer = CheckboxTableViewer.newCheckList(this.fTableLabelComposite, 67600);
      this.fViewer.getTable().setHeaderVisible(true);
      this.fViewer.getTable().setLinesVisible(true);
      this.createColumns();
      this.fViewer.setContentProvider(new IStructuredContentProvider() {
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (((CheckboxTableViewer)viewer).getTable() != null && ((CheckboxTableViewer)viewer).getTable().getChildren() != null) {
               Control[] var7;
               int var6 = (var7 = ((CheckboxTableViewer)viewer).getTable().getChildren()).length;

               for(int var5 = 0; var5 < var6; ++var5) {
                  Control item = var7[var5];
                  if (item instanceof Button && (Button)item != null && !((Button)item).isDisposed()) {
                     ((Button)item).dispose();
                  }
               }
            }

         }

         public void dispose() {
         }

         public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Object[]) {
               return (Object[])inputElement;
            } else {
               return inputElement instanceof ArrayList ? ((ArrayList)inputElement).toArray() : new Object[0];
            }
         }
      });
      this.fViewer.addCheckStateListener(new ICheckStateListener() {
         public void checkStateChanged(CheckStateChangedEvent event) {
            if (event.getChecked()) {
               Object[] var5;
               int var4 = (var5 = ManagerSTLinkCheckboxTableViewer.this.fViewer.getCheckedElements()).length;

               for(int var3 = 0; var3 < var4; ++var3) {
                  Object checkedElem = var5[var3];
                  if (!((STLinkModel)event.getElement()).getID().equals(((STLinkModel)checkedElem).getID())) {
                     ManagerSTLinkCheckboxTableViewer.this.fViewer.setChecked(checkedElem, false);
                  } else {
                     boolean blinkable = ((STLinkModel)checkedElem).isBlinkable();
                     if (!blinkable) {
                        ManagerSTLinkCheckboxTableViewer.this.firmwareBadVersion();
                     }

                     ManagerSTLinkCheckboxTableViewer.this.fViewer.setChecked(checkedElem, blinkable);
                  }
               }
            }

            ManagerSTLinkCheckboxTableViewer.this.fireEventChanged();
         }
      });
      this.fViewer.setInput(STLinkModelProvider.INSTANCE.getSTLinks());
   }

   private void firmwareBadVersion() {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
         public void run() {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", "Please update the firmware version to use this board");
         }
      });
   }

   private void createColumns() {
      TableViewerColumn col = this.createTableViewerColumn("Name", 250, 0, "Name of the boards");
      col.setLabelProvider(new ColumnLabelProvider() {
         public String getText(Object element) {
            STLinkModel stlink = (STLinkModel)element;
            this.updateSTLinkNameInDialogSettings(stlink);
            return String.format("%s", stlink.getName());
         }

         private void updateSTLinkNameInDialogSettings(STLinkModel stlink) {
            IDialogSettings settings = MCUDebugPlugin.getSTLinkDialogSettings();
            IDialogSettings idSection = settings.getSection(stlink.getID());
            if (idSection == null) {
               IDialogSettings idSectionx = new DialogSettings(stlink.getID());
               idSectionx.put("NAME", stlink.getName());
               settings.addSection(idSectionx);
            } else {
               if (idSection.get("NAME") != null && idSection.get("NAME").equals(stlink.getName())) {
                  return;
               }

               idSection.put("NAME", stlink.getName());
            }

            MCUDebugPlugin.saveSTLinkDialogSettings(settings);
         }
      });
      col.setEditingSupport(new STLinkNameEditingSupport(this.fViewer));
      TableViewerColumn col2 = this.createTableViewerColumn("Blink", 50, 1, "Flash the led");
      col2.setLabelProvider(new ColumnLabelProvider() {
         Map<Object, Button> fButtons = new HashMap();

         public void update(final ViewerCell cell) {
            TableItem item = (TableItem)cell.getItem();
            final STLinkModel stlModel = (STLinkModel)cell.getElement();
            if (this.fButtons.containsKey(cell.getElement()) && !((Button)this.fButtons.get(cell.getElement())).isDisposed()) {
               Button var10000 = (Button)this.fButtons.get(cell.getElement());
            } else {
               Composite compositeButton = (Composite)cell.getViewerRow().getControl();
               final Button button = new Button(compositeButton, 2);
               button.setImage(ManagerSTLinkCheckboxTableViewer.LED_ON_PNG);
               this.fButtons.put(cell.getElement(), button);
               TableEditor editor = new TableEditor(item.getParent());
               editor.grabHorizontal = true;
               editor.grabVertical = true;
               editor.setEditor(button, item, cell.getColumnIndex());
               editor.layout();
               if (((STLinkModel)cell.getElement()).isBlinkable()) {
                  button.setToolTipText("Flash the stlink led to identify the board more easily.");
                  button.addSelectionListener(new SelectionAdapter() {
                     public void widgetSelected(SelectionEvent e) {
                        button.setSelection(false);

                        try {
                           Thread.sleep(200L);
                        } catch (InterruptedException var5) {
                           MCULoggerPlugin.logException(MCUDebugPlugin.getUniqueIdentifier(), var5);
                        }

                        (new Thread(new Runnable() {
                           public void run() {
                              for(int i = 0; i < 20; ++i) {
                                 Display.getDefault().asyncExec(new Runnable() {
                                    public void run() {
                                       button.setImage(button.getImage() == ManagerSTLinkCheckboxTableViewer.LED_OFF_PNG ? ManagerSTLinkCheckboxTableViewer.LED_ON_PNG : ManagerSTLinkCheckboxTableViewer.LED_OFF_PNG);
                                    }
                                 });

                                 try {
                                    Thread.sleep(100L);
                                 } catch (InterruptedException var3) {
                                    MCULoggerPlugin.logException(MCUDebugPlugin.getUniqueIdentifier(), var3);
                                 }
                              }

                           }
                        })).start();
                        String serialCode = stlModel.getID();
                        int usb_index = STLinkModelProvider.INSTANCE.getUsb_index(serialCode);
                        if (usb_index > -1) {
                           STLinkTcpClient stlinkclient = ManagerSTLinkClient.openClient();
                           stlinkclient.refresh();
                           stlinkclient.blink(usb_index);
                           ManagerSTLinkClient.closeClient(stlinkclient);
                        }

                     }
                  });
               } else {
                  button.setEnabled(false);
               }

               compositeButton.setToolTipText("The minimum requested version of stlink firmware is \"J28\", if necessary please update the firmware to use it.");
            }

         }
      });
   }

   private TableViewerColumn createTableViewerColumn(String title, final int bound, final int colNumber, final String tooltip) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this.fViewer, 0);
      TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(false);
      column.setToolTipText(tooltip);
      return viewerColumn;
   }

   public void refresh(String stlink_preselected) {
      Collection<STLinkModel> stlinks = STLinkModelProvider.INSTANCE.getSTLinks();
      if (stlinks.isEmpty()) {
         this.createSTLinksRedLabelControl();
      } else {
         this.setInput(stlinks);
         this.setChecked(stlink_preselected);
         this.refresh();
      }

   }

   public void refresh() {
      if (this.fViewer != null && !this.fViewer.getTable().isDisposed()) {
         this.fViewer.refresh();
         this.fViewer.getTable().computeSize(-1, this.fViewer.getTable().getItemHeight() * this.fViewer.getTable().getItemCount());
      }

   }

   public void setInput(Collection<STLinkModel> stlinks) {
      if (this.fViewer != null && !this.fViewer.getTable().isDisposed()) {
         this.fViewer.setInput(stlinks);
      } else {
         this.createSTLinksTableViewerControl();
      }

   }

   public void setChecked(String stlink_preselected) {
      STLinkModel stlink;
      Iterator var3;
      if (stlink_preselected != null && !stlink_preselected.isEmpty()) {
         this.fViewer.setAllChecked(false);
         var3 = STLinkModelProvider.INSTANCE.getSTLinks().iterator();

         while(var3.hasNext()) {
            stlink = (STLinkModel)var3.next();
            if (stlink.getID().equals(stlink_preselected)) {
               this.fViewer.setChecked(stlink, true);
               break;
            }
         }
      } else if (STLinkModelProvider.INSTANCE.getSTLinks().size() == 1) {
         var3 = STLinkModelProvider.INSTANCE.getSTLinks().iterator();

         while(var3.hasNext()) {
            stlink = (STLinkModel)var3.next();
            this.fViewer.setAllChecked(stlink.isBlinkable());
         }
      } else {
         this.fViewer.setAllChecked(false);
      }

   }

   public void dispose() {
      if (this.fNoBoardLabel != null) {
         this.fNoBoardLabel.dispose();
         this.fNoBoardLabel = null;
      }

      if (this.fViewer != null) {
         this.fViewer.getTable().dispose();
         this.fViewer = null;
      }

   }

   public void addEventListener(ManagerSTLinkCheckboxTableViewer.STLinkEventListener listener) {
      this.listeners.add(listener);
   }

   private void fireEventChanged() {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         ManagerSTLinkCheckboxTableViewer.STLinkEventListener listener = (ManagerSTLinkCheckboxTableViewer.STLinkEventListener)var2.next();
         listener.eventAppear();
      }

   }

   public CheckboxTableViewer getSTlinkTableViewer() {
      return this.fViewer;
   }

   public int getNbSTLinkConnected() {
      Collection<STLinkModel> stLinks = STLinkModelProvider.INSTANCE.getSTLinks();
      return stLinks.size();
   }

   public Button getRefreshButton() {
      return this.fRefreshButton;
   }

   public interface STLinkEventListener extends EventListener {
      void eventAppear();
   }
}
