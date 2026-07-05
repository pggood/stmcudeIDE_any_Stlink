package com.st.stm32cube.ide.mcu.debug.stlinkclient;

import com.st.stm32cube.common.logger.MCULoggerPlugin;
import com.st.stm32cube.ide.mcu.debug.MCUDebugPlugin;
import com.st.stm32cube.ide.mcu.debug.stlinkfwutil.StLinkFwUtil;
import com.st.stm32cube.ide.mcu.externaltools.MCUExternalToolsPlugin;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

public class STLinkTcpClient {
   private static final int MAX_CONNECT_ATTEMPT = 20;
   private static int fPort = 7184;
   private static boolean DEBUG;
   private Socket fSocket;
   private PrintStream fOut;
   private BufferedInputStream fBIn;
   private STLinkTcpClient.DeviceInfo[] devices_list;
   private int send_count = 0;

   static {
      DEBUG = MCUDebugPlugin.isDebuggingStatic() && Boolean.parseBoolean((String)Optional.ofNullable(Platform.getDebugOption("com.st.stm32cube.ide.mcu.debug/debug/stlink_client")).orElse(Boolean.FALSE.toString().toLowerCase()));
   }

   public static String getSTLinkServerLocation() {
      String stLinkServerLocation = null;

      try {
         stLinkServerLocation = MCUExternalToolsPlugin.getSTLinkServerLocation();
      } catch (IllegalArgumentException | InterruptedException | URISyntaxException | IOException | OperationCanceledException var2) {
         MCULoggerPlugin.logException(MCUDebugPlugin.getUniqueIdentifier(), var2);
      }

      return stLinkServerLocation;
   }

   public STLinkTcpClient() {
      this.initTcpClient();
   }

   public static boolean launchServer() {
      boolean ret = true;
      String stLinkServerLocation = getSTLinkServerLocation();
      if (stLinkServerLocation == null) {
         return false;
      } else {
         IPath path = new Path(stLinkServerLocation);
         String[] cmdarray = new String[]{path.toOSString(), "-a", "-p" + String.valueOf(fPort)};

         try {
            Process proc = Runtime.getRuntime().exec(cmdarray, (String[])null, new File(System.getProperty("user.home")));
            (new STLinkTcpClient.StreamGobbler(proc.getErrorStream())).start();
            (new STLinkTcpClient.StreamGobbler(proc.getInputStream())).start();
         } catch (IOException var5) {
            MCUDebugPlugin.logException(var5);
            ret = false;
         }

         return ret;
      }
   }

   private boolean initSocket(boolean maskException) {
      boolean ret = false;

      try {
         InetAddress serveur = InetAddress.getByName("localhost");
         this.fSocket = null;
         this.fSocket = new Socket(serveur, fPort);
         this.fSocket.setSoLinger(true, 1);
         this.fSocket.setTcpNoDelay(true);
         ret = true;
      } catch (IOException var4) {
         if (!maskException) {
            MCUDebugPlugin.logException(var4);
         }
      }

      return ret;
   }

   private boolean initReaderAndStreamer() {
      boolean ret = true;

      try {
         this.fBIn = new BufferedInputStream(this.fSocket.getInputStream());
         this.fOut = new PrintStream(this.fSocket.getOutputStream());
         MCULoggerPlugin.debugLog(MCUDebugPlugin.getUniqueIdentifier(), MCUDebugPlugin.isDebuggingStatic(), "com.st.stm32cube.ide.mcu.debug/debug/stlink_client", String.format("init : open socket value : '%s'", this.fSocket.toString()));
      } catch (IOException var3) {
         MCUDebugPlugin.logException(var3);
         ret = false;
      }

      if (this.fBIn == null || this.fOut == null) {
         ret = false;
      }

      return ret;
   }

