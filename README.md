## STM32CubeIDE ST-LINK Detection Patch

## Overview

This patch addresses a critical issue in STM32CubeIDE where the IDE incorrectly reports "ST-LINK not installed" and exits, even when a genuine ST-LINK debugger is connected. This occurs because the IDE's detection mechanism is overly aggressive and unreliable, often misidentifying legitimate ST-LINK devices as third-party or unsupported hardware.

## The Problem

STM32CubeIDE (versions up to at least 1.18.1) contains a flawed hardware detection routine that:

- Actively checks for specific Nucleo board signatures
- Exits the application if it determines the connected ST-LINK is not "genuine"
- Frequently misidentifies genuine ST-LINK devices, especially:
  - Standalone ST-LINK/V2 programmers
  - Custom boards with integrated ST-LINK
  - Development boards with slightly different firmware versions
  - Even official Nucleo boards under certain conditions

This detection code provides no benefit to the end-user and prevents legitimate hardware from being used with the IDE.

## The Solution

This patch *removes the problematic ST-LINK detection logic*, allowing the IDE to work with any ST-LINK compatible debugger without artificially blocking connections.

### What the Patch Does

1. **Compiles modified versions** of two critical Java classes:
   - `StLinkDebugHardware.java` - Contains the main detection logic
   - `StLinkFwUtil.java` - Contains the firmware validation utilities

2. **Creates patched JAR files** that replace the originals in your STM32CubeIDE installation

3. **Removes the faulty detection code** so the IDE will no longer:
   - Check for specific Nucleo board signatures
   - Block connections based on device identification
   - Exit unexpectedly with "ST-LINK not installed" errors

## Installation

### Prerequisites

- Java Development Kit (JDK) installed
- STM32CubeIDE 1.18.1 (compatible with other versions with path adjustments)
- Administrative/Write access to your STM32CubeIDE installation directory

### Automated Build

1. **Configure the build script** (`compile_stlink.cmd`):

```batch
set PLUGIN_DIR=D:\ST\STM32CubeIDE_1.18.1\STM32CubeIDE\plugins
set SOURCE_STLINK=D:\com.st.stm32cube.ide.mcu.debug.stlink
set SOURCE_DEBUG=D:\com.st.stm32cube.ide.mcu.debug
```

   Update these paths to match your STM32CubeIDE installation.

2. **Run the compilation script**:

```batch
compile_stlink.cmd
```

3. **The script will create**:
   - Patched JAR files with today's date in the filename
   - Compiled class files in `D:\bin` (configurable)

### Manual Installation

1. **Backup original JAR files** from your `plugins` directory:
   - `com.st.stm32cube.ide.mcu.debug.stlink.*.jar`
   - `com.st.stm32cube.ide.mcu.debug.*.jar`

2. **Copy the generated JAR files** to your `plugins` directory:
   - `stlink_modified_YYYYMMDD.jar`
   - `debug_modified_YYYYMMDD.jar`

3. **Restart STM32CubeIDE**

### Command Line Installation (Alternative)

If you prefer command-line operations:

```batch
rem Backup originals
copy "%PLUGIN_DIR%\com.st.stm32cube.ide.mcu.debug.stlink.*.jar" "%PLUGIN_DIJ%\backup\"
copy "%PLUGIN_DIR%\com.st.stm32cube.ide.mcu.debug.*.jar" "%PLUGIN_DIR%\backup\"
@
# Copy modified JARs
copy "%JAR_NAME_STLINK%" "%PLUGIN_DIR%\"
copy "%JAR_NAME_DEBUG%" "%PLUGIN_DIR%\"
```

## Verification

After installation, verify the patch worked:

1. Launch STM32CubeIDE
2. Connect your ST-LINK debugger
3. Create or open a project
4. Attempt to debug - the IDE should now connect without "not installed" errors

## Technical Details

### Modified Files

