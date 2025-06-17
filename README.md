# About Request Filter 
Simple Request Filter for Enonic XP that prints every request to the server log. Very useful for looking at your traffic in real time and debugging issues

# Features
- Logs all HTTP requests with timing, session, IP, host, status code, method and URL
- When DEBUG logging is enabled, logs POST request bodies with JSON content
- Automatically masks sensitive fields like passwords in JSON payloads
- Filters out asset, image and admin requests to reduce noise

# Output
T = How many seconds did the request take

S = What sessionID did the request have

I = Ip addresses

H = What hostname was used

C = What response code did the server return

M = What method was used (GET, POST etc)

R = What was the request including simple parameters and optionally JSON body

!! The filter will not process requests that are related to resources or anything related to /admin !!

# Examples
```
T[0.125] S[] I[10.0.0.1] H[www.mysite.no] C[200] M[GET] R[/news/random?param=123]
```

With DEBUG logging enabled for POST requests with JSON:
```
T[0.256] S[ABC123] I[10.0.0.1] H[api.mysite.no] C[200] M[POST] R[/api/users|body={"username":"john","password":"xxxxx"}]
```

# Configuration
To enable JSON body logging for POST requests, set the log level for `no.tine.web.enonic.RequestFilter` to DEBUG in your logging configuration.

# Todo: 
- ~~Mask passwords in parameters~~
- Handle exceptions and still get the log output. Otherwise the chain will just terminate and not log anything
- ~~Add support for json input data. Now it only handles simple url/post parameters~~
