package com.migueldev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

  public static void main(String[] args) {
    System.out.println("=================================================");
    System.out.println("          Java File Renamer Tool                 ");
    System.out.println("=================================================");

    Scanner scanner = new Scanner(System.in);

    // 1. Get and validate Source Folder
    File sourceDir = null;
    while (sourceDir == null) {
      System.out.print("Enter source folder path: ");
      String sourcePath = scanner.nextLine().trim();
      if (sourcePath.isEmpty()) {
        System.out.println(
            "[Error] Source folder path cannot be empty.");
        continue;
      }

      File dir = new File(sourcePath);
      if (!dir.exists()) {
        System.out.println(
            "[Error] Directory does not exist: " + dir.getAbsolutePath());
      } else if (!dir.isDirectory()) {
        System.out.println(
            "[Error] Path is not a directory: " + dir.getAbsolutePath());
      } else {
        sourceDir = dir;
      }
    }

    // 2. Get and validate Output Folder
    File outputDir = null;
    while (outputDir == null) {
      String defaultDestPath = new File(
          sourceDir,
          "renamed").getAbsolutePath();
      System.out.print(
          "Enter output folder path (Press Enter to use default: " +
              defaultDestPath +
              "): ");
      String outputPath = scanner.nextLine().trim();

      File dir;
      if (outputPath.isEmpty()) {
        dir = new File(defaultDestPath);
      } else {
        dir = new File(outputPath);
      }

      if (dir.exists() && !dir.isDirectory()) {
        System.out.println(
            "[Error] Target path exists but is not a directory.");
      } else {
        outputDir = dir;
      }
    }

    // 3. Get naming pattern
    String pattern = "";
    System.out.print(
        "Enter new name pattern (use '#' or '#Number' for file index, e.g., 'new_name_#', extensions are preserved automatically): ");
    pattern = scanner.nextLine().trim();
    if (pattern.isEmpty()) {
      pattern = "#";
    }

    // 4. Get index, default to 0
    int idx = 0;

    // Get base pattern and ensure it has the '#' placeholder
    String basePattern = getBasePattern(pattern);
    if (!basePattern.contains("#")) {
      basePattern += " #";
      System.out.println(
          "-> Notice: Pattern did not contain ' #'. Automatically adjusted to: " +
              basePattern);
    }

    // Expresion regular: busca un '#' seguido de uno o más dígitos
    Pattern pattern2 = Pattern.compile("#(\\d+)");
    Matcher matcher = pattern2.matcher(basePattern);

    if (matcher.find()) {
      String numeroStr = matcher.group(1); // Captura solo los dígitos
      try {
        idx = Integer.parseInt(numeroStr);
        basePattern = basePattern.replaceFirst("#(\\d+)", "#");
      } catch (NumberFormatException e) {
        System.out.println(
            "El número es demasiado grande para un int.");
      }
    }

    // 4. Retrieve and sort files in ascending order alphabetically by filename
    File[] files = sourceDir.listFiles(File::isFile);
    if (files == null || files.length == 0) {
      System.out.println(
          "\n[Error] No files found in the source directory: " +
              sourceDir.getAbsolutePath());
      scanner.close();
      return;
    }

    // Sort alphabetically case-insensitively, fallback to case-sensitive if equal
    Arrays.sort(
        files,
        Comparator.comparing(
            File::getName,
            String.CASE_INSENSITIVE_ORDER).thenComparing(File::getName));

    System.out.println("\nFound " + files.length + " file(s) to process.");
    System.out.println("Source: " + sourceDir.getAbsolutePath());
    System.out.println("Output: " + outputDir.getAbsolutePath());
    System.out.println(
        "Pattern base: " + basePattern + " (original extensions preserved)");
    System.out.print("\nProceed with renaming? (Y/N): ");
    String confirm = scanner.nextLine().trim();
    if (!confirm.equalsIgnoreCase("y") &&
        !confirm.equalsIgnoreCase("yes") &&
        !confirm.equalsIgnoreCase("s") &&
        !confirm.equalsIgnoreCase("si")) {
      System.out.println("Operation cancelled by user.");
      scanner.close();
      return;
    }

    // Create output directory if it doesn't exist
    if (!outputDir.exists()) {
      boolean created = outputDir.mkdirs();
      if (!created) {
        System.out.println(
            "[Error] Failed to create output directory: " +
                outputDir.getAbsolutePath());
        scanner.close();
        return;
      }
    }

    System.out.println(
        "\n========================================================================");
    System.out.printf("%-35s --> %-35s\n", "Original Name", "Renamed Name");
    System.out.println(
        "========================================================================");

    int successCount = 0;
    int failCount = 0;
    final int lengthDigits = Integer.toString(files.length + idx).length();

    for (int i = 0; i < files.length; i++, idx++) {
      final String numberFormat = String.format(
          "%0" + lengthDigits + "d",
          idx);
      File sourceFile = files[i];
      String ext = getFileExtension(sourceFile);
      String newFileName = basePattern.replace("#", numberFormat) + ext;
      File destFile = new File(outputDir, newFileName);

      try {
        // Copy file to avoid data loss
        Files.copy(
            sourceFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        System.out.printf(
            "%-35s --> %-35s [OK]\n",
            sourceFile.getName(),
            newFileName);
        successCount++;
      } catch (IOException e) {
        System.out.printf(
            "%-35s --> %-35s [FAILED: %s]\n",
            sourceFile.getName(),
            newFileName,
            e.getMessage());
        failCount++;
      }
    }

    System.out.println(
        "========================================================================");
    System.out.printf(
        "Summary: %d files successfully renamed/copied, %d failed.\n",
        successCount,
        failCount);
    System.out.println(
        "========================================================================");

    scanner.close();
  }

  private static String getBasePattern(String pattern) {
    int dotIndex = pattern.lastIndexOf('.');
    if (dotIndex != -1 && dotIndex > pattern.lastIndexOf('#')) {
      String possibleExt = pattern.substring(dotIndex);
      if (possibleExt.matches("\\.[a-zA-Z0-9]+")) {
        return pattern.substring(0, dotIndex);
      }
    }
    return pattern;
  }

  private static String getFileExtension(File file) {
    String name = file.getName();
    int dotIndex = name.lastIndexOf('.');
    if (dotIndex != -1 && dotIndex > 0) {
      return name.substring(dotIndex);
    }
    return "";
  }
}
