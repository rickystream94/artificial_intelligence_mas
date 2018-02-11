param (
    [Parameter(Mandatory=$true)][string]$LevelName,
    [string]$SearchType = "bfs",
    [int]$MillisPerGuiAction = 50,
    [int]$TimeoutInSeconds = 300
 )

# Recompile classes
javac searchclient/*.java

# Launch server + search client
java -jar server.jar -l "levels/$LevelName.lvl" -c "java -Xmx4g searchclient.SearchClient -$SearchType" -g $MillisPerGuiAction -t $TimeoutInSeconds