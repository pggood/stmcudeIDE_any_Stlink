package com.st.stm32cube.ide.mcu.debug.ui.views;

import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.debug.utils.GDBDSFHelper;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Filter;

public class DebugContextManager implements IDebugContextListener {
   private IDebugContextManagerListener fListener;
   private IWorkbenchWindow fWorkbench;
   private DsfServicesTracker fServicesTracker;
   private boolean fSuspended;
   private ILaunchConfiguration fActiveLaunchConfig;
   private DsfSession fActiveDsfSession;
   private String fActiveSessionId;
   private DebugContextManager.Debugger fDebugger;
   public static final int DSF_LAUNCH = 0;
   public static final int UNSPECIFIED_LAUNCH = 3;

   public DebugContextManager(IDebugContextManagerListener listener) {
      this(listener, (IWorkbenchWindow)null);
   }

   public DebugContextManager(IDebugContextManagerListener listener, IWorkbenchWindow workbench) {
      this.fSuspended = true;
      this.fActiveDsfSession = null;
      this.fActiveSessionId = null;
      this.fListener = listener;
      this.fWorkbench = workbench;
      this.fDebugger = new DebugContextManager.Debugger();
   }

   public void start() {
      if (this.fWorkbench == null) {
         this.fWorkbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      }

      IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(this.fWorkbench);
      contextService.addDebugContextListener(this);
      Object context = null;
      Object selection = contextService.getActiveContext();
      if (selection instanceof StructuredSelection) {
         context = ((StructuredSelection)selection).getFirstElement();
      }

      this.handleDebugContextChanged(context);
   }

   public void debugContextChanged(final DebugContextEvent event) {
      this.handleDebugContextChanged(((StructuredSelection)event.getContext()).getFirstElement());
   }

   private void handleDebugContextChanged(Object context) {
      ILaunch contextLaunch = DebugUIPlugin.getLaunch(context);
      this.fActiveLaunchConfig = contextLaunch == null ? null : contextLaunch.getLaunchConfiguration();
      if (context instanceof IDMVMContext) {
         GdbLaunch gdbLaunch = (GdbLaunch)contextLaunch;
         String tmpSessionId = gdbLaunch.getSession().getId();
         if (!tmpSessionId.equals(this.fActiveSessionId)) {
            if (this.fActiveDsfSession != null) {
               this.fActiveDsfSession.removeServiceEventListener(this);
               this.fServicesTracker.dispose();
            }

            this.fActiveSessionId = tmpSessionId;
            this.fActiveDsfSession = DsfSession.getSession(tmpSessionId);
            if (this.fActiveDsfSession == null) {
               MCUDebugPlugin.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug", "Error - No active DSF-Session. tmpSessionId = " + tmpSessionId.toString() + " (contextManager row 121)"));
               MCUDebugPlugin.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug", "Existing sessions are:"));
               DsfSession[] dsfSessions = DsfSession.getActiveSessions();

               for(int i = 0; i < dsfSessions.length; ++i) {
                  MCUDebugPlugin.getDefault().getLog().log(new Status(4, "com.st.stm32cube.ide.mcu.debug", dsfSessions[i].getId()));
               }
            } else {
               this.fActiveDsfSession.addServiceEventListener(this, (Filter)null);
            }

            this.fServicesTracker = new DsfServicesTracker(MCUDebugPlugin.getDefault().getBundle().getBundleContext(), tmpSessionId);
            IRunControl rc = (IRunControl)this.fServicesTracker.getService(IRunControl.class);
            if (rc != null && rc instanceof IRunControl && rc.isSuspended((IExecutionDMContext)DMContexts.getAncestorOfType(((IDMVMContext)context).getDMContext(), IExecutionDMContext.class))) {
               this.fSuspended = true;
            } else {
               this.fSuspended = false;
            }

            this.fListener.handleSession(this.fActiveSessionId);
         }
      } else {
         if (this.fActiveDsfSession != null) {
            this.fActiveDsfSession.removeServiceEventListener(this);
            this.fServicesTracker.dispose();
         }

         this.fActiveDsfSession = null;
         this.fServicesTracker = null;
         this.fActiveSessionId = null;
         this.fListener.handleSession((String)null);
      }

   }

   public int getActiveLaunchType() {
      return 3;
   }

   public DebugContextManager.Debugger getDebugger() {
      return this.fDebugger;
   }

   public String getActiveSessionId() {
      return this.fActiveDsfSession.getId();
   }

   public DsfSession getActiveSession() {
      return this.fActiveDsfSession;
   }

   public ILaunchConfiguration getActiveLaunchConfiguration() {
      return this.fActiveLaunchConfig;
   }

   public DsfServicesTracker getfServicesTracker() {
      return this.fServicesTracker;
   }

