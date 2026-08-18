package com.migueldev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRenamer {

    public record RenameItem(
        String originalName,
        String newFileName,
        String status
    ) {}

    public record RenameResult(
        int successCount,
        int failCount,
        List<RenameItem> items,
        List<String> errors
    ) {}

    public List<RenameItem> preview(File sourceDir, String pattern) {
        List<RenameItem> items = new ArrayList<>();
        File[] files = listSortedFiles(sourceDir);
        if (files == null || files.length == 0) {
            return items;
        }

        PatternInfo info = normalizePattern(pattern);
        int startIndex = info.startIndex;
        int lengthDigits = Integer.toString(files.length + startIndex).length();
        for (File file : files) {
            String numberFormat = String.format(
                "%0" + lengthDigits + "d",
                startIndex++
            );
            String newFileName =
                info.basePattern.replace("#", numberFormat) +
                getFileExtension(file);
            items.add(new RenameItem(file.getName(), newFileName, ""));
        }
        return items;
    }

    public RenameResult execute(
        File sourceDir,
        File outputDir,
        String pattern
    ) {
        File[] files = listSortedFiles(sourceDir);
        if (files == null || files.length == 0) {
            return new RenameResult(
                0,
                0,
                List.of(),
                List.of("No files found in the source directory.")
            );
        }

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            return new RenameResult(
                0,
                0,
                List.of(),
                List.of(
                    "Failed to create output directory: " +
                        outputDir.getAbsolutePath()
                )
            );
        }

        int successCount = 0;
        int failCount = 0;
        List<RenameItem> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        PatternInfo info = normalizePattern(pattern);
        int startIndex = info.startIndex;
        int lengthDigits = Integer.toString(files.length + startIndex).length();
        for (File file : files) {
            String numberFormat = String.format(
                "%0" + lengthDigits + "d",
                startIndex++
            );
            String newFileName =
                info.basePattern.replace("#", numberFormat) +
                getFileExtension(file);
            File destFile = new File(outputDir, newFileName);

            try {
                Files.copy(
                    file.toPath(),
                    destFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                );
                successCount++;
                items.add(new RenameItem(file.getName(), newFileName, "OK"));
            } catch (IOException e) {
                failCount++;
                items.add(
                    new RenameItem(file.getName(), newFileName, "FAILED")
                );
                errors.add(
                    file.getName() +
                        " -> " +
                        newFileName +
                        " [FAILED: " +
                        e.getMessage() +
                        "]"
                );
            }
        }

        return new RenameResult(successCount, failCount, items, errors);
    }

    private record PatternInfo(String basePattern, int startIndex) {}

    private static PatternInfo normalizePattern(String pattern) {
        int startIndex = 0;
        String basePattern = getBasePattern(pattern);
        Matcher matcher = Pattern.compile("#(\\d+)").matcher(basePattern);
        if (matcher.find()) {
            try {
                startIndex = Integer.parseInt(matcher.group(1));
                basePattern = basePattern.replaceFirst("#(\\d+)", "#");
            } catch (NumberFormatException e) {
                startIndex = 0;
            }
        }
        if (!basePattern.contains("#")) {
            basePattern += " #";
        }
        return new PatternInfo(basePattern, startIndex);
    }

    private File[] listSortedFiles(File sourceDir) {
        File[] files = sourceDir.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return files;
        }
        Arrays.sort(
            files,
            Comparator.comparing(
                File::getName,
                String.CASE_INSENSITIVE_ORDER
            ).thenComparing(File::getName)
        );
        return files;
    }

    public static String getBasePattern(String pattern) {
        int dotIndex = pattern.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex > pattern.lastIndexOf('#')) {
            String possibleExt = pattern.substring(dotIndex);
            if (possibleExt.matches("\\.[a-zA-Z0-9]+")) {
                return pattern.substring(0, dotIndex);
            }
        }
        return pattern;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex > 0) {
            return name.substring(dotIndex);
        }
        return "";
    }
}
