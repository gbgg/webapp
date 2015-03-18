// Compiled by ClojureScript 0.0-2311
goog.provide('webapp.core');
goog.require('cljs.core');
webapp.core.handle_click = (function handle_click(){return alert("Cf. src/cljs/webapp/core.cljs");
});
webapp.core.clickable = document.getElementById("clickable");
webapp.core.clickable.addEventListener("click",webapp.core.handle_click);

//# sourceMappingURL=core.js.map