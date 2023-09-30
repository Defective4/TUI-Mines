Replay format used by TUI Sweeper
Revision 1

General format:

| Field name    | Type          | Ex. Value |
|---------------|---------------|-----------|
| Header length | int           | 7         |
| Header        | byte\[\]      | TUIRPL1   |
| Identifier    | byte\[10\]    | TEST      |
| Start time    | long          | *n/a*     |
| Difficulty    | byte          | 1         |
| Board width   | int           | 10        |
| Board height  | int           | 10        |
| Bombs count   | int           | 10        |
| Bombs         | Bombs array   | *n/a*     |
| Actions count | int           | 10        |
| Actions       | Actions array | *n/a*     |

Bomb format:

| Field name | Type | Ex.Value |
|------------|------|----------|
| Bomb X     | int  | 0        |
| Bomb Y     | int  | 0        |

Action format:

| Field name | Type | Ex. Value |
|------------|------|-----------|
| Action ID  | byte | 1         |
| Action X   | int  | 0         |
| Action Y   | int  | 0         |
| Timestamp  | long | *n/a*     |
