torrent-batch
=============

Batch which allow to monitor Transmission torrent states by sending RPC call. 


Available jobs
---------------

### Activity Date

#### Goal

This job queries transmission by sending a RPC call and determine which seeding torrents are
exhausted, then write them into a file. Are exhausted all torrents which are seeding and have
a last activity date older than X days, where X is a custom value. 

#### How to run it? 

```
org.springframework.batch.core.launch.support.CommandLineJobRunner jobs/activity-job.xml activityDateJob
```

### Statistics

#### Goal

This job queries transmission by sending a RPC call and determine seeding torrents. For each
of them, it inserts into a database current uploaded bytes in order to compute further
statistics.

#### Database schema

Torrent(id (int), name (string), hash (string), downloadedBytes (string), status (string), dat_creprod (date), dat_supprod (date))
TorrentStat(id_torrent (int), dat_snapshot (date), upldoadedBytes (bigint))

#### How to run it?

```
org.springframework.batch.core.launch.support.CommandLineJobRunner jobs/statistics-job.xml statisticsJob
```