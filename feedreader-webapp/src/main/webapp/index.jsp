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
</style>
</head>
<body>
<h2>Sign In</h2>
<form action="/signIn" autocomplete="off" method="POST">
<div><label for="signInEmail">Email Address</label></div>
<input id="signInEmail" name="email" type="text" placeholder="Email" />
<div>
	<input type="submit" value="Sign In" />
</div> 
</form>
<h2>Sign Up</h2>
<form action="/createUser" autocomplete="off" method="POST">
<div><label for="createEmail">Email Address</label></div>
<input id="createEmail" name="email" type="text" placeholder="Email" />
<div>
	<input type="submit" value="Create" />
</div> 
</form>
</body>
</html>