   public boolean initTcpClient() {
      boolean init = false;
      this.debugLog("init socket");
      if (this.initSocket(true)) {
         init = this.initReaderAndStreamer();
         this.debugLog("init streams returned " + init);
      } else {
         this.debugLog("launching stlink-server");
         if (!launchServer()) {
            this.debugLog("stlink-server launch failed");
            return false;
         }

         for(int connectAttempt = 1; !init && connectAttempt <= 20; ++connectAttempt) {
            try {
               this.debugLog("init socket #" + connectAttempt);
               if (this.initSocket(connectAttempt < 20)) {
                  init = this.initReaderAndStreamer();
                  if (!init && this.fSocket != null) {
                     this.debugLog("init streams failed");
                     this.fSocket.close();
                  }
               }

               if (!init) {
                  this.debugLog("failed, retrying in 100ms");
                  Thread.sleep(100L);
               }
            } catch (Exception var4) {
               MCUDebugPlugin.logException(var4);
            }
         }
      }

      return init;
   }

   private void debugLog(String msg) {
      if (DEBUG) {
         System.out.println("stlink-client: " + msg);
      }

   }

   public void closeClient() {
      if (this.fSocket != null) {
         try {
            this.fBIn.close();
            this.fOut.close();
            this.fSocket.close();
         } catch (IOException var2) {
            MCUDebugPlugin.logException(var2);
         }
      }

   }

   private byte[] open_device(byte[] device_id) {
      byte[] connect_id = null;
      byte[] cmd = new byte[8];
      cmd[0] = 3;
      System.arraycopy(device_id, 0, cmd, 4, device_id.length);
      byte[] ret_buf = this.send_cmd(cmd, 8, 8, true);
      if (ret_buf != null) {
         connect_id = Arrays.copyOfRange(ret_buf, 4, 8);
      }

      return connect_id;
   }

   private boolean close_device(byte[] connect_id) {
      byte[] cmd = new byte[8];
      cmd[0] = 4;
      System.arraycopy(connect_id, 0, cmd, 4, connect_id.length);
      byte[] ret_buf = this.send_cmd(cmd, 8, 4, true);
      return ret_buf != null;
   }

   private STLinkTcpClient.DeviceInfo get_stlink_descriptor(int usb_key) {
      byte[] cmd = new byte[8];
      cmd[0] = 2;
      cmd[1] = (byte)usb_key;
      cmd[4] = 41;
      byte[] ret_buf = this.send_cmd(cmd, 8, 45, true);
      return ret_buf == null ? null : new STLinkTcpClient.DeviceInfo(ret_buf);
   }

   private StLinkFwUtil.StLinkFwInfo get_fw_info(byte[] device_id) {
      byte[] connect_id = this.open_device(device_id);
      if (connect_id == null) {
         return new StLinkFwUtil.StLinkFwInfo(-1, -1);
      } else {
         byte[] mode_buf = this.send_usb_cmd(new byte[]{-11}, 2, connect_id);
         if (mode_buf != null && mode_buf.length == 2 && mode_buf[0] == 0) {
            this.send_usb_cmd(new byte[]{-13, 7}, 0, connect_id);
         }

         byte[] ret_buf = this.send_usb_cmd(new byte[]{-15}, 6, connect_id);
         if (ret_buf == null) {
            this.close_device(connect_id);
            return new StLinkFwUtil.StLinkFwInfo(-1, -1);
         } else {
            byte[] version_buf = new byte[4];
            System.arraycopy(ret_buf, 0, version_buf, 2, 2);
            int version = ByteBuffer.wrap(version_buf).getInt();
            int major = version >> 12 & 15;
            int jtag = version >> 6 & 63;
            if (major >= 3) {
               ret_buf = this.send_usb_cmd(new byte[]{-5}, 12, connect_id);
               if (ret_buf == null) {
                  this.close_device(connect_id);
                  return new StLinkFwUtil.StLinkFwInfo(-1, -1);
               }

               major = ret_buf[0];
               jtag = ret_buf[2];
            }

            this.close_device(connect_id);
            return new StLinkFwUtil.StLinkFwInfo(major, jtag);
         }
      }
   }

