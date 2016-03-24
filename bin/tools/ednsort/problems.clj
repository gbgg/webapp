;; with asc, desc
Exception in thread "main" java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.lang.String
	at java.lang.String.compareTo(String.java:111)
	at clojure.lang.Util.compare(Util.java:153)
	at ednsort.core$desc.invoke(core.clj:32)
	at ednsort.core$compare_by.invoke(core.clj:35)
	at ednsort.core$tmapsort$fn__43.invoke(core.clj:45)
;; with 'asc, 'desc
Exception in thread "main" java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number
	at clojure.lang.Numbers.isZero(Numbers.java:90)
	at ednsort.core$compare_by.invoke(core.clj:36)
	at ednsort.core$tmapsort$fn__43.invoke(core.clj:45)
;; with asc, asc
Exception in thread "main" java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.lang.Comparable
	at clojure.lang.Util.compare(Util.java:153)
	at clojure.core$compare.invoke(core.clj:792)
	at ednsort.core$compare_by.invoke(core.clj:35)
	at ednsort.core$tmapsort$fn__43.invoke(core.clj:45)
;; with asc (compare), desc in (let [ . . .
Exception in thread "main" clojure.lang.ArityException: Wrong number of args (0) passed to: core$compare, compiling:(ednsort/core.clj:61:15)
	at clojure.lang.Compiler.analyzeSeq(Compiler.java:6567)
	at clojure.lang.Compiler.analyze(Compiler.java:6361)
	at clojure.lang.Compiler.access$100(Compiler.java:37)
	at clojure.lang.Compiler$LetExpr$Parser.parse(Compiler.java:5973)
;; with asc #(compare %1 %2), desc in (let [ . . .
Exception in thread "main" java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.lang.Comparable
	at clojure.lang.Util.compare(Util.java:153)
	at ednsort.core$do_lexterms$asc__98.invoke(core.clj:61)
	at ednsort.core$compare_by.invoke(core.clj:35)
	at ednsort.core$tmapsort$fn__40.invoke(core.clj:45)
;; with (def asc #(compare %1 %2)), (def desc 
Exception in thread "main" java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.lang.Comparable
	at clojure.lang.Util.compare(Util.java:153)
	at ednsort.core$asc.invoke(core.clj:30)
	at ednsort.core$compare_by.invoke(core.clj:35)
	at ednsort.core$tmapsort$fn__46.invoke(core.clj:45)
