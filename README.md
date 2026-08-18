# Java File Renamer Tool

A lightweight Java Swing application that copies and renames files from a source directory into an output directory. The files are sorted in ascending order alphabetically prior to renaming.

## Features

- **Graphical User Interface**: Simple Swing-based UI (built with Swing, which ships with the JDK) with folder pickers, a naming-pattern field, and a live preview table.
- **Preview Before Renaming**: Click **Refresh Preview** to see the original name mapped to the new name for every file before anything is copied.
- **Ascending Alphabetical Sorting**: Files are automatically sorted in alphabetical order (case-insensitive) before renaming, ensuring predictable ordering (e.g., `file_b1.txt`, `file_t3.pdf`, `file_u2.png` mapping to `new_name_1.txt`, `new_name_2.pdf`, `new_name_3.png` respectively).
- **Extension Preservation**: The application automatically extracts and preserves the original file extension of each source file.
- **Pattern Auto-formatting**: If you enter an extension in the naming pattern (e.g. `new_name_#.txt`), it will automatically strip it and use the base pattern (e.g. `new_name_#`), ensuring no duplicate extensions occur (e.g. `.txt.txt`).
- **Custom Start Index**: Use `#N` to start the sequence at `N` (e.g. `file_#3` names the first file `file_3`).
- **Safety First**: Files are **copied** rather than moved. This prevents any data loss in case of an incorrect naming pattern or execution error.
- **Background Processing**: Renaming runs on a background thread, keeping the UI responsive, and reports a summary with per-file OK/FAILED status.

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

You can also build a self-contained executable JAR with `mvn package`; the runnable JAR is produced as `renamefile-1.0-jar-with-dependencies.jar` in the `target` folder.

## How to Use

1. **Source folder**: Enter the path of the directory containing the files you wish to rename, or click **Browse...** to pick it.
2. **Output folder**: Enter the path where the renamed files should be saved, or click **Browse...**. By default it is auto-filled with a `renamed` subfolder inside your source directory.
3. **New name pattern**: The template for your new filenames. Use `#` to define where the sequential counter should be placed (or `#N` to start at `N`). The original extension (e.g. `.txt`, `.pdf`) will be appended automatically.
   - *Example*: `new_name_#` or `file_#`.
4. Click **Refresh Preview** to load the file list and see the resulting new names.
5. Click **Rename Files** to copy the files. A summary dialog reports how many files were successfully renamed.

---

## Example

Given a source folder with `file_b1.txt`, `file_t3.pdf`, and `file_u2.png`, and the pattern `new_name_#`, the preview shows:

| Original Name | New Name      |
| ------------- | ------------- |
| file_b1.txt   | new_name_1.txt |
| file_t3.pdf   | new_name_2.pdf |
| file_u2.png   | new_name_3.png |

After clicking **Rename Files**, the three files are copied into the output folder with their new names.