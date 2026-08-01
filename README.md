# Java File Renamer Tool

A lightweight, interactive Java console application designed to copy and rename files from a source directory into an output directory. The files are sorted in ascending order alphabetically prior to renaming.

## Features

- **Interactive CLI**: Easy-to-use prompts for configuring the source directory, output directory, and naming pattern.
- **Ascending Alphabetical Sorting**: Files are automatically sorted in alphabetical order (case-insensitive) before renaming, ensuring predictable ordering (e.g., `file_b1.txt`, `file_t3.pdf`, `file_u2.png` mapping to `new_name_1.txt`, `new_name_2.pdf`, `new_name_3.png` respectively).
- **Extension Preservation**: The application automatically extracts and preserves the original file extension of each source file.
- **Pattern Auto-formatting**: If you enter an extension in the naming pattern (e.g. `new_name_#.txt`), it will automatically strip it and use the base pattern (e.g. `new_name_#`), ensuring no duplicate extensions occur (e.g. `.txt.txt`).
- **Safety First**: Files are **copied** rather than moved. This prevents any data loss in case of an incorrect naming pattern or execution error.
- **Clean Execution Table**: Outputs a tabular summary showing the original filename, the renamed filename, and the status of each operation.

## Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher.
- **Apache Maven**: Installed and added to your system's PATH.

## Getting Started

### 1. Build the Application

From the root directory of the project, compile the application using Maven:

```bash
mvn compile
```

### 2. Run the Application

Execute the application with the following command:

```bash
mvn exec:java -Dexec.mainClass="com.migueldev.Main"
```

## How to Use

When running the application, you will be prompted for three inputs:

1. **Source Folder Path**: The absolute path of the directory containing the files you wish to rename.
2. **Output Folder Path**: The absolute path where the renamed files should be saved.
   - *Tip*: You can press **Enter** to accept the default folder (which creates a subfolder named `renamed` inside your source directory).
3. **New Name Pattern**: The template for your new filenames. Use `#` to define where the sequential 1-based counter should be placed.
   - *Example*: `new_name_#` or `file_#`. The original extension (e.g. `.txt`, `.pdf`) will be appended automatically.

Once configured, the application displays a confirmation prompt showing the file count, source/destination directories, and base pattern. Press `Y` to proceed.

---

## Example Run

```text
=================================================
          Java File Renamer Tool                 
=================================================
Enter source folder path: C:\Users\User\Documents\MyFolder
Enter output folder path (Press Enter to use default: C:\Users\User\Documents\MyFolder\renamed): 
Enter new name pattern (use '#' for file index, e.g., 'new_name_#', extensions are preserved automatically): new_name_#

Found 3 file(s) to process.
Source: C:\Users\User\Documents\MyFolder
Output: C:\Users\User\Documents\MyFolder\renamed
Pattern base: new_name_# (original extensions preserved)

Proceed with renaming? (Y/N): y

========================================================================
Original Name                       --> Renamed Name                       
========================================================================
file_b1.txt                         --> new_name_1.txt                      [OK]
file_t3.pdf                         --> new_name_2.pdf                      [OK]
file_u2.png                         --> new_name_3.png                      [OK]
========================================================================
Summary: 3 files successfully renamed/copied, 0 failed.
========================================================================
```

