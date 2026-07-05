package com.st.stm32cube.ide.mcu.debug.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarAssignInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarCreateInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarDeleteInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarEvaluateExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoPathExpressionInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarListChildrenInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarSetFormatInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarShowAttributesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarUpdateInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.model.MemoryByte;

public class GDBDSFHelper {
   public static MIVarCreateInfo createVar(DsfSession session, final IMICommandControl commandControlService, IExpressions expressionService, String expression) {
      if (session != null && commandControlService != null && expressionService != null && expression != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            IExpressionDMContext iexprDmContext = expressionService.createExpression(commandControlService.getContext(), expression);
            final ICommand<MIVarCreateInfo> commandCreate = comFac.createMIVarCreate(iexprDmContext, expression);
            Query evalExprQuery = new Query<MIVarCreateInfo>() {
               protected void execute(DataRequestMonitor<MIVarCreateInfo> rm) {
                  commandControlService.queueCommand(commandCreate, rm);
               }
            };

            try {
               executor.execute(evalExprQuery);
               MIVarCreateInfo varInfo = (MIVarCreateInfo)evalExprQuery.get();
               if (varInfo != null) {
                  if (!varInfo.isError()) {
                     return varInfo;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var10) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarShowAttributesInfo showVarAttributes(DsfSession session, final IMICommandControl commandControlService, String varName) {
      if (session != null && commandControlService != null && varName != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarShowAttributesInfo> command = comFac.createMIVarShowAttributes(commandControlService.getContext(), varName);
            Query query = new Query<MIVarShowAttributesInfo>() {
               protected void execute(DataRequestMonitor<MIVarShowAttributesInfo> rm) {
                  commandControlService.queueCommand(command, rm);
               }
            };

            try {
               executor.execute(query);
               MIVarShowAttributesInfo info = (MIVarShowAttributesInfo)query.get();
               if (info != null) {
                  if (!info.isError()) {
                     return info;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var8) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarAssignInfo assignVar(DsfSession session, final IMICommandControl commandControlService, String varName, String expression) {
      if (session != null && commandControlService != null && varName != null && expression != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarAssignInfo> command = comFac.createMIVarAssign(commandControlService.getContext(), varName, expression);
            Query query = new Query<MIVarAssignInfo>() {
               protected void execute(DataRequestMonitor<MIVarAssignInfo> rm) {
                  commandControlService.queueCommand(command, rm);
               }
            };

            try {
               executor.execute(query);
               MIVarAssignInfo info = (MIVarAssignInfo)query.get();
               if (info != null) {
                  if (!info.isError()) {
                     return info;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var9) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarDeleteInfo deleteVar(DsfSession session, final IMICommandControl commandControlService, String varName) {
      if (session != null && commandControlService != null && varName != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarDeleteInfo> commandDelete = comFac.createMIVarDelete(commandControlService.getContext(), varName);
            Query varDelQuery = new Query<MIVarDeleteInfo>() {
               protected void execute(DataRequestMonitor<MIVarDeleteInfo> rm) {
                  commandControlService.queueCommand(commandDelete, rm);
               }
            };

            try {
               executor.execute(varDelQuery);
               MIVarDeleteInfo varInfo = (MIVarDeleteInfo)varDelQuery.get();
               if (varInfo != null) {
                  if (!varInfo.isError()) {
                     return varInfo;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var8) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarListChildrenInfo listVarChildren(DsfSession session, final IMICommandControl commandControlService, String varName, int from, int to) {
      if (session != null && commandControlService != null && varName != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand commandVarListChild;
            if (from == 0 && to == 0) {
               commandVarListChild = comFac.createMIVarListChildren(commandControlService.getContext(), varName);
            } else {
               commandVarListChild = comFac.createMIVarListChildren(commandControlService.getContext(), varName, from, to);
            }

            Query listChildQuery = new Query<MIVarListChildrenInfo>() {
               protected void execute(DataRequestMonitor<MIVarListChildrenInfo> rm) {
                  commandControlService.queueCommand(commandVarListChild, rm);
               }
            };

            try {
               executor.execute(listChildQuery);
               MIVarListChildrenInfo varInfo = (MIVarListChildrenInfo)listChildQuery.get();
               if (varInfo != null) {
                  if (!varInfo.isError()) {
                     return varInfo;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var10) {
               return null;
            }
         }
      }

      return null;
   }

   public static String evaluateVarExpression(DsfSession session, final IMICommandControl commandControlService, String expression) {
      if (session != null && commandControlService != null && expression != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarEvaluateExpressionInfo> commandEval = comFac.createMIVarEvaluateExpression(commandControlService.getContext(), expression);
            Query evalExprQuery = new Query<MIVarEvaluateExpressionInfo>() {
               protected void execute(DataRequestMonitor<MIVarEvaluateExpressionInfo> rm) {
                  commandControlService.queueCommand(commandEval, rm);
               }
            };

            try {
               executor.execute(evalExprQuery);
               MIVarEvaluateExpressionInfo exprInfo = (MIVarEvaluateExpressionInfo)evalExprQuery.get();
               if (exprInfo != null) {
                  if (!exprInfo.isError()) {
                     String retVal = exprInfo.getValue();
                     if (retVal != null) {
                        return retVal;
                     }

                     return null;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var9) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarInfoPathExpressionInfo infoVarPathExpression(DsfSession session, final IMICommandControl commandControlService, String varName) {
      if (session != null && commandControlService != null && varName != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            if (comFac != null) {
               final ICommand<MIVarInfoPathExpressionInfo> commandVarInfoPath = comFac.createMIVarInfoPathExpression(commandControlService.getContext(), varName);
               Query pathInfoQuery = new Query<MIVarInfoPathExpressionInfo>() {
                  protected void execute(DataRequestMonitor<MIVarInfoPathExpressionInfo> rm) {
                     commandControlService.queueCommand(commandVarInfoPath, rm);
                  }
               };

               try {
                  executor.execute(pathInfoQuery);
                  MIVarInfoPathExpressionInfo pathInfo = (MIVarInfoPathExpressionInfo)pathInfoQuery.get();
                  if (pathInfo != null) {
                     if (!pathInfo.isError()) {
                        return pathInfo;
                     }

                     return null;
                  }

                  return null;
               } catch (InterruptedException | ExecutionException | RejectedExecutionException var8) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   public static String evaluateExpression(DsfSession session, final IMICommandControl commandControlService, String expression) {
      if (session != null && commandControlService != null && expression != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIDataEvaluateExpressionInfo> commandEval = comFac.createMIDataEvaluateExpression(commandControlService.getContext(), expression);
            Query evalExprQuery = new Query<MIDataEvaluateExpressionInfo>() {
               protected void execute(DataRequestMonitor<MIDataEvaluateExpressionInfo> rm) {
                  commandControlService.queueCommand(commandEval, rm);
               }
            };

            try {
               executor.execute(evalExprQuery);
               MIDataEvaluateExpressionInfo exprInfo = (MIDataEvaluateExpressionInfo)evalExprQuery.get();
               if (exprInfo != null) {
                  if (!exprInfo.isError()) {
                     String retVal = exprInfo.getValue();
                     if (retVal != null) {
                        return retVal;
                     }

                     return null;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var9) {
               return null;
            }
         }
      }

      return null;
   }

   public static void evaluateExpression(DsfSession session, final IMICommandControl commandControlService, String expression, final DataRequestMonitor<MIDataEvaluateExpressionInfo> rm) {
      if (session != null && commandControlService != null && expression != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand command = comFac.createMIDataEvaluateExpression(commandControlService.getContext(), expression);

            try {
               executor.execute(new Runnable() {
                  public void run() {
                     commandControlService.queueCommand(command, rm);
                  }
               });
            } catch (RejectedExecutionException var8) {
               return;
            }
         }
      }

   }

   public static MIVarUpdateInfo updateVar(DsfSession session, final IMICommandControl commandControlService, String varName) {
      if (session != null && commandControlService != null && varName != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarUpdateInfo> commandVarUpdate = comFac.createMIVarUpdate(commandControlService.getContext(), varName);
            Query varUpdateQuery = new Query<MIVarUpdateInfo>() {
               protected void execute(DataRequestMonitor<MIVarUpdateInfo> rm) {
                  commandControlService.queueCommand(commandVarUpdate, rm);
               }
            };

            try {
               executor.execute(varUpdateQuery);
               MIVarUpdateInfo updateInfo = (MIVarUpdateInfo)varUpdateQuery.get();
               if (updateInfo != null) {
                  if (!updateInfo.isError()) {
                     return updateInfo;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var8) {
               return null;
            }
         }
      }

      return null;
   }

   public static MIVarSetFormatInfo setFormatVar(DsfSession session, final IMICommandControl commandControlService, String varName, String fmt) {
      if (session != null && commandControlService != null && varName != null && fmt != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIVarSetFormatInfo> commandCreate = comFac.createMIVarSetFormat(commandControlService.getContext(), varName, fmt);
            Query setFormatQuery = new Query<MIVarSetFormatInfo>() {
               protected void execute(DataRequestMonitor<MIVarSetFormatInfo> rm) {
                  commandControlService.queueCommand(commandCreate, rm);
               }
            };

            try {
               executor.execute(setFormatQuery);
               MIVarSetFormatInfo formatInfo = (MIVarSetFormatInfo)setFormatQuery.get();
               if (formatInfo != null) {
                  if (!formatInfo.isError()) {
                     return formatInfo;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var9) {
               return null;
            }
         }
      }

      return null;
   }

   public static MemoryByte[] readMemory(DsfSession session, final IMICommandControl commandControlService, String address, int nrBytes) {
      if (session != null && commandControlService != null && address != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIDataReadMemoryInfo> commandRead = comFac.createMIDataReadMemory(commandControlService.getContext(), 0L, address, 0, 1, 1, nrBytes, 'y');
            Query readMemQuery = new Query<MIDataReadMemoryInfo>() {
               protected void execute(DataRequestMonitor<MIDataReadMemoryInfo> rm) {
                  commandControlService.queueCommand(commandRead, rm);
               }
            };

            try {
               executor.execute(readMemQuery);
               MIDataReadMemoryInfo dataReadInfo = (MIDataReadMemoryInfo)readMemQuery.get();
               if (dataReadInfo != null) {
                  if (!dataReadInfo.isError()) {
                     MemoryByte[] miMem = dataReadInfo.getMIMemoryBlock();
                     if (miMem.length == nrBytes) {
                        return miMem;
                     }

                     return null;
                  }

                  return null;
               }

               return null;
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var10) {
               return null;
            }
         }
      }

      return null;
   }

   public static void readMemory(DsfSession session, final IMICommandControl commandControlService, String address, int nrBytes, final DataRequestMonitor<MIDataReadMemoryInfo> rm) {
      if (session != null && commandControlService != null && address != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand command = comFac.createMIDataReadMemory(commandControlService.getContext(), 0L, address, 0, 1, 1, nrBytes, 'y');

            try {
               executor.execute(new Runnable() {
                  public void run() {
                     commandControlService.queueCommand(command, rm);
                  }
               });
            } catch (RejectedExecutionException var9) {
               return;
            }
         }
      }

   }

   public static boolean writeMemory(DsfSession session, final IMICommandControl commandControlService, String address, String value, int wordSize) {
      boolean ret = false;
      if (session != null && commandControlService != null && address != null && value != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand<MIDataWriteMemoryInfo> command = comFac.createMIDataWriteMemory(commandControlService.getContext(), 0L, address, 0, wordSize, value);
            Query writeMemQuery = new Query<MIDataWriteMemoryInfo>() {
               protected void execute(DataRequestMonitor<MIDataWriteMemoryInfo> rm) {
                  commandControlService.queueCommand(command, rm);
               }
            };

            try {
               executor.execute(writeMemQuery);
               MIDataWriteMemoryInfo dataWriteInfo = (MIDataWriteMemoryInfo)writeMemQuery.get();
               if (dataWriteInfo != null && !dataWriteInfo.isError()) {
                  ret = true;
               }
            } catch (InterruptedException | ExecutionException | RejectedExecutionException var11) {
            }
         }
      }

      return ret;
   }

   public static void writeMemory(DsfSession session, final IMICommandControl commandControlService, String address, String value, int wordSize, final DataRequestMonitor<MIDataWriteMemoryInfo> rm) {
      if (session != null && commandControlService != null && address != null && value != null) {
         DsfExecutor executor = session.getExecutor();
         if (executor != null) {
            CommandFactory comFac = commandControlService.getCommandFactory();
            final ICommand command = comFac.createMIDataWriteMemory(commandControlService.getContext(), 0L, address, 0, wordSize, value);

            try {
               executor.execute(new Runnable() {
                  public void run() {
                     commandControlService.queueCommand(command, rm);
                  }
               });
            } catch (RejectedExecutionException var10) {
               return;
            }
         }
      }

   }
}
