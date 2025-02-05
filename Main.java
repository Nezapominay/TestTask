/*
Задача утилиты записать разные типы данных в разные файлы. Целые числа в один
выходной файл, вещественные в другой, строки в третий.
По умолчанию файлы с результатами располагаются в текущей папке с именами integers.txt, floats.txt, strings.txt
Дополнительно с помощью опции -o нужно уметь задавать путь для результатов. Опция -p
задает префикс имен выходных файлов. Например -o /some/path -p result_ задают вывод в
файлы /some/path/result_integers.txt, /some/path/result_strings.txt и тд.
По умолчанию файлы результатов перезаписываются. С помощью опции -a можно задать
режим добавления в существующие файлы.
В процессе фильтрации данных необходимо собирать статистику по каждому типу данных.
Статистика должна поддерживаться двух видов: краткая и полная. Выбор статистики
производится опциями -s и -f соответственно. Краткая статистика содержит только
количество элементов записанных в исходящие файлы. Полная статистика для чисел
дополнительно содержит минимальное и максимальное значения, сумма и среднее.
Полная статистика для строк, помимо их количества, содержит также размер самой
короткой строки и самой длинной.


Инструкция по запуску:
Через консоль откомпилировать программу
javac ..\path\Main.java

Существующие флаги:
-o - флаг для указания пути сохранения файла (по умолчанию базовая директория)
-p - флаг для указания префикса, после флага необходимо в кавычках
     написать название префикса (пример: -p "out-")
-a - флаг для дополнения записи в файл (по умолчанию файл перезаписывается)
-s - флаг для вывода краткой статистики в консоль
-f - флаг для вывода полной статистики в консоль

Запустить программу с необходимыми флагами, например:
java Main -o .\output -p "out_" -s in1.txt in2.txt


Версия jdk-23.0.2
 */




import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> inputFiles = new ArrayList<>();
        String outputPath = ".";
        String prefix = "";
        boolean appendMode = false;
        boolean fullStats = false;
        boolean shortStats = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) outputPath = args[++i];
                    break;
                case "-p":
                    if (i + 1 < args.length) prefix = args[++i];
                    break;
                case "-a":
                    appendMode = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                default:
                    inputFiles.add(args[i]);
                    break;
            }
        }

        if (inputFiles.isEmpty()) {
            System.out.println("Ошибка: Не переданы входные файлы.");
            return;
        }

        processFiles(inputFiles, outputPath, prefix, appendMode, shortStats, fullStats);
    }

    private static void processFiles(List<String> inputFiles, String outputPath, String prefix, boolean appendMode, boolean shortStats, boolean fullStats) {
        Map<String, List<String>> sortedData = new HashMap<>();
        sortedData.put("integers", new ArrayList<>());
        sortedData.put("floats", new ArrayList<>());
        sortedData.put("strings", new ArrayList<>());

        for (String fileName : inputFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    classifyData(line, sortedData);
                }
            } catch (IOException e) {
                System.out.println("Ошибка при чтении файла " + fileName + ": " + e.getMessage());
            }
        }

        writeSortedData(sortedData, outputPath, prefix, appendMode);
        printStatistics(sortedData, shortStats, fullStats);
    }

    private static void classifyData(String line, Map<String, List<String>> sortedData) {
        if (line.matches("-?\\d+")) {
            sortedData.get("integers").add(line);
        } else if (line.matches("-?\\d*\\.\\d+([eE][+-]?\\d+)?")) {
            sortedData.get("floats").add(line);
        } else {
            sortedData.get("strings").add(line);
        }
    }

    private static void writeSortedData(Map<String, List<String>> sortedData, String outputPath, String prefix, boolean appendMode) {
        Map<String, String> fileNames = Map.of(
                "integers", "integers.txt",
                "floats", "floats.txt",
                "strings", "strings.txt"
        );

        for (Map.Entry<String, List<String>> entry : sortedData.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String fileName = outputPath + File.separator + prefix + fileNames.get(entry.getKey());
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, appendMode))) {
                    for (String value : entry.getValue()) {
                        writer.write(value);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при записи файла " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    private static void printStatistics(Map<String, List<String>> sortedData, boolean shortStats, boolean fullStats) {
        for (Map.Entry<String, List<String>> entry : sortedData.entrySet()) {
            List<String> data = entry.getValue();
            if (data.isEmpty()) continue;

            if (shortStats) {
                System.out.println("Статистика для " + entry.getKey() + ":");
                System.out.println("Количество элементов: " + data.size());
            }

            if (fullStats) {
                System.out.println("Статистика для " + entry.getKey() + ":");
                System.out.println("Количество элементов: " + data.size());
                if (entry.getKey().equals("integers") || entry.getKey().equals("floats")) {
                    List<Double> numbers = data.stream().map(Double::parseDouble).toList();
                    System.out.println("Мин: " + Collections.min(numbers));
                    System.out.println("Макс: " + Collections.max(numbers));
                    System.out.println("Сумма: " + numbers.stream().mapToDouble(Double::doubleValue).sum());
                    System.out.println("Среднее: " + numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                } else if (entry.getKey().equals("strings")) {
                    String minStr = data.stream().min(Comparator.comparingInt(String::length)).get();
                    String maxStr = data.stream().max(Comparator.comparingInt(String::length)).get();
                    System.out.println("Самая короткая строка (длина): " + minStr.length());
                    System.out.println("Самая длинная строка (длина): " + maxStr.length());
                }
            }
        }
    }
}
