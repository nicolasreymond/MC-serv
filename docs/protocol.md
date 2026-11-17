# MC-serv protocol specification

## 1. Overview

MC-serv is a shared bank of image tiles (BMP), hosted on a TCP server and
accessed by several clients at the same time. A client can:

- list the tiles currently available in the bank,
- upload a tile to the bank,
- download a tile from the bank,
- remove a tile from the bank,
- ask the server to build a photo mosaic of a target image, using the
  tiles currently in the bank.

Any client can upload, download or remove any tile — the bank has no
notion of ownership or authentication, by design (see
[Design decisions](#5-design-decisions)).

## 2. Transport

- **TCP**, default port `7000` (overridable with `-p`/`--port` on both
  `server` and `client`).
- Commands are **UTF-8 text, one per line**, terminated by `\n` —
  same framing as the SMS protocol seen in class.
- **Binary payloads** (BMP tiles) cannot be framed the same way: a BMP
  file can contain any byte value, including `\n`, so it cannot be
  delimited by a line terminator. Instead, any command that carries a
  BMP payload includes the payload's exact size in bytes as the last
  argument on its command line; the receiver then reads exactly that
  many bytes immediately after the line, before parsing anything else.
- The client opens the connection and speaks first. There is no `JOIN`:
  the bank is not per-client, so no identification is needed.
- A client may send several commands on the same connection (REPL). It
  closes the connection when done; there is no explicit `QUIT` command
  (closing the socket is enough, same reasoning as SMS: a crashed
  client looks the same as one that quit cleanly).
- Unknown commands, or a command that cannot be parsed, get `ERROR 5`.

## 3. Messages

### `LIST`

Request:
```
LIST\n
```

Response:
```
OK <count>\n
<name> <width>x<height>\n   (repeated count times)
```

No error case (an empty bank simply returns `OK 0\n`).

### `UPLOAD <name> <size>` + binary payload

Request:
```
UPLOAD <name> <size>\n
<size bytes of BMP data>
```

Response (success):
```
OK\n
```

Errors:
- `ERROR 1\n` — a tile named `<name>` already exists in the bank.
- `ERROR 3\n` — the payload is not a valid BMP file (bad signature,
  compressed, unsupported bit depth, etc. — same checks as PW1's
  `BmpImage.readFrom`).

### `DOWNLOAD <name>`

Request:
```
DOWNLOAD <name>\n
```

Response (success):
```
OK <size>\n
<size bytes of BMP data>
```

Errors:
- `ERROR 2\n` — no tile named `<name>` in the bank.

### `REMOVE <name>`

Request:
```
REMOVE <name>\n
```

Response (success):
```
OK\n
```

Errors:
- `ERROR 2\n` — no tile named `<name>` in the bank.

### `MOSAIC <cols> <rows> <size>` + binary payload

Request:
```
MOSAIC <cols> <rows> <size>\n
<size bytes of BMP data (target image)>
```

The server splits the target image into a `<cols>` × `<rows>` grid
(same truncation rule as PW1's `split`: non-divisible dimensions are
handled by integer division, the leftover pixels on the right/bottom
edge are dropped). For each cell, it computes the average RGB color and
picks, among the tiles currently in the bank, the one whose average
color is closest (Euclidean RGB distance). The chosen tile is resized to
fit the cell and pasted into the result image.

Response (success):
```
OK <size>\n
<size bytes of BMP data (the mosaic)>
```

Errors:
- `ERROR 3\n` — the target image is not a valid BMP.
- `ERROR 4\n` — the bank is empty (nothing to build a mosaic from).

### Error codes summary

| Code | Meaning | Raised by |
|---|---|---|
| `ERROR 1` | Name already taken | `UPLOAD` |
| `ERROR 2` | Tile not found | `DOWNLOAD`, `REMOVE` |
| `ERROR 3` | Invalid BMP payload | `UPLOAD`, `MOSAIC` |
| `ERROR 4` | Bank is empty | `MOSAIC` |
| `ERROR 5` | Unknown or malformed command | any |

## 4. Examples

### Success — two clients sharing the bank

```
Client A                         Server                         Client B
   |--- UPLOAD forest 352000 -------->|                               |
   |         <352000 bytes>           |                               |
   |<-------------- OK ---------------|                               |
   |                                  |<---------- LIST --------------|
   |                                  |--- OK 1\nforest 400x300 ----->|
   |                                  |<--- UPLOAD abstract 360000 ---|
   |                                  |          <360000 bytes>       |
   |                                  |-------------- OK ------------>|
   |                                  |<--- MOSAIC 20 15 360054 ------|
   |                                  |          <360054 bytes>       |
   |                                  |--- OK 360054\n<...> --------->|
   |<-------------- LIST -------------|                               |
   |--- OK 2\nforest ...\nabstract ...|                               |
```

Client A sees `abstract` in the bank right after client B uploaded it,
without reconnecting — proof that the bank is shared in real time.

### Error — duplicate name

```
Client A                         Server
   |--- UPLOAD forest 352000 -------->|
   |         <352000 bytes>           |
   |<-------------- OK ---------------|

Client B                         Server
   |--- UPLOAD forest 340000 -------->|
   |         <340000 bytes>           |
   |<------------ ERROR 1 ------------|
```

### Error — removing a tile that doesn't exist

```
Client A                         Server
   |--- REMOVE unicorn --------------->|
   |<------------ ERROR 2 -------------|
```

## 5. Design decisions

- **No authentication / ownership.** The grading rubric does not score
  security, and the course's own SMS model has no notion of permissions
  either (`JOIN` only picks a name, it doesn't protect anything). Adding
  client identity and per-tile ownership would mean a login-like
  handshake, per-connection state, and more error cases to test, for no
  points gained — not worth it given the timeline. Any client can
  therefore `REMOVE` any tile.
- **Length-prefixed binary framing**, rather than a text delimiter, so
  that BMP payloads (arbitrary bytes) can be transmitted safely over the
  same line-oriented connection used for commands.
- **Reuses PW1's `BmpImage`** for BMP parsing and the mosaic algorithm
  (average color per cell, nearest-match by RGB distance) rather than
  reimplementing it — see the README for details.
