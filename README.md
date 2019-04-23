# babel-middleware

[![Clojars Project](https://img.shields.io/clojars/v/babel-middleware.svg)](https://clojars.org/babel-middleware)

A proof of concept tool for transforming error message in Clojure into beginner friendly forms.
Very early in development, and will experience breaking changes.
Paper describing its use and development availible [here](https://github.com/Clojure-Intro-Course/mics2018demo/blob/master/mics2018.pdf "MICS Paper").

## Usage
Adjust your project.clj as follows, and ensure you are running clojure 1.9.0 or later.
```
:dependencies [[clojure "1.10.0"]
               [babel-middleware "0.2.0-alpha"]]
:repl-options {:nrepl-middleware
              [babel.middleware/interceptor]}
```            
Launch a repl with ```lein repl```, or whichever tool you prefer.
(Has not been tested outside of Atom/proto-repl, and lein repl)
## logging

To provide a transparent testing environment, the logging system creates logs that record your testing code, its actual modified error message and its original error message.

See details at [logging manual](/doc/logging.md).

## License

Copyright © 2018

Distributed under the MIT license