   public STLinkTcpClient.DeviceInfo[] get_devices_connected() {
      return this.devices_list;
   }

   public void update_devices_list_connected() {
      int nb_devices = this.get_nb_stlink();
      if (nb_devices != 0) {
         this.devices_list = new STLinkTcpClient.DeviceInfo[nb_devices];

         for(int i = 0; i < nb_devices; ++i) {
            STLinkTcpClient.DeviceInfo device = this.get_stlink_descriptor(i);
            if (device != null) {
               StLinkFwUtil.StLinkFwInfo fwInfo = this.get_fw_info(device.device_id);
               device.set_fwInfo(fwInfo);
               this.devices_list[i] = device;
            }
         }

      }
   }

   public List<StLinkFwUtil.StLinkInfo> get_connected() {
      List<StLinkFwUtil.StLinkInfo> connected = new ArrayList();
      int n = this.get_nb_stlink();

      for(int i = 0; i < n; ++i) {
         if (this.devices_list != null && this.devices_list.length == n) {
            StLinkFwUtil.StLinkFwInfo fwInfo = this.devices_list[i].get_fwInfo();
            String serial = this.devices_list[i].getSerialId();
            short vendorId = this.devices_list[i].getVendorId();
            short productId = this.devices_list[i].getProductId();
            if (serial != null && fwInfo != null) {
               connected.add(new StLinkFwUtil.StLinkInfo(serial, fwInfo, vendorId, productId));
            }
         }
      }

      return connected;
   }

