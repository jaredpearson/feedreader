<!DOCTYPE html>
<html>
<head>
<title>FeedReader</title>
<link href="https://fonts.googleapis.com/css?family=Nunito+Sans" rel="stylesheet">
<style>
body {
    font-family: 'Nunito Sans', sans-serif;
    margin: 0;
    padding-left: 1em;
    padding-top: 10px;
    padding-right: 1em;
}
.codeblock {
    background-color: #eaeaea;
    padding: 1em;
    font-family: monospace;
}
</style>
<head>
<body>
<h1>FeedReader</h1>
<p>This website is primarily REST based application. Below are some instructions on how to get started.</p>
<p>
    Your Session ID: ${sid}
</p>
<h2>Examples</h2>
<h3>Viewing the stream</h3>
<p>Gets a stream, which is a combination of all RSS feeds to read at once.</p>
<div class="codeblock">
    curl http://localhost:8080/services/v1/stream -H "Authorization:SID ${sid}"
</div>

<h3>Adding a RSS Stream</h3>
<p>To request a new feed be added to the stream:</p>
<div class="codeblock">
    curl http://localhost:8080/services/v1/feedSubscriptions -H "Authorization:SID ${sid}" -X POST -H "Content-Type: application/json" -d '{"url":"http://www.nasa.gov/rss/dyn/breaking_news.rss"}'
</div>
</body>
</html>