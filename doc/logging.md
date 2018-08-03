# Logging manual

Logging tool provides you a transparent testing environment. It records your testing code, its actual modified error message and its original error message.

## Usage

1. Copy the loggings folder to your src directory with babel (logging tool needs to work with babel!).
2. Add ``[hiccup "1.0.5"]`` into your ``project.clj`` dependencies. Hiccup is required and used for auto-generating html file.
3. Include ``(:use [loggings.loggingtool :only [get-error start-log add-log]])`` in each of your testing file's namespace.
4. Call ``(start-log true)`` in the first testing file as the very first function.
5. You can always turn on/off the logging system by changing ``(start-log *boolean*)``.
6. Call ``(expect nil (add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))``
   at the beginning of each testing file to get the tested file names.
7. IMPORTANT! Only calling function ``(get-error *testing-expr*)`` will generate the log.
8. After running the test, a log folder will be generated in you project. Open the log_category.html to check the logs.
9. There is also a .txt version log which records the latest test log.

## Logging contents

This is how the test log looks like:

![This is the logging screen shot](/doc/img/logging0000.png)

You are able to filter out the nil result:

![This is the logging screen shot](/doc/img/logging0001.png)

You are also able to see the testing details that are retrieved from our middleware:

![This is the logging screen shot](/doc/img/logging0002.png)

## Trouble shooting

1. If you delete a log in the /log/history folder, it's name will still remain in the log_category until next time you run the test.
2. If the log file shows "Error loading test data!!!", it is likely that your test is broken.