   public IMemoryDMContext getMemoryContext() {
      Object dbgContext = DebugUITools.getDebugContext();
      IDMVMContext dmvmContext = null;
      if (dbgContext instanceof IDMVMContext) {
         dmvmContext = (IDMVMContext)DebugUITools.getDebugContext();
      }

      if (dmvmContext == null) {
         return null;
      } else {
         IDMContext dmContext = dmvmContext.getDMContext();
         return dmContext == null ? null : (IMemoryDMContext)DMContexts.getAncestorOfType(dmContext, IMemoryDMContext.class);
      }
   }

   @DsfServiceEventHandler
   public void handleDsfEvent(Object event) {
      if (event instanceof IDMEvent) {
         IDMEvent<?> idmEvent = (IDMEvent)event;
         IDMContext context = idmEvent.getDMContext();
         if (context instanceof IExecutionDMContext) {
            if (idmEvent instanceof ISuspendedDMEvent) {
               this.fSuspended = true;
               Display.getDefault().asyncExec(new Runnable() {
                  public void run() {
                     DebugContextManager.this.fListener.handleSuspend();
                  }
               });
            } else if (idmEvent instanceof IResumedDMEvent) {
               IResumedDMEvent realEvent = (IResumedDMEvent)idmEvent;
               if (realEvent.getReason() == StateChangeReason.USER_REQUEST) {
                  this.fSuspended = false;
                  Display.getDefault().asyncExec(new Runnable() {
                     public void run() {
                        DebugContextManager.this.fListener.handleResume();
                     }
                  });
               }
            } else if (idmEvent instanceof IExitedDMEvent) {
               Display.getDefault().asyncExec(new Runnable() {
                  public void run() {
                     DebugContextManager.this.fListener.handleExited();
                  }
               });
            }
         }
      }

   }

   public void dispose() {
      DebugUITools.getDebugContextManager().getContextService(this.fWorkbench).removeDebugContextListener(this);
      if (this.fActiveDsfSession != null) {
         this.fActiveDsfSession.removeServiceEventListener(this);
      }

      if (this.fServicesTracker != null) {
         this.fServicesTracker.dispose();
      }

   }

   public boolean targetSuspended() {
      return this.fSuspended;
   }

   public IProject getProject() {
      ILaunchConfiguration configuration = this.getActiveLaunchConfiguration();
      if (configuration != null) {
         String projectName = null;

         try {
            projectName = configuration.getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", "");
            if (projectName != null && !projectName.isEmpty()) {
               IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
               if (project.exists()) {
                  return project;
               }
            }
         } catch (CoreException var4) {
            var4.printStackTrace();
         }
      }

      return null;
   }

   public DsfExecutor getExecutor() {
      DsfSession dsfSession = this.getActiveSession();
      return dsfSession != null ? dsfSession.getExecutor() : null;
   }

   IMICommandControl getCommandControl() {
      DsfServicesTracker tracker = this.getServicesTracker();
      return tracker != null ? (IMICommandControl)tracker.getService(IMICommandControl.class) : null;
   }

   IExpressions getExpressionService() {
      DsfServicesTracker tracker = this.getServicesTracker();
      return tracker != null ? (IExpressions)tracker.getService(IExpressions.class) : null;
   }

   IMIBackend getGDBBackend() {
      DsfServicesTracker tracker = this.getServicesTracker();
      return tracker != null ? (IMIBackend)tracker.getService(IMIBackend.class) : null;
   }

   CommandFactory getCommandFactory() {
      IMICommandControl commandControl = this.getCommandControl();
      return commandControl != null ? commandControl.getCommandFactory() : null;
   }

   public DsfServicesTracker getServicesTracker() {
      return this.getfServicesTracker();
   }

   public class Debugger {
      public String evaluateExpression(String expression) {
         return GDBDSFHelper.evaluateExpression(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), expression);
      }

      public void evaluateExpression(String expression, final DataRequestMonitor<MIDataEvaluateExpressionInfo> rm) {
         GDBDSFHelper.evaluateExpression(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), expression, rm);
      }

      public MemoryByte[] readMemory(String address, int nrBytes) {
         return GDBDSFHelper.readMemory(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), address, nrBytes);
      }

      public void readMemory(String address, int nrBytes, final DataRequestMonitor<MIDataReadMemoryInfo> rm) {
         GDBDSFHelper.readMemory(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), address, nrBytes, rm);
      }

      public boolean writeMemory(String address, String value, int wordSize) {
         return GDBDSFHelper.writeMemory(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), address, value, wordSize);
      }

      public void writeMemory(String address, String value, int wordSize, final DataRequestMonitor<MIDataWriteMemoryInfo> rm) {
         GDBDSFHelper.writeMemory(DebugContextManager.this.getActiveSession(), DebugContextManager.this.getCommandControl(), address, value, wordSize, rm);
      }
   }
}
