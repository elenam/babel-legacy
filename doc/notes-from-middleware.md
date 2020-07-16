# Preamble
Gievn that we no longer need to use interceptor, IE, lein middleware, and should instead work for anything that can run a function before a user REPL is presented to them, we can improve our setup.

# Leiningen
I note that we could potentially be a [lein plugin](https://github.com/technomancy/leiningen/blob/stable/doc/PLUGINS.md) instead of a middleware. Note that the documentation calls us a bad plugin if we just want to use 'eval-in-project, but the other option could be a "babel-repl" alias, though not necesarily with that name. This would probably be best accomplished in a seperate repo, IE, have a babel project, and a babel-lein plugin.

TODO
[]- Remove fossil middleware code
[]- Create lein plugin,
[]- Adjust babel to use the plugin in a dev profile

