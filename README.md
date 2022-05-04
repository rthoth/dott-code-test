# Dott code test.

This project is using Scala 2.13.8, and you must have the sbt installed in your computer.

In sbt console you just need to use de assembly plugin, as shown bellow.

```
sbt:dott-code-test> assembly
```

After this a runnable JAR will be created at `target/scala-2.13/dott.jar`, to run the program, just do this for example:

``
java -jar target/scala-2.13/dott.jar 2019-05-12 01:00:00 2019-05-12 05:00:00 "1-6" "7-8" "9-15" ">16"
``

**Important**, In the first time the program will create a new random database, please wait. This database probably
will have about 500 products and 590000 orders. 