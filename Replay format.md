Replay format used by TUI Sweeper  
Revision `4`

> The whole file is compressed using `ZLIB`

### General format

| Field name    |     Type      | Ex. Value |
|---------------|:-------------:|:---------:|
| Header length |     byte      |     7     |
| Header        |   byte\[\]    |  TUIRPL1  |
| Seed          |     long      |   *n/a*   |
| Identifier    |  byte\[10\]   |   TEST    |
| Start time    |     long      |   *n/a*   |
| Create time   |     long      |   *n/a*   |
| Difficulty    |     byte      |     1     |
| Board width   |     byte      |    10     |
| Board height  |     byte      |    10     |
| Bombs count   |      int      |    10     |
| Bombs         |  Bombs array  |   *n/a*   |
| Actions count |      int      |    10     |
| Actions       | Actions array |   *n/a*   |

Bomb format:

| Field name | Type | Ex.Value |
|------------|:----:|:--------:|
| Bomb X     | byte |    0     |
| Bomb Y     | byte |    0     |

## Actions

Actions are the most important data stored in a replay.  
Each caret move, flag/unflag and field reveal is stored as a separate action.
> Action coordinates are stored as `byte`s, meaning the board can have maximum width/height of `128` fields

### [Action format](https://github.com/Defective4/TUI-Mines/blob/a05ac9ed50d890535e76b2abaa9101d227960216/src/main/java/io/github/defective4/javajam/tuisweeper/core/replay/Replay.java#L23)

| Field name | Type | Ex. Value |
|------------|:----:|:---------:|
| Action ID  | byte |     1     |
| Action X   | byte |     0     |
| Action Y   | byte |     0     |
| Timestamp  | int  |   *n/a*   |

### [Action IDs](https://github.com/Defective4/TUI-Mines/blob/a05ac9ed50d890535e76b2abaa9101d227960216/src/main/java/io/github/defective4/javajam/tuisweeper/core/replay/Replay.java#L146C6-L146C6)

| Action Type | ID |
|-------------|:--:|
| CARET       | 0  |
| REVEAL      | 1  |                                      |
| FLAG        | 2  |