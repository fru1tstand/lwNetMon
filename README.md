# lwNetMon
Lightweight Network Monitor - for *nix systems

---

lwNetMon monitors network traffic on a given interface, and exposes a web server that shows the
network usage. Network monitoring is done via reading `/proc/net/dev`. Web server is implemented
with `ktor`.
