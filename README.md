## daraja

A project to show example usage of [clojure-mpesa-wrapper](https://github.com/MawiraIke/clojure-mpesa-wrapper)


### Overview
#### Working endpoints 
1. Base64 Encoding
2. M-Pesa Auth
3. Check balance API
4. B2B API
5. B2C API
6. C2B Register
7. C2B Simulate
8. Transaction status
9. Lipa na MPesa

> Example usages is demonstrated in clj/daraja.keys

### Production
To run the production version without the repl run
    
    lein run
    
and watch the opened tab in your browser.

### Development

To get an interactive development environment with figwheel run:

    lein fig:build

This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

	lein clean

After this run lein to start the server from the server side (Clojure code) with

    lein repl
    
After the server starts, load ```start-server!``` and watch for port ```10666``` or provide 
a different port to ```start-server!```
## License

Copyright © 2020 Ike Mawira

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
