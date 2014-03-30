JHU_NN_AnalysisApp
==================



A few points about git usage:
- please use semi-descriptive comments when pushing to the repo

- generally, it is bad to push data that will be processed into repositories
  this bloats the repo
  we should keep the data outside of the repo...
  UNLESS the data is small enough, then it would be more convenient to keep it in the repo
  we'll see how much data we have first

- the /JARs folder is intended to hold imports
  any libraries that you add should be added there
  the project's Java build path should point to .jar in that folder when adding a library
  example, I added gson to the build path and put the .jar in the /JARs folder