   public int get_nb_stlink() {
      byte[] ret_buf = this.send_cmd(new byte[]{1}, 1, 4, false);
      return ret_buf != null ? ByteBuffer.wrap(ret_buf, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() : 0;
   }

   public boolean refresh() {
      byte[] ret_buf = this.send_cmd(new byte[2], 2, 4, true);
      if (ret_buf != null) {
         this.update_devices_list_connected();
      }

      return ret_buf != null;
   }

   public boolean blink(int usb_key) {
      boolean ret = false;
      byte[] connect_id = this.open_device(this.devices_list[usb_key].device_id);
      if (connect_id != null) {
         byte[] ret_buf = this.send_usb_cmd(new byte[]{-14, 73}, 2, connect_id);
         this.close_device(connect_id);
         if (ret_buf != null) {
            ret = ret_buf[0] == 128;
         }
      }

      return ret;
   }

   private byte[] send_cmd(byte[] cmd_buf, int cmd_len, int recv_len, boolean check_tcp_status) {
      ++this.send_count;
      this.debugLog(NLS.bind("send_cmd#{0}: cmd_buf={1} (len={2}) expect_len={3} check_tcp_status={4}", new Object[]{this.send_count, Arrays.toString(cmd_buf), cmd_len, recv_len, check_tcp_status}));

      try {
         if (this.fOut != null) {
            this.fOut.write(cmd_buf, 0, cmd_len);
            if (this.fBIn != null) {
               int byteRead = true;
               byte[] recv_buf = new byte[recv_len];
               int byteRead = this.fBIn.read(recv_buf, 0, recv_len);
               this.debugLog(NLS.bind("send_cmd#{0}: received_bytes={1} recv_buf={2}", new Object[]{this.send_count, byteRead, Arrays.toString(recv_buf)}));
               if (byteRead != recv_len) {
                  this.debugLog(NLS.bind("send_cmd#{0}: command failed, expect != current", new Object[]{this.send_count}));
                  return null;
               }

               if (check_tcp_status) {
                  ByteBuffer bb = ByteBuffer.wrap(recv_buf);
                  byte[] status_buf = new byte[4];
                  bb.get(status_buf, 0, 4);
                  int status = ByteBuffer.wrap(status_buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
                  if (status != 1) {
                     this.debugLog(NLS.bind("send_cmd#{0}: command failed", new Object[]{this.send_count}));
                     return null;
                  }

                  this.debugLog(NLS.bind("send_cmd#{0}: command success", new Object[]{this.send_count}));
               }

               return recv_buf;
            }

            this.debugLog(NLS.bind("send_cmd#{0}: fBin is null!", this.send_count));
         } else {
            this.debugLog(NLS.bind("send_cmd#{0}: fOut is null!", this.send_count));
         }
      } catch (Exception var10) {
         MCUDebugPlugin.logException(var10);
      }

      this.debugLog(NLS.bind("send_cmd#{0}: got exception, return null", this.send_count));
      return null;
   }

   private byte[] send_usb_cmd(byte[] usb_cmd, int recv_len, byte[] connect_id) {
      byte[] tcp_cmd = new byte[32];
      tcp_cmd[0] = 5;
      System.arraycopy(connect_id, 0, tcp_cmd, 4, 4);
      System.arraycopy(usb_cmd, 0, tcp_cmd, 8, usb_cmd.length);
      tcp_cmd[24] = 1;
      System.arraycopy(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(recv_len).array(), 0, tcp_cmd, 28, 4);
      byte[] ret_buf = this.send_cmd(tcp_cmd, tcp_cmd.length, recv_len + 4, true);
      return ret_buf != null && ret_buf.length > 4 ? Arrays.copyOfRange(ret_buf, 4, ret_buf.length) : null;
   }

   public static class DeviceInfo {
      private byte[] device_id = null;
      private byte[] serial_id = null;
      private byte[] vendor_id = null;
      private byte[] product_id = null;
      private byte used;
      private StLinkFwUtil.StLinkFwInfo fwInfo = null;

      public DeviceInfo(byte[] infos) {
         this.device_id = Arrays.copyOfRange(infos, 4, 8);
         this.serial_id = this.getSerialId(Arrays.copyOfRange(infos, 8, 40));
         this.vendor_id = Arrays.copyOfRange(infos, 40, 42);
         this.product_id = Arrays.copyOfRange(infos, 42, 44);
         this.used = infos[44];
      }

      private byte[] getSerialId(byte[] buf) {
         int index = 0;

         for(int i = 0; i < buf.length; ++i) {
            if (buf[i] == 0) {
               index = i;
               break;
            }
         }

         return Arrays.copyOfRange(buf, 0, index);
      }

      private String le_to_hexStr(byte[] b) {
         StringBuffer sb = new StringBuffer(b.length * 2);

         for(int i = b.length - 1; i >= 0; --i) {
            int v = b[i] & 255;
            if (v < 16) {
               sb.append('0');
            }

            sb.append(Integer.toHexString(v));
         }

         return sb.toString().toUpperCase();
      }

      public String getDeviceId() {
         return this.le_to_hexStr(this.device_id);
      }

      public String getSerialId() {
         return new String(this.serial_id);
      }

      public boolean isUsed() {
         return this.used != 0;
      }

      public boolean isBlinkable() {
         if (this.fwInfo == null) {
            return false;
         } else {
            return this.fwInfo.major == 2 && this.fwInfo.jtag >= 28 || this.fwInfo.major >= 3;
         }
      }

      public StLinkFwUtil.StLinkFwInfo get_fwInfo() {
         return this.fwInfo;
      }

      public void set_fwInfo(StLinkFwUtil.StLinkFwInfo fwInfo) {
         this.fwInfo = fwInfo;
      }

      public short getVendorId() {
         return ByteBuffer.wrap(this.vendor_id).order(ByteOrder.LITTLE_ENDIAN).getShort();
      }

      public short getProductId() {
         return ByteBuffer.wrap(this.product_id).order(ByteOrder.LITTLE_ENDIAN).getShort();
      }
   }

   static class StreamGobbler extends Thread {
      InputStream is;

      StreamGobbler(InputStream is) {
         this.is = is;
      }

      public void run() {
         try {
            InputStreamReader isr = new InputStreamReader(this.is);
            BufferedReader br = new BufferedReader(isr);

            while(br.readLine() != null) {
            }
         } catch (IOException var3) {
            var3.printStackTrace();
         }

      }
   }
}
