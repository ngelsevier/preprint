# Load testing Beta Search

Using the [Goad utility](https://goad.io/), we are able to test the performance of Beta Search.

An example command run:

````
./goad -n 0 -c 100 -s 5 -t 60 \ 
--region=us-east-1 --region=us-west-2 \
--region=eu-west-1 --region=eu-central-1 \
--region=ap-southeast-1 --region=ap-southeast-2 \
https://username:password@www.ssrn.com/n/fastsearch?query=Effects+of+the+Real+Plan+on+the+Brazilian+Banking+System
````

Which does:

* As many requests as possible within 60 seconds (-n 0 -t 60)
* A timeout of 5 seconds (-s 5)
* A concurrency of 100 lambdas (-c 100)
* In 6 regions

Resulted in:

````
Region: ap-southeast-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
       866     2.6 MB     0.688s       13.10      40 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.789s     0.508s          0          0
Region: ap-southeast-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
       858     2.7 MB     0.695s       12.56      40 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.777s     0.508s          0          0
Region: eu-central-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      1721     5.3 MB     0.692s       25.18      77 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.874s     0.508s          0          0
Region: eu-west-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      1701     5.4 MB     0.701s       25.06      79 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.856s     0.508s          0          0
Region: us-east-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      1699     5.5 MB     0.701s       25.40      82 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.731s     0.417s          0          0
Region: us-west-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      1671     5.3 MB     0.713s       24.71      78 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.232s     0.508s          0          0

Overall

   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      8516      27 MB     0.700s      126.01     396 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.232s     0.417s          0          0
HTTPStatus   Requests
       200       8516
       
````

We subsequently ran a similiar test with the concurrency set to 200, and got these results:

````
Region: ap-southeast-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      3080     8.1 MB     0.582s       47.62     125 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.712s     0.507s          0          0
Region: ap-southeast-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      3116     8.0 MB     0.574s       47.70     122 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.634s     0.508s          0          0
Region: eu-central-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      3100     7.9 MB     0.578s       47.60     121 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.929s     0.507s          0          0
Region: eu-west-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      3139     8.0 MB     0.571s       47.92     122 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.936s     0.507s          0          0
Region: us-east-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      4087      11 MB     0.585s       63.06     164 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.458s     0.461s          0          0
Region: us-west-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
      4188      11 MB     0.570s       64.83     165 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.778s     0.508s          0          0

Overall

   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     20710      53 MB     0.577s      318.73     819 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.458s     0.461s          0          0
HTTPStatus   Requests
       200      20710   
````

Running 300 current instances in 6 regions for two minutes:

```
./goad -n 0 -c 300 -s 5 -t 120 --region=us-east-1 --region=us-west-2 --region=eu-west-1 --region=eu-central-1 --region=ap-southeast-1 --region=ap-southeast-2 https://ssrn-els:gEP8FuBY@www.ssrn.com/n/fastsearch?query=Effects+of+the+Real+Plan+on+the+Brazilian+Banking+System
Regional results

Region: ap-southeast-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10610      26 MB     0.564s       81.18     196 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.954s     0.507s          0          0
Region: ap-southeast-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10528      26 MB     0.569s       80.31     195 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.673s     0.507s          0          0
Region: eu-central-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10596      26 MB     0.565s       80.91     195 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.154s     0.507s          0          0
Region: eu-west-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10603      26 MB     0.564s       80.99     197 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.076s     0.508s          0          0
Region: us-east-1
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10605      26 MB     0.564s       81.13     198 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    1.918s     0.471s          0          0
Region: us-west-2
   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     10454      26 MB     0.573s       79.76     195 kB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.888s     0.507s          0          0

Overall

   TotReqs   TotBytes    AvgTime    AvgReq/s  (post)unzip
     63396     154 MB     0.566s      484.27     1.2 MB/s
   Slowest    Fastest   Timeouts  TotErrors
    2.888s     0.471s          0          0
HTTPStatus   Requests
       200      63396
```



