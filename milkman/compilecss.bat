@echo off


"%JAVA_HOME%\bin\javapackager" -createbss -srcdir "src\main\resources\themes\" -outdir "src\main\resources\themes\"
"%JAVA_HOME%\bin\javapackager" -createbss -srcdir "src\main\resources\themes\syntax\" -outdir "src\main\resources\themes\syntax\"

