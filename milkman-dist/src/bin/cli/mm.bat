@.\jre-win64\bin\java.exe ^
	-client ^
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
	-cp plugins\*;milkman.jar;milkman-cli.jar milkmancli.MilkmanCli %*
	