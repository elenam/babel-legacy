# Running Files
**Files cannot be run using ``lein run``.**

Follow the following steps to run outside files:

1. Start the repl with `lein repl`
2. Run `(load-file "<filepath>")`
3. Run `(<file namespace>/<function>)`, if you want to run the main function, it will be `-main`

Files cannot be run if they make use of dependencies not specified in
babel's project.clj file. So if you want to use something like
`clojure.math.combinatorics` in a file you will need to add
`[org.clojure/math.combinatorics "0.1.5"]` to the dependencies in
project.clj.
