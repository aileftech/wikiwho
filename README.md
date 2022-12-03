#  Wikiwho

### Wikiwho is a tool to retrieve and analyze Wikipedia edits made by institutions, companies and governments.

It works by retrieving all the Wikipedia edits made by IP addresses that belong to known ranges and exposes a web application to navigate and analyze them. This process is long and cumbersome. If you want a high-level overview, you can read [the blog post](https://ailef.tech/2020/04/18/discovering-wikipedia-edits-made-by-institutions-companies-and-government-agencies/) or keep reading here if you're interested in running it. If you just want to try it, maybe you're lucky and [wikiwho.ailef.tecg](http://wikiwho.ailef.tech) is up running.

**NOTE** This project is not actively maintained and I wouldn't recommend anybody use it as a starting point. If I were to do it now I would make different choices, like using ElasticSearch instead of MongoDB + Lucene. If you want to build something similar, I suggest you re-use some parts like the Wikipedia XML parsing, the API requests and/or diff processing and build the rest from scratch. Due to its architecture Wikiwho has one big limitation: it doesn't support partial updates. This means that if you need newer data or if you want to add new IP ranges to detect, you must run the initialization again from scratch, and as you'll see this process will take days to complete.

## Requirements

To compile you'll need a JDK and Maven to install all the necessary Java dependencies. The code connects to a MongoDB server on localhost, so you'll need that running as well. It uses the default port 27017 with no authentication. If your MongoDB server is configured differently, you'll need to change these settings in the code before compiling (sorry, no config file).

## How to run

**1.** Clone the repo, move into the directory and run `mvn package` to compile. This will create a runnable JAR in the `target` directory named `wikiwho-jar-with-dependencies.jar`.

Before you run, you might want to add specific IP ranges you'll want to detect. **You won't be able to do this later!** Wikiwho reads the `resources/ranges/ranges.json` file, which already contains several hundreds IP ranges in the following format (sample):

```
{
  "ipranges": {
    "Langley AFB": [
      {
        "startIp": "132.54.0.0",
        "endIp": "132.54.255.255",
        "startIpLong": 2218131456,
        "endIpLong": 2218196991
      }
    ],
    "U. S. Strategic Command": [
      {
        "startIp": "157.217.0.0",
        "endIp": "157.217.255.255",
        "startIpLong": 2648244224,
        "endIpLong": 2648309759
      }
    ],
```

Notice you can specify an array of ranges in case you have multiple distinct ranges for the same organization. By the way, feel free to contribute IP ranges that you think may be of interest to the community!

Conversely, if you want to extract less edits remove IP ranges you're not interested in. This will speed up the following steps as there'll be less data to process.

**2.** Move the `wikiwho-jar-with-dependencies.jar` file from the `target` folder to the repo root and rename it as `wikiwho.jar`.

**3.** Download the latest Wikipedia `stub-meta-history` dump: https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-stub-meta-history.xml.gz

**4.** Run `java -jar wikiwho.jar -c <path_to_wikipedia_dump> out.tsv`. Pass the path to the (still gzipped) Wikipedia dump and a name for the output file (tab-separated values) that this will generate. This file will contain all the edit IDs in the Wikipedia dump that match one of our IP ranges, and it will look like this:

```
367054333       142.92.60.20    Industry Canada 2010-06-09T20:36:23Z
243756877       132.25.0.206    786 CS/SCBM     2008-10-07T22:35:56Z
513253905       128.157.160.13  NASA Johnson Space Center       2012-09-17T21:01:25Z
123846053       132.40.121.33   347 COMMUNICATION SQUADRON      2007-04-18T16:46:24Z
123846469       132.40.121.33   347 COMMUNICATION SQUADRON      2007-04-18T16:48:02Z
142721036       137.240.136.81  Air Force Materiel Command      2007-07-05T18:53:18Z
142889694       137.240.136.81  Air Force Materiel Command      2007-07-06T13:22:24Z
144166854       137.240.136.80  Air Force Materiel Command      2007-07-12T12:04:27Z
4357890 131.44.121.252  Randolph Air Force Base 2004-06-28T13:07:54Z
6044559 134.78.110.18   Army Information Systems Command-ATCOM  2004-09-21T17:46:44Z
23125016        207.30.242.144  Sprint/United Telephone of Florida      2005-09-12T22:14:54Z
30276654        207.30.244.108  Sprint/United Telephone of Florida      2005-12-05T23:39:23Z
76442452        128.190.62.54   Army Belvoir Reasearch and Development Center   2006-09-18T17:46:25Z
```

This step can take several hours. When the process is done you can `wc -l` to check how many edits have been extracted. This data will be used as input for the next step.

**5.** Run `java -jar wikiwho.jar -d out.tsv`. This will retrieve the actual content for each edit ID in the file, process the Wikipedia diff and save everything to MongoDB. In my experience this takes around 2.5 minutes per 1k Wikipedia diffs, so again this will take several hours. We're almost there, though! Last step is to build a Lucene index to allow full-text search and other features for the web app (form autocomplete).

**6.** If an empty `data/index` directory isn't already present (it should've been created by the previous steps), create it. Then run `java -jar wikiwho.jar -l data/index`. This will build the Lucene index from the data in MongoDB and it should be relatively fast, at least compared to the previous steps.

It's now possible to start the web server. There exists a `config/server.conf` file where you can set the server port (default 8080):

```
server.port = $PORT  # Port the web server is listening on
server.https = false # To run with HTTPS you need to set up certificates, which I'm not going to cover here
```

**7.** Run `java -jar wikiwho.jar -s` and visit `http://localhost:$PORT` to use the web app.