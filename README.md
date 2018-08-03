# babel

A proof of concept tool for transforming error message in Clojure into beginner friendly forms.
Very early in development.
Paper describing its use and development availible [here](https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf "MICS Paper").

## Usage

Copy ``babel``, ``errors``, and ``loggings`` into your src directory, and then make sure your ``project.clj`` has compatible version of all packages used in our ``project.clj``. Be sure to include the ``:nrepl-middleware`` keys.

## logging

To provide a transparent testing environment, the logging system creates logs that record your testing code, its actual modified error message and its original error message.

See details at [logging manual](/doc/logging.md).

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
