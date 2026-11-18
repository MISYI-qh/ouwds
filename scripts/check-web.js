const s=require("fs").readFileSync("web/index.html","utf8");
new Function(s.split("<script>")[1].split("</script>")[0]);console.log("web/index.html OK ("+Math.round(s.length/1024)+" KB)");
