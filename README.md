# System Monitor

This plugin will monitor the various subsystems required by dotCMS and report on their availability.  It tests cache reads, file system read/writes, index and db availability.  It will fail fast, with a timeout of 5 seconds for each check.  This can be configured by editing the constant timeout values here  

https://github.com/dotCMS/plugin-system-monitor/blob/master/src/main/java/com/dotcms/plugin/rest/monitor/MonitorResource.java#L63

and recompiling.

to build the plugin, 

```
./gradlew jar
```



```
hot-fries:com.dotcms.plugin.rest.monitor will$ curl http://localhost:8080/api/v1/system-status
{
  "asset_fs_rw": false,
  "cache": true,
  "dbSelect": true,
  "indexLive": true,
  "indexWorking": true,
  "local_fs_rw": true
}
```
