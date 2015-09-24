package com.mehmetakiftutuncu.muezzin.utilities;

import android.os.Environment;

import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * A utility class for basic file operations
 *
 * @author mehmetakiftutuncu
 */
public class FileUtils {
    /** Full path of data folder to store data in external storage of device */
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + Conf.DATA_PATH;

    /** An optional File object pointing to {@link com.mehmetakiftutuncu.muezzin.utilities.FileUtils#DATA_PATH} */
    public static Option<File> dataPath = getDataPath();

    /**
     * Reads a file with given name from data folder into a String
     *
     * @param fileName Name of the file in data folder to read
     *
     * @return A Some&lt;String&gt; with contents of the file with given name or None if any error occurs
     */
    public static Option<String> readFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            Log.error(FileUtils.class, "Failed to read file, file name is empty!");
            return new None<>();
        } else {
            if (dataPath.isEmpty) {
                Log.error(FileUtils.class, "Failed to read file " + fileName + ", data path is None!");
                return new None<>();
            } else {
                File file = new File(dataPath.get().getAbsolutePath() + "/" + fileName);

                if (!file.exists() || !file.canRead()) {
                    Log.error(FileUtils.class, "Failed to read file " + file + ", it cannot be accessed!");
                    return new None<>();
                } else {
                    try {
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        StringBuilder stringBuilder = new StringBuilder();

                        while (bufferedReader.ready()) {
                            stringBuilder.append(bufferedReader.readLine()).append("\n");
                        }
                        bufferedReader.close();

                        return new Some<>(stringBuilder.toString());
                    } catch (Exception e) {
                        Log.error(FileUtils.class, "Failed to read file " + file + "!", e);
                        return new None<>();
                    }
                }
            }
        }
    }

    /**
     * Writes given String to a file with given name in data folder
     *
     * @param data     Data to write
     * @param fileName Name of the file to which data will be written
     *
     * @return true if successfully written or false if any error occurs
     */
    public static boolean writeFile(String data, String fileName) {
        if (StringUtils.isEmpty(data)) {
            Log.error(FileUtils.class, "Failed to write file " + fileName + ", data is empty!");

            return false;
        } else if (StringUtils.isEmpty(fileName)) {
            Log.error(FileUtils.class, "Failed to write " + data + " to file, file name is empty!");

            return false;
        } else {
            if (dataPath.isEmpty) {
                Log.error(FileUtils.class, "Failed to write " + data + " to " + fileName + ", data path is None!");

                return false;
            } else {
                File file = new File(dataPath.get().getAbsolutePath() + "/" + fileName);
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    bufferedWriter.write(data);

                    bufferedWriter.flush();
                    bufferedWriter.close();

                    return true;
                } catch (Exception e) {
                    Log.error(FileUtils.class, "Failed to write " + data + " to " + file + "!", e);

                    return false;
                }
            }
        }
    }

    /**
     * Gets a File object pointing to {@link com.mehmetakiftutuncu.muezzin.utilities.FileUtils#DATA_PATH}
     * making sure that all folders in the path exist
     *
     * @return A Some&lt;File&gt; object pointing to data folder or None if any error occurs
     */
    public static Option<File> getDataPath() {
        File path = new File(DATA_PATH);

        if ((!path.exists() && !path.mkdirs()) || !path.canRead()) {
            Log.error(FileUtils.class, "Failed to get data path, data directory cannot be accessed!");
            return new None<>();
        } else {
            return new Some<>(path);
        }
    }
}
