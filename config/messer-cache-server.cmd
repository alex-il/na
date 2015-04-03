@echo off
@
@rem This will start a cache server
@
setlocal

:config
@rem specify the Coherence installation directory
set coherence_home=%~dp0\..

@rem specify the JVM heap size
set memory=512m


:start
if not exist "%coherence_home%\lib\coherence.jar" goto instructions

if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)


:launch

if "%1"=="-jmx" (
	set jmxproperties=-Dcom.sun.management.jmxremote -Dtangosol.coherence.management=all -Dtangosol.coherence.management.remote=true
	shift  
)	

set java_opts=-Xms%memory% -Xmx%memory% %jmxproperties%  -Djava.net.preferIPv4Stack=true  

set java_opts=%java_opts% -Dtangosol.coherence.cacheconfig=%coherence_home%\bin\messer-coherence-cache-config.xml -Dtangosol.coherence.override=%coherence_home%\bin\messer-tangosol-coherence-override.xml -Dtangosol.pof.config=%coherence_home%\bin\messer-pof-config.xml -Dtangosol.coherence.log.level=9 -Dtangosol.coherence.ttl=0


@echo %java_exec% -server -showversion %java_opts% -cp "c:\temp\messer.jar;%coherence_home%\lib\coherence.jar;d:\j-scala\MesserLeumi\lib\coherence-common-2.3.0.39174.jar" com.tangosol.net.DefaultCacheServer %1
@pause
%java_exec% -server -showversion %java_opts% -cp "c:\temp\messer.jar;%coherence_home%\lib\coherence.jar;d:\j-scala\MesserLeumi\lib\coherence-common-2.3.0.39174.jar" com.tangosol.net.DefaultCacheServer %1

goto exit

:instructions

echo Usage:
echo   ^<coherence_home^>\bin\cache-server.cmd
goto exit

:exit
endlocal
@echo on
