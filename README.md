# Xp-app-requestfilter
Simple Request Filter for Enonic XP that prints every request to the server log. Very useful for looking at your traffic in real time and debugging issues

# Output
T = How many seconds did the request take

S = What sessionID did the request have

I = Ip addresses

H = What hostname was used

C = What response code did the server return

M = What method was used (GET, POST etc)

R = What was the request including simple parameters

!! The filter will not process requests that are related to resources or anything related to /admin !!

# Example
```
T[0.125] S[] I[10.0.0.1] H[www.mysite.no] C[200] M[GET] R[/news/random?param=123]
```

# Todo: 
- Handle exceptions and still get the log output. Otherwise the chain will just terminate and not log anything
- Add support for json input data. Now it only handles simple url/post parameters
