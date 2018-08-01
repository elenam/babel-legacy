# Logging

Logging tool provides you a transparent testing environment. It records your testing code, its actual modified error message and its original error message.

## Usage

1. Copy the loggings folder to your src directory with babel (logging tool needs to work with babel!).
2. Add ``[hiccup "1.0.5"]`` into your ``project.clj`` dependencies. Hiccup is required and used for auto-generating html file.
3. Include ``(:use [loggings.loggingtool :only [get-error start-log add-log]])`` in each of your testing file's namespace.
4. Call ``(start-log)`` in the first testing file as the very first function.
5. Call ``(expect nil (add-log
              (do
                (def file-name "this file")
                (:file (meta #'file-name)))))``
   at the beginning of each testing file to get the tested file names.

## Logging contents