- **StLinkDebugHardware.java**: Contains the main detection routine that:
   - Originally checked board types and firmware signatures
   - Now allows all ST-LINK devices to connect without validation

- **StLinkFwUtil.java**: Handles firmware utilities that:
   - Originally validated firmware authenticity
   - Now bypasses these checks

### Build Process

The compilation script:

1. Gathers all plugin JARs and directories for the classpath
2. Compiles the modified Java source files
3. Copies compiled classes back to the source directories
4. Packages everything into new JAR files

### Compatibility

| Version | Status |
|---------|-------|
| 1.18.1  | ✅ Tested |
| 1.16.0 - 1.18.x | ✅ Should work |
| Other versions | ❤️ May work with path adjustments |

## Troubleshooting

### Compilation Errors

If `javc` cannot find classes, ensure:

- Your JDK version is compatible (Java 8 or later)
- The PLUGIN_DIR path contains all required JARs
- The source directories contain the complete plugin structure

### Installation Issues

If the IDE still shows errors:

1. Verify JAR files were copied to the correct plugins directory
2. Check file permissions on the plugins directory
3. Try clearing the IDE's cache (delete `.metadata` in workspace)
4. Confirm no other versions of the JAR files exist in plugins

### Restoring Original Functionality

To revert to the original IDE behavior:

1. Delete or rename the modified JAR files
2. Restore the original JAR files from backup
3. Restart STM32CubeIDE

## File Structure

```
.
├── compile_stlink.cmd          # Main build script
∔ ├── README.md                 # This file
├── src/
    └── com/
    ∔— st/
    ∔——    └── stm32cube/
    ∔——        └── ide/
    ∔——             └── mcu/
    ∔——                 └── debug/
    ∔—                    └── stlink/
    ∔——                       └── StLinkDebugHardware.java
    ∔——                    └── stlinkfwutil/
        ———                      └── StLinkFwUtil.java
└── (modified source files)
```

## Common Error Messages

| Error Message | Cause | Solution |
|---------------|--------|---------|
| "ST-LINK not installed" | Detection failure | Apply this patch |
| "javac: command not found" | JDK not installed or not in PATH | Install JDK and add to PATH |
| "ClassNotFoundException" | Incorrect classpath | Verify PLUGIN_DIR path |
| "Access denied" | Insufficient permissions | Run as administrator |

## FAQ

**Q: Will this patch void my warranty?**
A: This modifies software only and doesn't affect hardware warranties.

**Q: Does this work with ST-LIJK/V3?**A8: Yes, the patch removes all detection checks and should work with all ST-LINK variants.

**Q: Can I use this with other STM32CubeIDE versions?**A: The patch is designed for 1.18.1 but may work with other versions with minor adjustments.

**Q: Is this legal?**A: This modifies software you have licensed. It's for personal use and doesn't redistribute proprietary code.

## Disclaimer

> ⟠️ WARNING: This patch modifies core STM32CubeIDE functionality. While it resolves the detection issue, it removes safeguards that STLicroelectronics implemented. Use at your own risk. Always backup your original files before applying this patch.

## Contributing

This is a community-driven patch. If you encounter issues or have improvements:

1. Fork the repository
2. Make your changes
3. Submit a pull request

### Guidelines

- Ensure compatibility with multiple STM32CubeIDE versions
- Document any changes thoroughly
- Test with different ST-LINK hardware variants

## License

This patch is provided as-is under the MIT License. STM32CubeIDE remains the property of STMicroelectronics.

```
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FER A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

## Changelog

### Version 1.0.0 (2026-07-05)
- Initial release
- Support for STM32CubeIDE 1.18.1
- Removed Nucleo board detection checks
- Bypassed firmware validation routines

## Acknowledgments

- Thanks to the STM32 community for identifying this issue
- Contributors who tested the patch on various hardware configurations

## Support

For issues or questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Join the STM32 community forums for additional help

---

*\Made with ❥'📣 for the STM32 community*
