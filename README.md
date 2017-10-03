Two identical play applications, one as a play 2.5.17, backed by netty.
The other is a play 2.6.5 backed by akka-http.
apps were generated using `sbt new playframework/play-scala-seed.g8` and were added the same APIs.

- `GET /i` will operate against a simple sync echo endpoint (use query parameter `?echo=HelloWorld`)
- `GET /s` will operate against an async scheduled endpoint (use query parameter `?echo=HelloWorld` and optional `&delay=5` in seconds that defaults to 1 second delayed response) 
- `GET /a` will operate against an instant async endpoint (use query parameter `?real` to determine if `Future.apply` or `Future.successful`)
- `/stream/rand` will operate a rolling hash stream (use query parameter `?size=5242880` to set returned payload size, and `&seed=123~SGVsbG9Xb3JsZAo`)
- `/astream/rand` will operate a rolling async hash stream (use query parameter `?size=5242880` to set returned payload size, and `&seed=123~SGVsbG9Xb3JsZAo`)

All handlers are defind in `StreamController`.
