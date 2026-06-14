# Scoring Rules

Scoring can tach 2 he rieng:

- EXP/Level: do muc do cham chi va van dong deu.
- Rank/Rank Point: do tien bo lien quan den suc manh va nhom co.

## Loai hoat dong

### Strength

Tap ta, bodyweight, machine, cable.

- Cong Rank Point nhieu.
- Cong EXP vua phai.
- Cong diem cho nhom co chinh va phu.

### Cardio

Chay, dap xe, nhay day, HIIT nhe.

- Cong EXP nhieu.
- Cong Rank Point it.
- Chu yeu anh huong Cardio rank.

### Sport

Bong da, cau long, bong ro, boi, vo thuat.

- Cong EXP nhieu.
- Cong Rank Point it den vua tuy mon.
- Co the cong them cho legs/core/cardio.

## Cong thuc MVP

Cong thuc dau tien nen de don gian va de giai thich:

```text
Strength:
  exp = duration_minutes * 2 + total_volume_kg / 100
  rank_point = duration_minutes + total_volume_kg / 50

Cardio:
  exp = duration_minutes * 4
  rank_point = duration_minutes / 3

Sport:
  exp = duration_minutes * 3
  rank_point = duration_minutes / 4
```

## Level

```text
level = 1 + floor(total_exp / 1000)
current_level_exp = total_exp % 1000
```

## Rank

```text
Bronze:   0 - 999
Silver:   1000 - 2499
Gold:     2500 - 4999
Platinum: 5000 - 7999
Diamond:  8000+
```

## Streak

- 1 tuan du 3 buoi tap hop le: giu streak.
- Duoi 3 buoi/tuan: streak bi reset tu tuan tiep theo.
- 1 buoi hop le can co it nhat 10 phut hoat dong hoac 3 set strength.

## Nguyen tac can bang

- Cardio khong bi thiet: van len level nhanh.
- Tap tang co khong bi thiet: rank nhom co tang ro.
- Tap lech qua nhieu: Bodygraph hien nhom co bi bo quen.
