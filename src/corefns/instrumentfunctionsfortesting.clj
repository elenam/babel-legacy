(ns babel.instrumentfunctionsfortesting
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.java.jdbc :as sql]
   [clojure.java.jdbc.spec]))

(stest/instrument `sql/create-table-ddl)
(stest/instrument `sql/update!)
(stest/instrument `sql/query)
(stest/instrument `sql/reducible-result-set)
(stest/instrument `sql/reducible-query)
(stest/instrument `sql/find-by-keys)
(stest/instrument `sql/get-by-id)
(stest/instrument `sql/db-query-with-resultset)
(stest/instrument `sql/db-do-prepared)
(stest/instrument `sql/db-do-prepared-return-keys)
(stest/instrument `sql/db-do-commands)
(stest/instrument `sql/metadata-query)
(stest/instrument `sql/metadata-result)
(stest/instrument `sql/with-db-metadata)
